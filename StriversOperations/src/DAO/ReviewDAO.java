package DAO;

import auth.DatabaseConnection;
import Models.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) class for handling review-related database operations.
 * This class provides methods to interact with the reviews table in the database,
 * including inserting new review records with proper transaction management and error handling.
 */
public class ReviewDAO {
    /**
     * Inserts a new review record into the database.
     * This method handles the entire review insertion process, including:
     * - Transaction management (begin, commit, rollback)
     * - Parameter binding for the prepared statement
     * - Error handling and connection cleanup
     * 
     * @param review The Review object containing the review details to be inserted,
     *              including user ID, booking ID, rating, comments, and response
     * @return true if the review was successfully recorded, false otherwise
     */
    public static boolean insertReview(Review review) {
        String query = "INSERT INTO reviews (UserID, BookingID, Rating, Comments, Response) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, review.getUserID());
                stmt.setInt(2, review.getBookingID());
                stmt.setInt(3, review.getRating());
                stmt.setString(4, review.getComments());
                stmt.setString(5, review.getResponse());
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
