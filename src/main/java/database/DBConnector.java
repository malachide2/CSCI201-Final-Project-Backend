package database;
import java.sql.*;

public class DBConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME";
    private static final String USER = "root";
    private static final String PASSWORD = "YOUR_PASSWORD_HERE";
    
    private static DBConnector instance;
    private Connection conn;

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            printClassStudentCounts(conn);
            System.out.println();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private DBConnector() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to database.");
            e.printStackTrace();
        }
    }

    // -----------------------------
    // Singleton access
    // -----------------------------
    public static DBConnector getInstance() {
        if (instance == null) {
            synchronized (DBConnector.class) {
                if (instance == null) {
                    instance = new DBConnector();
                }
            }
        }
        return instance;
    }
    
    // -----------------------------
    // Get database connection
    // -----------------------------
    public Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return conn;
    }
    
    // -----------------------------
    // Method 1: ClassName → # of Students
    // -----------------------------
    public static void printClassStudentCounts(Connection conn) {
        String query = """
            SELECT ClassName, COUNT(DISTINCT SID) AS NumberOfStudents
            FROM Grades
            GROUP BY ClassName;
        """;

        System.out.println("ClassName\tNumber of Students");
        System.out.println("---------------------------------");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String className = rs.getString("ClassName");
                int count = rs.getInt("NumberOfStudents");
                System.out.printf("%-10s\t%d%n", className, count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
