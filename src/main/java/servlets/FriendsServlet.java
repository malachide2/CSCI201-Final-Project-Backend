package servlets;

import database.FriendDao;
import database.FriendResponse;
import database.FriendsListResponse;

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

@WebServlet("/api/friends")
public class FriendsServlet extends HttpServlet {
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
            List<FriendResponse> friends = friendDao.getFollowing(userId);
            int totalFriends = friendDao.getFollowingCount(userId);
            int totalFollowers = friendDao.getFollowersCount(userId);

            FriendsListResponse response = new FriendsListResponse(
                userId, 
                totalFriends, 
                totalFollowers, 
                friends
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json");

        Integer userId = (Integer) req.getAttribute("userId");
        if (userId == null) {
            userId = 1;
        }

        try {
            JsonObject requestJson = gson.fromJson(req.getReader(), JsonObject.class);
            
            if (requestJson == null || !requestJson.has("username")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeError(resp, "Missing required field: username");
                return;
            }

            String username = requestJson.get("username").getAsString().trim();
            
            if (username.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeError(resp, "Username cannot be empty");
                return;
            }

            int targetUserId = friendDao.findUserIdByUsername(username);
            
            if (targetUserId == -1) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeError(resp, "User not found: " + username);
                return;
            }

            if (targetUserId == userId) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeError(resp, "You cannot follow yourself");
                return;
            }

            if (friendDao.isFollowing(userId, targetUserId)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                writeError(resp, "You are already following this user");
                return;
            }

            boolean success = friendDao.addFriend(userId, targetUserId);
            
            if (success) {
                FriendResponse friend = friendDao.getUserById(targetUserId);
                
                JsonObject response = new JsonObject();
                response.addProperty("status", "success");
                response.addProperty("message", "Now following " + username);
                response.add("friend", gson.toJsonTree(friend));

                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write(gson.toJson(response));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writeError(resp, "Failed to add friend");
            }

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

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) 
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

            if (!friendDao.userExists(friendUserId)) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeError(resp, "User not found");
                return;
            }

            if (!friendDao.isFollowing(userId, friendUserId)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeError(resp, "You are not following this user");
                return;
            }

            boolean success = friendDao.removeFriend(userId, friendUserId);
            
            if (success) {
                JsonObject response = new JsonObject();
                response.addProperty("status", "success");
                response.addProperty("message", "Successfully unfollowed user");
                response.addProperty("unfollowedUserId", friendUserId);

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(response));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writeError(resp, "Failed to remove friend");
            }

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
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
    }
}