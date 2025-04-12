package DAO;

import auth.DatabaseConnection;
import Models.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) class for handling booking-related database operations.
 * This class provides methods to interact with the booking table in the database,
 * including inserting new bookings with proper transaction management and error handling.
 */
public class BookingDAO {
    /**
     * Inserts a new booking into the database.
     * This method handles the entire booking insertion process, including:
     * - Transaction management (begin, commit, rollback)
     * - Parameter binding for the prepared statement
     * - Error handling and connection cleanup
     * 
     * @param booking The Booking object containing the booking details to be inserted
     * @return true if the booking was successfully inserted, false otherwise
     */
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
