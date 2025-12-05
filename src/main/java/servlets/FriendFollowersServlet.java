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

@WebServlet("/api/friends/followers")
public class FriendFollowersServlet extends HttpServlet {
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
            List<FriendResponse> followers = friendDao.getFollowers(userId);
            int totalFriends = friendDao.getFollowingCount(userId);
            int totalFollowers = friendDao.getFollowersCount(userId);

            FriendsListResponse response = new FriendsListResponse(
                userId, 
                totalFriends, 
                totalFollowers, 
                followers
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