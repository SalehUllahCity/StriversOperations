package auth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
    private static final String USER = "in2033t26_a";  // change to DDL
    private static final String PASSWORD = "jLxOPuQ69Mg";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean authenticateUser(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE Email = ? AND Password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();  // True if user exists
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
