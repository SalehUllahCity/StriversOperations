package DAO;

import auth.DatabaseConnection;
import Models.Payment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PaymentDAO {
    public static void insertPayment(Payment payment) {
        String query = "INSERT INTO payments (BookingID, Amount, PaymentDate, PaymentMethod) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, payment.getBookingID());
            stmt.setDouble(2, payment.getAmount());
            stmt.setString(3, payment.getPaymentDate());
            stmt.setString(4, payment.getPaymentMethod());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
