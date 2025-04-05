package Models;

public class Review {
    private int reviewID;
    private int userID;
    private int bookingID;
    private int rating;
    private String comments;
    private String response;

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
