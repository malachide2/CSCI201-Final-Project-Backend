package database;

import java.sql.*;

/**
 * DAO for hikes.
 * Uses the shared DBConnector (same pattern as other DAOs in the project).
 */
public class HikeDao {

    /**
     * Case-insensitive existence check for hike name.
     */
    public boolean existsByNameIgnoreCase(String name) throws SQLException {
        String sql = "SELECT COUNT(*) as cnt FROM hikes WHERE LOWER(name) = LOWER(?)";
        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            }
        }
        return false;
    }

    /**
     * Insert a hike and return generated hike_id.
     * Caller may manage transactions (commit/rollback) on the shared connection.
     */
    public int createHike(
            String name,
            String locationText,
            Double difficulty,
            Double distance,
            Integer elevation,
            Integer createdBy,
            Double latitude,
            Double longitude
    ) throws SQLException {
        String sql = "INSERT INTO hikes (name, location_text, difficulty, distance, elevation, created_by, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            if (locationText != null) stmt.setString(2, locationText); else stmt.setNull(2, Types.VARCHAR);
            if (difficulty != null) stmt.setDouble(3, difficulty); else stmt.setNull(3, Types.DOUBLE);
            if (distance != null) stmt.setDouble(4, distance); else stmt.setNull(4, Types.DOUBLE);
            if (elevation != null) stmt.setInt(5, elevation); else stmt.setNull(5, Types.INTEGER);
            if (createdBy != null) stmt.setInt(6, createdBy); else stmt.setNull(6, Types.INTEGER);
            if (latitude != null) stmt.setDouble(7, latitude); else stmt.setNull(7, Types.DOUBLE);
            if (longitude != null) stmt.setDouble(8, longitude); else stmt.setNull(8, Types.DOUBLE);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Creating hike failed, no rows affected.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                } else {
                    throw new SQLException("Creating hike failed, no ID obtained.");
                }
            }
        }
    }
}
