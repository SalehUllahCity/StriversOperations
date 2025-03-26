package Models;

public class Review {
    private int reviewID;
    private int userID;
    private int eventID;
    private int rating;
    private String comments;

    public Review(int reviewID, int userID, int eventID, int rating, String comments) {
        this.reviewID = reviewID;
        this.userID = userID;
        this.eventID = eventID;
        this.rating = rating;
        this.comments = comments;
    }

    // Getters
    public int getReviewID() { return reviewID; }
    public int getUserID() { return userID; }
    public int getEventID() { return eventID; }
    public int getRating() { return rating; }
    public String getComments() { return comments; }

    // Setters
    public void setReviewID(int reviewID) { this.reviewID = reviewID; }
    public void setUserID(int userID) { this.userID = userID; }
    public void setEventID(int eventID) { this.eventID = eventID; }
    public void setRating(int rating) { this.rating = rating; }
    public void setComments(String comments) { this.comments = comments; }
}
