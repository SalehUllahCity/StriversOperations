package Models;

import java.sql.Time;
import java.sql.Date;

public class Booking {
    private int bookingID;
    private int userID;
    private Date bookingDate;
    private Time startTime;
    private Time endTime;
    private String bookingType;
    private String paymentStatus;

    public Booking(int bookingID, int userID, Date bookingDate, Time startTime, Time endTime, String bookingType, String paymentStatus) {
        this.bookingID = bookingID;
        this.userID = userID;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bookingType = bookingType;
        this.paymentStatus = paymentStatus;
    }

    public int getBookingID() { return bookingID; }
    public int getUserID() { return userID; }
    public Date getBookingDate() { return bookingDate; }
    public Time getStartTime() { return startTime; }
    public Time getEndTime() { return endTime; }
    public String getBookingType() { return bookingType; }
    public String getPaymentStatus() { return paymentStatus; }
}
