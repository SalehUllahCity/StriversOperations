package Models;

/**
 * Represents a review in the venue management system.
 * This class stores information about reviews submitted by users for bookings,
 * including ratings, comments, and any responses from venue staff.
 */
public class Review {
    private int reviewID;
    private int userID;
    private int bookingID;
    private int rating;
    private String comments;
    private String response;

    /**
     * Constructs a new Review with the specified details.
     * 
     * @param reviewID Unique identifier for the review
     * @param userID ID of the user who submitted the review
     * @param bookingID ID of the booking this review is associated with
     * @param rating Rating given in the review
     * @param comments Text comments provided in the review
     * @param response Response from venue staff to the review
     */
    public Review(int reviewID, int userID, int bookingID, int rating, String comments, String response) {
        this.reviewID = reviewID;
        this.userID = userID;
        this.bookingID = bookingID;
        this.rating = rating;
        this.comments = comments;
        this.response = response;
    }

    // Getters
    public int getReviewID() { return reviewID; }
    public int getUserID() { return userID; }
    public int getBookingID() { return bookingID; }
    public int getRating() { return rating; }
    public String getComments() { return comments; }
    public String getResponse() { return response; }

    // Setters
    public void setReviewID(int reviewID) { this.reviewID = reviewID; }
    public void setUserID(int userID) { this.userID = userID; }
    public void setEventID(int eventID) { this.bookingID = bookingID; }
    public void setRating(int rating) { this.rating = rating; }
    public void setComments(String comments) { this.comments = comments; }
}
