package DAO;

import auth.DatabaseConnection;
import Models.Booking;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BookingDAO {
    public static void insertBooking(Booking booking) {
        String query = "INSERT INTO booking (UserID, BookingDate, StartTime, EndTime, BookingType, PaymentStatus) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, booking.getUserID());
            stmt.setString(2, booking.getBookingDate());
            stmt.setString(3, booking.getStartTime());
            stmt.setString(4, booking.getEndTime());
            stmt.setString(5, booking.getBookingType());
            stmt.setString(6, booking.getPaymentStatus());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
