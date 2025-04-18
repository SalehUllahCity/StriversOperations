package Models;

/**
 * Represents a booking in the venue management system.
 * This class stores information about venue bookings, including timing,
 * type, and payment status. It is used throughout the system to manage
 * and track venue reservations.
 */
public class Booking {
    private int bookingID;
    private int userID;
    private String bookingDate;
    private String startTime;
    private String endTime;
    private String bookingType;
    private String paymentStatus;

    /**
     * Constructs a new Booking with the specified details.
     * 
     * @param bookingID Unique identifier for the booking
     * @param userID ID of the user who made the booking
     * @param bookingDate Date of the booking in YYYY-MM-DD format
     * @param startTime Start time of the booking in HH:MM:SS format
     * @param endTime End time of the booking in HH:MM:SS format
     * @param bookingType Type of booking (e.g., Conference, Meeting, Event)
     * @param paymentStatus Current payment status of the booking
     */
    public Booking(int bookingID, int userID, String bookingDate, String startTime, String endTime, String bookingType, String paymentStatus) {
        this.bookingID = bookingID;
        this.userID = userID;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bookingType = bookingType;
        this.paymentStatus = paymentStatus;
    }

    // Getters
    public int getBookingID() { return bookingID; }
    public int getUserID() { return userID; }
    public String getBookingDate() { return bookingDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getBookingType() { return bookingType; }
    public String getPaymentStatus() { return paymentStatus; }

    // Setters
    public void setBookingID(int bookingID) { this.bookingID = bookingID; }
    public void setUserID(int userID) { this.userID = userID; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
