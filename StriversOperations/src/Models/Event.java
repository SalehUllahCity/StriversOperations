package Models;

public class Event {
    private int eventID;
    private String eventName;
    private String eventDate;
    private String location;
    private int capacity;

    public Event(int eventID, String eventName, String eventDate, String location, int capacity) {
        this.eventID = eventID;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.location = location;
        this.capacity = capacity;
    }

    // Getters
    public int getEventID() { return eventID; }
    public String getEventName() { return eventName; }
    public String getEventDate() { return eventDate; }
    public String getLocation() { return location; }
    public int getCapacity() { return capacity; }

    // Setters
    public void setEventID(int eventID) { this.eventID = eventID; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public void setLocation(String location) { this.location = location; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}
