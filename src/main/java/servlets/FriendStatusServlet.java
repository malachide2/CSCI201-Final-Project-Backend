package servlets;

import database.FriendDao;
import database.FriendResponse;

import java.io.IOException;
import java.sql.SQLException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

@WebServlet("/api/friends/status")
public class FriendStatusServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final FriendDao friendDao = new FriendDao();

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

            FriendResponse friend = friendDao.getUserById(friendUserId);
            if (friend == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeError(resp, "User not found");
                return;
            }

            boolean isFollowing = friendDao.isFollowing(userId, friendUserId);
            boolean followsBack = friendDao.isFollowing(friendUserId, userId);

            JsonObject response = new JsonObject();
            response.addProperty("currentUserId", userId);
            response.addProperty("targetUserId", friendUserId);
            response.addProperty("targetUsername", friend.getUsername());
            response.addProperty("isFollowing", isFollowing);
            response.addProperty("followsBack", followsBack);

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