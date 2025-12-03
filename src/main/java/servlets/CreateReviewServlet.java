package servlets;

import database.DBConnector;
import database.ReviewDao;
import database.ReviewResponse;
import database.ReviewListResponse;

import java.io.IOException;
import java.sql.SQLException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

@WebServlet("/api/reviews")
public class CreateReviewServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final ReviewDao reviewDao = new ReviewDao();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json");

        try {
            // Get hikeId from query parameter
            String hikeIdParam = req.getParameter("hikeId");
            if (hikeIdParam == null || hikeIdParam.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "Missing required parameter: hikeId");
                resp.getWriter().write(gson.toJson(errorResponse));
                return;
            }

            int hikeId;
            try {
                hikeId = Integer.parseInt(hikeIdParam);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "Invalid hikeId format");
                resp.getWriter().write(gson.toJson(errorResponse));
                return;
            }

            // Get current user ID (may be null if not authenticated)
            Integer currentUserId = (Integer) req.getAttribute("userId");

            // Fetch reviews
            var reviews = reviewDao.getReviewsForHike(hikeId, currentUserId);
            
            // Fetch average rating
            double averageRating = reviewDao.getAverageRating(hikeId);
            
            // Fetch total reviews count
            int totalReviews = reviewDao.getTotalReviews(hikeId);

            // Create response
            ReviewListResponse response = new ReviewListResponse(
                hikeId, 
                averageRating, 
                totalReviews, 
                reviews
            );

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(response));

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Database error: " + e.getMessage());
            resp.getWriter().write(gson.toJson(errorResponse));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Internal server error: " + e.getMessage());
            resp.getWriter().write(gson.toJson(errorResponse));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json");

        // Check authentication
        Integer userId = (Integer) req.getAttribute("userId");
        
        if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Not authenticated");
            resp.getWriter().write(gson.toJson(errorResponse));
            return;
        }

        try {
            // Read JSON request body
            JsonObject requestJson = gson.fromJson(req.getReader(), JsonObject.class);
            
            if (requestJson == null || !requestJson.has("hikeId") || 
                !requestJson.has("rating") || !requestJson.has("comment")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "Missing required fields: hikeId, rating, comment");
                resp.getWriter().write(gson.toJson(errorResponse));
                return;
            }

            int hikeId = requestJson.get("hikeId").getAsInt();
            double rating = requestJson.get("rating").getAsDouble();
            String comment = requestJson.get("comment").getAsString();

            // Validate rating (1-5, 0.5 increments)
            if (rating < 1.0 || rating > 5.0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "Rating must be between 1.0 and 5.0");
                resp.getWriter().write(gson.toJson(errorResponse));
                return;
            }

            // Check if rating is in 0.5 increments
            double remainder = (rating * 2) % 1;
            if (remainder != 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "Rating must be in 0.5 increments");
                resp.getWriter().write(gson.toJson(errorResponse));
                return;
            }

            // Create review
            int reviewId = reviewDao.createReview(hikeId, userId, rating, comment);

            // Fetch the created review to return
            var reviews = reviewDao.getReviewsForHike(hikeId, userId);
            ReviewResponse createdReview = reviews.stream()
                .filter(r -> r.getId() == reviewId)
                .findFirst()
                .orElse(null);

            if (createdReview == null) {
                // If we can't find it, create a minimal response
                createdReview = new ReviewResponse();
                createdReview.setId(reviewId);
                createdReview.setHikeId(hikeId);
                createdReview.setUserId(userId);
                createdReview.setRating(rating);
                createdReview.setComment(comment);
                createdReview.setUpvotes(0);
                createdReview.setCreatedAt(java.time.LocalDateTime.now().toString());
                createdReview.setUpvotedByCurrentUser(false);
            }

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(gson.toJson(createdReview));

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Database error: " + e.getMessage());
            resp.getWriter().write(gson.toJson(errorResponse));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Internal server error: " + e.getMessage());
            resp.getWriter().write(gson.toJson(errorResponse));
        }
    }

    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }
}

