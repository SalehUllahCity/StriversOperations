package Main;

import DAO.*;
import Models.*;

public class Main {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        BookingDAO bookingDAO = new BookingDAO();
        EventDAO eventDAO = new EventDAO();
        PaymentDAO paymentDAO = new PaymentDAO();
        ReviewDAO reviewDAO = new ReviewDAO();

        // Insert Users
        userDAO.insertUser(new User(1, "Alice", "Smith", "alice@example.com", "123456789", "Customer", "password1"));
        userDAO.insertUser(new User(2, "Bob", "Johnson", "bob@example.com", "987654321", "Admin", "password2"));
        userDAO.insertUser(new User(3, "Charlie", "Brown", "charlie@example.com", "111222333", "Customer", "password3"));

        // Insert Bookings
        bookingDAO.insertBooking(new Booking(1, 1, "2025-04-01", "10:00:00", "12:00:00", "Conference", "Paid"));
        bookingDAO.insertBooking(new Booking(2, 2, "2025-04-02", "14:00:00", "16:00:00", "Meeting", "Pending"));
        bookingDAO.insertBooking(new Booking(3, 3, "2025-04-03", "18:30:00", "20:30:00", "Workshop", "Unpaid"));

        // Insert Events
        eventDAO.insertEvent(new Event(1, "Music Concert", "2025-05-10", "Main Hall", 500));
        eventDAO.insertEvent(new Event(2, "Comedy Show", "2025-06-15", "Auditorium", 300));
        eventDAO.insertEvent(new Event(3, "Theatre Play", "2025-07-20", "Theatre Room", 200));

        // Insert Payments
        paymentDAO.insertPayment(new Payment(1, 1, 150.00, "2025-04-01", "Credit Card"));
        paymentDAO.insertPayment(new Payment(2, 2, 200.00, "2025-04-02", "PayPal"));
        paymentDAO.insertPayment(new Payment(3, 3, 100.00, "2025-04-03", "Bank Transfer"));

        // Insert Reviews
        reviewDAO.insertReview(new Review(1, 1, 1, 5, "Amazing event! Loved it."));
        reviewDAO.insertReview(new Review(2, 2, 2, 4, "Great comedy night."));
        reviewDAO.insertReview(new Review(3, 3, 3, 3, "It was okay, but could be better."));

        System.out.println("✅ Sample data inserted successfully.");
    }
}
