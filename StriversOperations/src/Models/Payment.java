package Models;

public class Payment {
    private int paymentID;
    private int bookingID;
    private double amount;
    private String paymentDate;
    private String paymentMethod;

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
