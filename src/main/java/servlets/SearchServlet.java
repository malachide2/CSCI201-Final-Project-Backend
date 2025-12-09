package servlets;

import database.DBConnector;
import database.Hike; 
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/api/hikes")
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final Gson gson = new Gson();
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
        
		// Add CORS headers
		setCorsHeaders(response);
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		// --- 1. Parameter Extraction ---
		String searchQuery = request.getParameter("q");
		String difficulty = request.getParameter("difficulty");
		String minLengthStr = request.getParameter("min_length");
		String maxLengthStr = request.getParameter("max_length");
		String minRatingStr = request.getParameter("min_rating");
		
		try {
			// List of Hike objects returned from the database
			List<Hike> hikes = executeSearch(searchQuery, difficulty, minLengthStr,
												maxLengthStr, minRatingStr);
			
			// --- 2. JSON Response ---
			String jsonResponse = gson.toJson(hikes);
			response.getWriter().write(jsonResponse);
			
		} catch(SQLException e){
            log("Database error: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
            response.getWriter().write(gson.toJson(new ErrorResponse("Database error: " + e.getMessage())));
		} catch(NumberFormatException e) {
            log("Client error: Invalid number format provided for filter: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); 
            response.getWriter().write(gson.toJson(new ErrorResponse("Filter parameters must be valid numbers.")));
		} catch(Exception e) {
            log("Server error: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
            response.getWriter().write(gson.toJson(new ErrorResponse("An unexpected server error occurred.")));
		}
	}
	
	// Utility class for standard error response JSON structure
	private static class ErrorResponse {
        String error;
        ErrorResponse(String error) { this.error = error; }
    }
	
	
	private List<Hike> executeSearch(String searchQuery, String difficulty, String minLengthStr, 
            String maxLengthStr, String minRatingStr) throws SQLException, NumberFormatException {

        // The list of parameters to be safely inserted into the PreparedStatement
		List<Object> params = new ArrayList<>();
        // The final list of Hike objects to return
		List<Hike> foundHikes = new ArrayList<>();

        // --- 1. Parse Parameters and Map Difficulty ---
        // Parse numbers safely (throws NumberFormatException on bad input)
		Double minLength = (minLengthStr != null && !minLengthStr.isEmpty()) ? Double.parseDouble(minLengthStr) : null;
		Double maxLength = (maxLengthStr != null && !maxLengthStr.isEmpty()) ? Double.parseDouble(maxLengthStr) : null;
		Double minRating = (minRatingStr != null && !minRatingStr.isEmpty()) ? Double.parseDouble(minRatingStr) : null;
		
        // Map difficulty string to exact DB values
        // Match the difficulty mapping used in AddHikeServlet:
        // Easy: 1, Moderate: 2.5, Hard: 4, Expert: 5
        // Use exact values or very tight ranges to match these specific values
		Double difficultyValue = null;
        
		if (difficulty != null && !difficulty.equalsIgnoreCase("All")) {
			if (difficulty.equalsIgnoreCase("Easy")) {
				difficultyValue = 1.0;
			} else if (difficulty.equalsIgnoreCase("Moderate")) {
				difficultyValue = 2.5;
			} else if (difficulty.equalsIgnoreCase("Hard")) {
				difficultyValue = 4.0;
			} else if (difficulty.equalsIgnoreCase("Expert")) {
				difficultyValue = 5.0;
			}
		}


        // --- 2. Build Dynamic SQL Query ---
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT h.hike_id, h.name, h.location_text, h.description, h.distance, h.difficulty, ");
		sql.append("COALESCE(AVG(r.rating), 0.0) AS average_rating, ");
		sql.append("COALESCE(COUNT(r.review_id), 0) AS total_ratings, ");
		// Subquery to get one photo's URL for the thumbnail
		sql.append("(SELECT p.image_url FROM photos p WHERE p.hike_id = h.hike_id ORDER BY p.created_at ASC LIMIT 1) AS thumbnail_url ");
		sql.append("FROM hikes h LEFT JOIN reviews r ON h.hike_id = r.hike_id ");
		sql.append("WHERE 1=1 "); // Base condition for easy AND appending

        // Append Search Condition (q)
		if (searchQuery != null && !searchQuery.trim().isEmpty()) {
			sql.append("AND (LOWER(h.name) LIKE LOWER(?) OR LOWER(h.location_text) LIKE LOWER(?)) ");
			params.add("%" + searchQuery + "%"); 
			params.add("%" + searchQuery + "%");
		}

        // Append Difficulty Filter (using exact value matching)
		if (difficultyValue != null) {
			// Use a small range (±0.1) to account for any floating point precision issues
			sql.append("AND h.difficulty >= ? AND h.difficulty <= ? "); 
			params.add(difficultyValue - 0.1);
			params.add(difficultyValue + 0.1);
		}

        // Append Length Filters
		if (minLength != null) {
			sql.append("AND h.distance >= ? ");
			params.add(minLength);
		}
		if (maxLength != null) {
			sql.append("AND h.distance <= ? ");
			params.add(maxLength);
		}

        // Group By Clause (Required for AVG(r.rating) and thumbnail subquery)
		sql.append("GROUP BY h.hike_id, h.name, h.location_text, h.description, h.distance, h.difficulty, h.elevation, h.created_at ");
		
        // Append Minimum Rating Filter (HAVING clause)
		if (minRating != null) {
			sql.append("HAVING COALESCE(AVG(r.rating), 0.0) >= ? ");
			params.add(minRating);
		}
		
        // Final Ordering
		sql.append("ORDER BY average_rating DESC, h.created_at DESC ");


        // --- 3. JDBC Execution ---
		try (Connection conn = DBConnector.getInstance().getConnection(); 
			 PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
			
            // Set parameters onto the PreparedStatement
			int index = 1;
			for (Object param : params) {
                if (param instanceof String) {
                    pstmt.setString(index++, (String) param);
                } else if (param instanceof Double) {
                    pstmt.setDouble(index++, (Double) param);
                } else {
                    pstmt.setObject(index++, param);
                }
			}
			
			try (ResultSet rs = pstmt.executeQuery()) {
                // --- 4. Map Results to Hike Objects ---
				while (rs.next()) {
					Hike hike = new Hike(
						rs.getInt("hike_id"),
						rs.getString("name"),
						rs.getString("location_text"),
						rs.getDouble("distance"),
						rs.getDouble("difficulty"),
						rs.getDouble("average_rating"),
						rs.getInt("total_ratings"),
						rs.getString("thumbnail_url")
					);
					foundHikes.add(hike); 
				}
			}
		}
		
		return foundHikes;
	}
	
	private void setCorsHeaders(HttpServletResponse resp) {
		resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
		resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
	}
	
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		setCorsHeaders(resp);
		resp.setStatus(HttpServletResponse.SC_OK);
	}
}
