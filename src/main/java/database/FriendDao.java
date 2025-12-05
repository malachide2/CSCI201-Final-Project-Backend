package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendDao {

    public boolean addFriend(int followerId, int followedId) throws SQLException {
        if (isFollowing(followerId, followedId)) {
            return false;
        }

        String sql = "INSERT INTO friends (follower_id, followed_id) VALUES (?, ?)";
        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        }
    }

    public boolean removeFriend(int followerId, int followedId) throws SQLException {
        String sql = "DELETE FROM friends WHERE follower_id = ? AND followed_id = ?";
        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        }
    }

    public boolean isFollowing(int followerId, int followedId) throws SQLException {
        String sql = "SELECT COUNT(*) as cnt FROM friends WHERE follower_id = ? AND followed_id = ?";
        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            }
        }
        return false;
    }

    public List<FriendResponse> getFollowing(int userId) throws SQLException {
        List<FriendResponse> friends = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.email, f.created_at as friends_since " +
                     "FROM friends f " +
                     "INNER JOIN users u ON f.followed_id = u.user_id " +
                     "WHERE f.follower_id = ? " +
                     "ORDER BY f.created_at DESC";

        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FriendResponse friend = new FriendResponse(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getTimestamp("friends_since").toLocalDateTime().toString()
                    );
                    friends.add(friend);
                }
            }
        }
        return friends;
    }

    public List<FriendResponse> getFollowers(int userId) throws SQLException {
        List<FriendResponse> followers = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.email, f.created_at as friends_since " +
                     "FROM friends f " +
                     "INNER JOIN users u ON f.follower_id = u.user_id " +
                     "WHERE f.followed_id = ? " +
                     "ORDER BY f.created_at DESC";

        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FriendResponse follower = new FriendResponse(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getTimestamp("friends_since").toLocalDateTime().toString()
                    );
                    followers.add(follower);
                }
            }
        }
        return followers;
    }

    public int getFollowingCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) as cnt FROM friends WHERE follower_id = ?";
        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt");
                }
            }
        }
        return 0;
    }

    public int getFollowersCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) as cnt FROM friends WHERE followed_id = ?";
        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt");
                }
            }
        }
        return 0;
    }

    public int findUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }
        return -1;
    }

    public boolean userExists(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) as cnt FROM users WHERE user_id = ?";
        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            }
        }
        return false;
    }

    public List<ActivityItemResponse> getFriendActivity(int requestingUserId, int friendUserId, int limit) throws SQLException {
        if (!isFollowing(requestingUserId, friendUserId)) {
            return null;
        }

        List<ActivityItemResponse> activities = new ArrayList<>();
        String sql = "SELECT r.review_id, r.hike_id, h.name as hike_name, r.rating, " +
                     "r.review_body, r.created_at, u.username " +
                     "FROM reviews r " +
                     "INNER JOIN hikes h ON r.hike_id = h.hike_id " +
                     "INNER JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.user_id = ? " +
                     "ORDER BY r.created_at DESC " +
                     "LIMIT ?";

        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, friendUserId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ActivityItemResponse activity = new ActivityItemResponse(
                        "review",
                        rs.getInt("review_id"),
                        rs.getInt("hike_id"),
                        rs.getString("hike_name"),
                        rs.getDouble("rating"),
                        rs.getString("review_body"),
                        rs.getTimestamp("created_at").toLocalDateTime().toString(),
                        rs.getString("username")
                    );
                    activities.add(activity);
                }
            }
        }
        return activities;
    }

    public FriendResponse getUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, email, created_at FROM users WHERE user_id = ?";
        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new FriendResponse(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getTimestamp("created_at").toLocalDateTime().toString()
                    );
                }
            }
        }
        return null;
    }
}