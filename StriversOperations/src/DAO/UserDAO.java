package DAO;

import auth.DatabaseConnection;
import Models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) class for handling user-related database operations.
 * This class provides methods to interact with the users table in the database,
 * including inserting new user records with proper transaction management and error handling.
 */
public class UserDAO {
    /**
     * Inserts a new user record into the database.
     * This method handles the entire user insertion process, including:
     * - Transaction management (begin, commit, rollback)
     * - Parameter binding for the prepared statement
     * - Error handling and connection cleanup
     * 
     * @param user The User object containing the user details to be inserted,
     *            including name, email, phone, role, and password
     * @return true if the user was successfully registered, false otherwise
     */
    public static boolean insertUser(User user) {
        String query = "INSERT INTO users (Name, Email, Phone, Role, Password) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, user.getName());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, user.getPhone());
                stmt.setString(4, user.getRole());
                stmt.setString(5, user.getPassword());
                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
