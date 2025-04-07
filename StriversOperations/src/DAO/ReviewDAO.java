package DAO;

import auth.DatabaseConnection;
import Models.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReviewDAO {
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
