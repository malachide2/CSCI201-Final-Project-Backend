package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao {

    public int createReview(int hikeId, int userId, double rating, String comment) throws SQLException {
        String sql = "INSERT INTO reviews (hike_id, user_id, rating, review_body) VALUES (?, ?, ?, ?)";

        // Do NOT close the shared connection – only close the statement/result set
        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, hikeId);
            stmt.setInt(2, userId);
            stmt.setDouble(3, rating);
            stmt.setString(4, comment);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating review failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating review failed, no ID obtained.");
                }
            }
        }
    }

    public List<ReviewResponse> getReviewsForHike(int hikeId, Integer currentUserId) throws SQLException {
        List<ReviewResponse> reviews = new ArrayList<>();

        String sql = """
            SELECT r.review_id, r.hike_id, r.user_id, u.username, r.rating, r.review_body,
                   r.upvotes_count, r.created_at
            FROM reviews r
            INNER JOIN users u ON r.user_id = u.user_id
            WHERE r.hike_id = ?
            ORDER BY r.created_at DESC
        """;

        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, hikeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int reviewId = rs.getInt("review_id");
                    int userId = rs.getInt("user_id");
                    String username = rs.getString("username");
                    double rating = rs.getDouble("rating");
                    String comment = rs.getString("review_body");
                    int upvotes = rs.getInt("upvotes_count");
                    Timestamp createdAt = rs.getTimestamp("created_at");

                    boolean upvotedByCurrentUser = false;
                    if (currentUserId != null) {
                        upvotedByCurrentUser = hasUserUpvoted(reviewId, currentUserId);
                    }

                    String createdAtStr = (createdAt != null)
                            ? createdAt.toLocalDateTime().toString()
                            : java.time.LocalDateTime.now().toString();

                    ReviewResponse review = new ReviewResponse(
                            reviewId, hikeId, userId, username, rating, comment,
                            upvotes, createdAtStr, upvotedByCurrentUser
                    );

                    reviews.add(review);
                }
            }
        }

        return reviews;
    }

    public double getAverageRating(int hikeId) throws SQLException {
        String sql = "SELECT AVG(rating) as avg_rating FROM reviews WHERE hike_id = ?";

        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, hikeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble("avg_rating");
                    return rs.wasNull() ? 0.0 : avg;
                }
            }
        }

        return 0.0;
    }

    public int getTotalReviews(int hikeId) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM reviews WHERE hike_id = ?";

        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, hikeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }

        return 0;
    }

    public boolean hasUserUpvoted(int reviewId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM review_upvotes WHERE review_id = ? AND user_id = ?";

        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reviewId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }

        return false;
    }

    public int toggleUpvote(int reviewId, int userId) throws SQLException {
        Connection conn = DBConnector.getInstance().getConnection();

        try {
            conn.setAutoCommit(false);

            // Check if upvote exists (this uses the same shared connection, but does not close it)
            boolean exists = hasUserUpvoted(reviewId, userId);

            if (exists) {
                // Remove upvote
                String deleteSql = "DELETE FROM review_upvotes WHERE review_id = ? AND user_id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, reviewId);
                    deleteStmt.setInt(2, userId);
                    deleteStmt.executeUpdate();
                }

                // Decrement upvotes_count
                String updateSql = "UPDATE reviews SET upvotes_count = upvotes_count - 1 WHERE review_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, reviewId);
                    updateStmt.executeUpdate();
                }
            } else {
                // Add upvote
                String insertSql = "INSERT INTO review_upvotes (review_id, user_id) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, reviewId);
                    insertStmt.setInt(2, userId);
                    insertStmt.executeUpdate();
                }

                // Increment upvotes_count
                String updateSql = "UPDATE reviews SET upvotes_count = upvotes_count + 1 WHERE review_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, reviewId);
                    updateStmt.executeUpdate();
                }
            }

            conn.commit();

            // Get updated upvotes count
            String countSql = "SELECT upvotes_count FROM reviews WHERE review_id = ?";
            try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                countStmt.setInt(1, reviewId);
                try (ResultSet rs = countStmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("upvotes_count");
                    }
                }
            }

            return 0;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
