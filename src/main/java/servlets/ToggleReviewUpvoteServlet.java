package servlets;

import database.DBConnector;
import database.ReviewDao;

import java.io.IOException;
import java.sql.SQLException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

@WebServlet("/api/reviews/upvote")
public class ToggleReviewUpvoteServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final ReviewDao reviewDao = new ReviewDao();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json");

        // Check authentication
        //Integer userId = (Integer) req.getAttribute("userId");
        Integer userId = 1; //TODO: Remove this
        /*if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Not authenticated");
            resp.getWriter().write(gson.toJson(errorResponse));
            return;
        }*/

        try {
            // Read JSON request body
            JsonObject requestJson = gson.fromJson(req.getReader(), JsonObject.class);
            
            if (requestJson == null || !requestJson.has("reviewId")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "Missing required field: reviewId");
                resp.getWriter().write(gson.toJson(errorResponse));
                return;
            }

            int reviewId = requestJson.get("reviewId").getAsInt();

            // Check if user already upvoted before toggle
            boolean wasUpvoted = reviewDao.hasUserUpvoted(reviewId, userId);

            // Toggle upvote
            int newUpvotesCount = reviewDao.toggleUpvote(reviewId, userId);

            // Determine if it's now upvoted (opposite of what it was before)
            boolean isUpvoted = !wasUpvoted;

            // Create response
            JsonObject response = new JsonObject();
            response.addProperty("reviewId", reviewId);
            response.addProperty("upvotes", newUpvotesCount);
            response.addProperty("upvoted", isUpvoted);

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

    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }
}

