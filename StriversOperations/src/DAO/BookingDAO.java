package DAO;

import Models.Booking;
import auth.DatabaseConnection;
import java.sql.*;

public class BookingDAO {
    public boolean addBooking(Booking booking) {
        String sql = "INSERT INTO booking (UserID, BookingDate, StartTime, EndTime, BookingType, PaymentStatus) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, booking.getUserID());
            stmt.setDate(2, booking.getBookingDate());
            stmt.setTime(3, booking.getStartTime());
            stmt.setTime(4, booking.getEndTime());
            stmt.setString(5, booking.getBookingType());
            stmt.setString(6, booking.getPaymentStatus());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}