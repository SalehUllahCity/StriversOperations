package Models;

/**
 * Represents a payment in the venue management system.
 * This class stores information about payments made for bookings,
 * including the amount, date, and payment method used.
 */
public class Payment {
    private int paymentID;
    private int bookingID;
    private double amount;
    private String paymentDate;
    private String paymentMethod;

    /**
     * Constructs a new Payment with the specified details.
     * 
     * @param paymentID Unique identifier for the payment
     * @param bookingID ID of the booking this payment is associated with
     * @param amount Amount of the payment
     * @param paymentDate Date when the payment was made in YYYY-MM-DD format
     * @param paymentMethod Method used for the payment
     */
    public Payment(int paymentID, int bookingID, double amount, String paymentDate, String paymentMethod) {
        this.paymentID = paymentID;
        this.bookingID = bookingID;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }

    // Getters
    public int getPaymentID() { return paymentID; }
    public int getBookingID() { return bookingID; }
    public double getAmount() { return amount; }
    public String getPaymentDate() { return paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }

    // Setters
    public void setPaymentID(int paymentID) { this.paymentID = paymentID; }
    public void setBookingID(int bookingID) { this.bookingID = bookingID; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
