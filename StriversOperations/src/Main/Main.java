package Main;

import DAO.*;
import Models.*;

public class Main {
    /**
     * Main method that initializes the application and inserts sample data.
     * Creates instances of various DAO classes and uses them to insert
     * sample records into the database, demonstrating the functionality
     * of the booking and event management system.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        BookingDAO bookingDAO = new BookingDAO();
        EventDAO eventDAO = new EventDAO();
        PaymentDAO paymentDAO = new PaymentDAO();
        ReviewDAO reviewDAO = new ReviewDAO();

        // Insert Users
        userDAO.insertUser(new User(11, "Brendon Cole", "brendon_C@gmail.com", "045932323", "Customer", "password1"));

        // Insert Bookings
        bookingDAO.insertBooking(new Booking(31, 11, "2025-04-21", "14:00:00", "17:00:00", "Conference", "Paid"));

        // Insert Events
        //eventDAO.insertEvent(new Event(1, "Music Concert", "2025-05-10", "Main Hall", 500));

        // Insert Payments
        paymentDAO.insertPayment(new Payment(45, 31, 150.00, "2025-04-22", "Credit Card"));

        // Insert Reviews
        reviewDAO.insertReview(new Review(30, 11, 31, 5, "Amazing event! Loved it.", "Thank you for visiting us!"));

        System.out.println("✅ Sample data inserted successfully.");
    }
}
