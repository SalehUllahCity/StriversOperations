package DAO;

import auth.DatabaseConnection;
import Models.Review;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReviewDAO {
    public static void insertReview(Review review) {
        String query = "INSERT INTO reviews (UserID, bookingID, Rating, Comments, Response) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, review.getUserID());
            stmt.setInt(2, review.getBookingID());
            stmt.setInt(3, review.getRating());
            stmt.setString(4, review.getComments());
            stmt.setString(5, review.getResponse());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
