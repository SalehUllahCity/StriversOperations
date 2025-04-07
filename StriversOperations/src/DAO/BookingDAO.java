package DAO;

import auth.DatabaseConnection;
import Models.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BookingDAO {
    public static boolean insertBooking(Booking booking) {
        String query = "INSERT INTO booking (UserID, BookingDate, StartTime, EndTime, BookingType, PaymentStatus) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, booking.getUserID());
                stmt.setString(2, booking.getBookingDate());
                stmt.setString(3, booking.getStartTime());
                stmt.setString(4, booking.getEndTime());
                stmt.setString(5, booking.getBookingType());
                stmt.setString(6, booking.getPaymentStatus());
                stmt.executeUpdate();
            }

            conn.commit(); // Commit transaction if successful
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback if something goes wrong
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit back to true
                    conn.close();             // Explicitly close connection
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }
}
