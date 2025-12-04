package servlets;

import database.DBConnector;

import java.io.IOException;
import java.sql.SQLException;
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

import database.Hike;


// Need to replace Object with Hike object (where does it get defined??)



@WebServlet("/api/hikes")
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final Gson gson = new Gson();
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		String searchQuery = request.getParameter("q");
		String difficulty = request.getParameter("difficulty");
		String minLengthStr = request.getParameter("min_length");
		String maxLengthStr = request.getParameter("max_length");
		String minRatingStr = request.getParameter("min_rating");
		
		try {
			List<Hike> hikes = executeSearch(searchQuery, difficulty, minLengthStr,
												maxLengthStr, minRatingStr);
			
			String jsonResponse = gson.toJson(hikes);
			response.getWriter().write(jsonResponse);
			
		}
		catch(SQLException e){
			log("Database error: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
            response.getWriter().write(gson.toJson(new ErrorResponse("Database error: " + e.getMessage())));
			
			
		} catch(NumberFormatException e) {
            log("Client error: Invalid number format provided for filter: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); 
            response.getWriter().write(gson.toJson(new ErrorResponse("Filter parameters must be valid numbers.")));
		
		}catch(Exception e) {
			log("Server error: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
            response.getWriter().write(gson.toJson(new ErrorResponse("An unexpected server error occurred.")));
			
		}
		
		
	}
	
	private static class ErrorResponse {
        String error;
        ErrorResponse(String error) { this.error = error; }
    }
	
	
	private List<Hike> executeSearch(String searchQuery, String difficulty, String minLengthStr, 
            String maxLengthStr, String minRatingStr) throws SQLException {

			StringBuilder sql = new StringBuilder();
			List<Object> params = new ArrayList<>();
			List<Hike> foundHikes = new ArrayList<>();


			
			sql.append("SELECT h.hike_id, h.name, h.location_text, h.distance, h.difficulty, ");
			sql.append("COALESCE(AVG(r.rating), 0.0) AS average_rating, ");
			sql.append("(SELECT p.image_url FROM photos p WHERE p.hike_id = h.hike_id ORDER BY p.created_at ASC LIMIT 1) AS thumbnail_url ");
			sql.append("FROM hikes h LEFT JOIN reviews r ON h.hike_id = r.hike_id ");
			sql.append("WHERE 1=1 "); 



			// Add logic for WHERE/HAVING
			// How do we define Difficulty??


			
			sql.append("ORDER BY average_rating DESC, h.created_at DESC ");
			
			
			// 
			try (Connection conn = DBConnector.getInstance().getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql.toString())){
				
				int index = 1;
	            for (Object param : params) {
	                pstmt.setObject(index++, param); 
	            }
	            
	            try (ResultSet rs = pstmt.executeQuery()) {
	                // 6. Map Results to Hike Objects
	                while (rs.next()) {
	                    Hike hike = new Hike(
	                        rs.getInt("hike_id"),
	                        rs.getString("name"),
	                        rs.getString("location_text"),
	                        rs.getDouble("distance"),
	                        rs.getDouble("difficulty"),
	                        rs.getDouble("average_rating"),
	                        rs.getString("thumbnail_url")
	                    );
	                    foundHikes.add(hike); 
	                }
	                
	            }
				
			}
			
			return foundHikes;
			
			
	}
			
	
	
	
}
