package Main;

import DAO.BookingDAO;
import Models.Booking;
import java.sql.Date;
import java.sql.Time;

public class Main {
    public static void main(String[] args) {
        BookingDAO bookingDAO = new BookingDAO();

        Booking booking1 = new Booking(0, 1, Date.valueOf("2025-04-01"), Time.valueOf("10:00:00"), Time.valueOf("12:00:00"), "Conference", "Paid");
        Booking booking2 = new Booking(0, 2, Date.valueOf("2025-04-02"), Time.valueOf("14:00:00"), Time.valueOf("16:00:00"), "Meeting", "Pending");
        Booking booking3 = new Booking(0, 3, Date.valueOf("2025-04-03"), Time.valueOf("18:30:00"), Time.valueOf("20:30:00"), "Workshop", "Unpaid");

        bookingDAO.addBooking(booking1);
        bookingDAO.addBooking(booking2);
        bookingDAO.addBooking(booking3);
    }
}