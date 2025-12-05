package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO for photos table.
 */
public class PhotoDao {

    /**
     * Insert a photo record linked to a hike and user.
     * imageUrl should be a server-relative path (e.g., "/images/full/abc.jpg")
     */
    public void insertPhoto(int hikeId, int userId, String imageUrl, String caption) throws SQLException {
        String sql = "INSERT INTO photos (hike_id, user_id, image_url, caption) VALUES (?, ?, ?, ?)";
        Connection conn = DBConnector.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, hikeId);
            stmt.setInt(2, userId);
            stmt.setString(3, imageUrl);
            if (caption != null) stmt.setString(4, caption); else stmt.setNull(4, java.sql.Types.VARCHAR);
            stmt.executeUpdate();
        }
    }
}
