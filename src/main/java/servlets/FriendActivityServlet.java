package servlets;

import database.FriendDao;
import database.FriendResponse;
import database.ActivityItemResponse;
import database.FriendActivityResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

@WebServlet("/api/friends/activity")
public class FriendActivityServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final FriendDao friendDao = new FriendDao();
    private static final int DEFAULT_LIMIT = 20;

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

        Integer userId = (Integer) req.getAttribute("userId");
        if (userId == null) {
            userId = 1;
        }

        try {
            String friendUserIdParam = req.getParameter("friendUserId");
            
            if (friendUserIdParam == null || friendUserIdParam.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeError(resp, "Missing required parameter: friendUserId");
                return;
            }

            int friendUserId;
            try {
                friendUserId = Integer.parseInt(friendUserIdParam);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeError(resp, "Invalid friendUserId format");
                return;
            }

            int limit = DEFAULT_LIMIT;
            String limitParam = req.getParameter("limit");
            if (limitParam != null && !limitParam.isEmpty()) {
                try {
                    limit = Math.min(Integer.parseInt(limitParam), 100);
                } catch (NumberFormatException ignored) {
                }
            }

            FriendResponse friend = friendDao.getUserById(friendUserId);
            if (friend == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeError(resp, "User not found");
                return;
            }

            List<ActivityItemResponse> activities = friendDao.getFriendActivity(userId, friendUserId, limit);
            
            if (activities == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                writeError(resp, "You must be following this user to view their activity");
                return;
            }

            FriendActivityResponse response = new FriendActivityResponse(
                friendUserId,
                friend.getUsername(),
                activities.size(),
                activities
            );

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(response));

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeError(resp, "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeError(resp, "Internal server error: " + e.getMessage());
        }
    }

    private void writeError(HttpServletResponse resp, String msg) throws IOException {
        JsonObject err = new JsonObject();
        err.addProperty("error", msg);
        resp.getWriter().write(gson.toJson(err));
    }

    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
    }
}