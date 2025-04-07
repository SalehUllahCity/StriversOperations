package DAO;

import auth.DatabaseConnection;
import Models.Payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PaymentDAO {
    public static boolean insertPayment(Payment payment) {
        String query = "INSERT INTO payments (BookingID, Amount, PaymentDate, PaymentMethod) VALUES (?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, payment.getBookingID());
                stmt.setDouble(2, payment.getAmount());
                stmt.setString(3, payment.getPaymentDate());
                stmt.setString(4, payment.getPaymentMethod());
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
