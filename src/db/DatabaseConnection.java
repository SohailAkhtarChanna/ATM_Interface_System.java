package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DatabaseConnection {
    // Configuration parameters
    private static final String URL = "jdbc:mysql://localhost:3306/atm";

    private static final String USER = "root"; // Database username
    private static final String PASSWORD = "1234"; // Database password

    // Additional connection parameters for better reliability
    private static final String CONNECTION_PARAMS =
            "?useSSL=false" +
                    "&autoReconnect=true" +
                    "&useUnicode=true" +
                    "&characterEncoding=UTF-8" +
                    "&serverTimezone=UTC";

    static {
        try {
            // Explicitly load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver"); // Ensure the JDBC driver is loaded
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "MySQL JDBC Driver not found!\n" + e.getMessage(),
                    "Driver Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            // Establish and return the connection
            Connection conn = DriverManager.getConnection(URL + CONNECTION_PARAMS, USER, PASSWORD);
            return conn;
        } catch (SQLException e) {
            // Show user-friendly error message
            JOptionPane.showMessageDialog(null,
                    "Database Connection Failed!\n" +
                            "Error: " + e.getMessage() + "\n\n" +
                            "Please ensure:\n" +
                            "1. MySQL server is running\n" +
                            "2. Database 'atm_system' exists\n" +
                            "3. Username/password are correct",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);

            // Re-throw the exception for the calling code to handle
            throw new SQLException("Failed to connect to database", e);
        }
    }

    // Helper method to close connection quietly
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
