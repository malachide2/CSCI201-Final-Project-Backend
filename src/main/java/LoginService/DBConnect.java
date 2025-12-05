package LoginService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DBConnect {

    private String url = "jdbc:mysql://localhost:3306/your_database_name";
    private String username = "root";
    private String password = "root";

    public DBConnect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public String getPasswordHash(String email) {
        String query = "SELECT password FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getUserId(String email) {
        String query = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean emailExists(String email) {
        String query = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean storeSecurityCode(String email, String code, long expiryMillis) {
        String query = "INSERT INTO verification_codes (email, code, expiry_time) VALUES (?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE code = ?, expiry_time = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            Timestamp expiry = new Timestamp(expiryMillis);

            stmt.setString(1, email);
            stmt.setString(2, code);
            stmt.setTimestamp(3, expiry);
            stmt.setString(4, code);
            stmt.setTimestamp(5, expiry);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}