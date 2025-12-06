package servlets;

import database.DBConnector;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/api/hikes/*")
public class HikeDetailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final Gson gson = new Gson();
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
        
		// Add CORS headers
		setCorsHeaders(response);
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		try {
			// Extract hike ID from path
			String pathInfo = request.getPathInfo();
			if (pathInfo == null || pathInfo.equals("/")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write(gson.toJson(new ErrorResponse("Missing hike ID")));
				return;
			}
			
			// Remove leading slash and parse ID
			String idStr = pathInfo.substring(1);
			int hikeId;
			try {
				hikeId = Integer.parseInt(idStr);
			} catch (NumberFormatException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write(gson.toJson(new ErrorResponse("Invalid hike ID format")));
				return;
			}
			
			System.out.println("HikeDetailServlet: Fetching hike with ID: " + hikeId);
			
			// Fetch hike details
			HikeDetail hikeDetail = getHikeById(hikeId);
			
			if (hikeDetail == null) {
				System.out.println("HikeDetailServlet: Hike not found for ID: " + hikeId);
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().write(gson.toJson(new ErrorResponse("Hike not found")));
				return;
			}
			
			System.out.println("HikeDetailServlet: Successfully fetched hike: " + hikeDetail.name);
			
			// Return hike details
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().write(gson.toJson(hikeDetail));
			
		} catch(SQLException e){
            e.printStackTrace();
            System.out.println("HikeDetailServlet: SQLException - " + e.getMessage());
            log("Database error: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
            response.getWriter().write(gson.toJson(new ErrorResponse("Database error: " + e.getMessage())));
		} catch(Exception e) {
            e.printStackTrace();
            System.out.println("HikeDetailServlet: Exception - " + e.getMessage());
            log("Server error: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
            response.getWriter().write(gson.toJson(new ErrorResponse("An unexpected server error occurred: " + e.getMessage())));
		}
	}
	
	private HikeDetail getHikeById(int hikeId) throws SQLException {
		// First, get the basic hike info
		String sql = "SELECT h.hike_id, h.name, h.location_text, h.distance, h.difficulty, " +
					"h.elevation, h.created_by, h.created_at, h.description, " +
					"u.username AS created_by_username " +
					"FROM hikes h " +
					"LEFT JOIN users u ON h.created_by = u.user_id " +
					"WHERE h.hike_id = ?";
		
		// Separate query for ratings to avoid GROUP BY issues
		String ratingSql = "SELECT COALESCE(AVG(rating), 0.0) AS average_rating, " +
						  "COUNT(*) AS total_ratings " +
						  "FROM reviews WHERE hike_id = ?";
		
		System.out.println("HikeDetailServlet: Executing query for hikeId: " + hikeId);
		
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnection();
			if (conn == null || conn.isClosed()) {
				throw new SQLException("Database connection is null or closed");
			}
			
			// Get basic hike info
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setInt(1, hikeId);
				
				try (ResultSet rs = pstmt.executeQuery()) {
					if (rs.next()) {
						System.out.println("HikeDetailServlet: Found hike in database");
						
						// Get all photos for this hike using the same connection
						List<String> images = getHikePhotos(hikeId, conn);
						
						// Map difficulty number to string - handle null difficulty
						double difficultyNum = 2.0; // default
						try {
							difficultyNum = rs.getDouble("difficulty");
							if (rs.wasNull()) {
								difficultyNum = 2.0;
							}
						} catch (SQLException e) {
							System.out.println("HikeDetailServlet: Error reading difficulty, using default");
							difficultyNum = 2.0;
						}
						
						// Map difficulty number to string
						// Backend stores: Easy=1.0, Moderate=2.5, Hard=4.0, Expert=5.0
						String difficultyStr = "Moderate";
						if (difficultyNum <= 1.5) difficultyStr = "Easy";        // 1.0
						else if (difficultyNum <= 3.0) difficultyStr = "Moderate"; // 2.5
						else if (difficultyNum <= 4.5) difficultyStr = "Hard";    // 4.0
						else difficultyStr = "Expert";                             // 5.0
						
						// Properly handle nullable elevation
						Integer elevation = null;
						try {
							int elev = rs.getInt("elevation");
							if (!rs.wasNull()) {
								elevation = elev;
							}
						} catch (SQLException e) {
							System.out.println("HikeDetailServlet: Error reading elevation");
						}
						
						// Properly handle nullable timestamp
						String createdAt = null;
						try {
							java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
							if (timestamp != null) {
								createdAt = timestamp.toString();
							}
						} catch (SQLException e) {
							System.out.println("HikeDetailServlet: Error reading created_at");
						}
						
						// Properly handle nullable created_by
						Integer createdBy = null;
						try {
							int cb = rs.getInt("created_by");
							if (!rs.wasNull()) {
								createdBy = cb;
							}
						} catch (SQLException e) {
							System.out.println("HikeDetailServlet: Error reading created_by");
						}
						
						String createdByUsername = "";
						try {
							String username = rs.getString("created_by_username");
							if (username != null) {
								createdByUsername = username;
							}
						} catch (SQLException e) {
							System.out.println("HikeDetailServlet: Error reading created_by_username");
						}
						
						// Get name and location_text with null handling
						String name = "";
						try {
							String n = rs.getString("name");
							if (n != null) {
								name = n;
							}
						} catch (SQLException e) {
							System.out.println("HikeDetailServlet: Error reading name");
						}
						
						String locationText = "";
						try {
							String loc = rs.getString("location_text");
							if (loc != null) {
								locationText = loc;
							}
						} catch (SQLException e) {
							System.out.println("HikeDetailServlet: Error reading location_text");
						}
						
						// Get distance with null handling
						double distance = 0.0;
						try {
							distance = rs.getDouble("distance");
							if (rs.wasNull()) {
								distance = 0.0;
							}
						} catch (SQLException e) {
							System.out.println("HikeDetailServlet: Error reading distance");
						}
						
						// Get hike_id
						int hikeIdValue = 0;
						try {
							hikeIdValue = rs.getInt("hike_id");
						} catch (SQLException e) {
							System.out.println("HikeDetailServlet: Error reading hike_id");
							hikeIdValue = hikeId; // fallback to parameter
						}
						
						// Get ratings separately to avoid GROUP BY issues
						double averageRating = 0.0;
						int totalRatings = 0;
						try (PreparedStatement ratingStmt = conn.prepareStatement(ratingSql)) {
							ratingStmt.setInt(1, hikeId);
							try (ResultSet ratingRs = ratingStmt.executeQuery()) {
								if (ratingRs.next()) {
									averageRating = ratingRs.getDouble("average_rating");
									if (ratingRs.wasNull()) {
										averageRating = 0.0;
									}
									totalRatings = ratingRs.getInt("total_ratings");
									if (ratingRs.wasNull()) {
										totalRatings = 0;
									}
								}
							}
						} catch (SQLException e) {
							System.out.println("HikeDetailServlet: Error fetching ratings: " + e.getMessage());
							// Continue with default values
						}
						
						// Get description from database
						String description = "";
						try {
							String desc = rs.getString("description");
							if (desc != null && !desc.isBlank()) {
								description = desc;
							}
						} catch (SQLException e) {
							System.out.println("HikeDetailServlet: Error reading description (column may not exist yet)");
							// If column doesn't exist, description will remain empty
						}
						
						return new HikeDetail(
							hikeIdValue,
							name,
							locationText,
							difficultyStr,
							distance,
							elevation,
							images,
							averageRating,
							totalRatings,
							createdByUsername,
							createdBy != null ? createdBy : 0,
							createdAt,
							description
						);
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("HikeDetailServlet: SQLException in getHikeById: " + e.getMessage());
			e.printStackTrace();
			throw e; // Re-throw to be handled by caller
		} finally {
			// Don't close the shared connection - it's managed by DBConnector
			// The connection will be reused for other requests
		}
		
		return null;
	}
	
	private List<String> getHikePhotos(int hikeId, Connection conn) throws SQLException {
		List<String> photos = new ArrayList<>();
		String sql = "SELECT image_url FROM photos WHERE hike_id = ? ORDER BY created_at ASC";
		
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, hikeId);
			
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					String imageUrl = rs.getString("image_url");
					if (imageUrl != null && !imageUrl.isEmpty()) {
						photos.add(imageUrl);
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("HikeDetailServlet: Error fetching photos: " + e.getMessage());
			// Return empty list instead of throwing - photos are optional
		}
		
		return photos;
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
	
	// Utility class for standard error response JSON structure
	private static class ErrorResponse {
        String error;
        ErrorResponse(String error) { this.error = error; }
    }
	
	// Inner class for hike detail response
	private static class HikeDetail {
		private int hike_id;
		private String name;
		private String location_text;
		private String difficulty;
		private double distance;
		private Integer elevation;
		private List<String> images;
		private double average_rating;
		private int total_ratings;
		private String created_by_username;
		private int created_by;
		private String created_at;
		private String description; // Added for frontend compatibility
		
		public HikeDetail(int hike_id, String name, String location_text, String difficulty,
						 double distance, Integer elevation, List<String> images,
						 double average_rating, int total_ratings, String created_by_username,
						 int created_by, String created_at, String description) {
			this.hike_id = hike_id;
			this.name = name;
			this.location_text = location_text;
			this.difficulty = difficulty;
			this.distance = distance;
			this.elevation = elevation;
			this.images = images;
			this.average_rating = average_rating;
			this.total_ratings = total_ratings;
			this.created_by_username = created_by_username;
			this.created_by = created_by;
			this.created_at = created_at;
			this.description = description != null ? description : ""; // Default to empty string
		}
	}
}

