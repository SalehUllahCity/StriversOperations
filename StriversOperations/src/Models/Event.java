package Models;

/**
 * Represents an event in the venue management system.
 * This class stores information about events hosted at the venue,
 * including details about the event name, date, location, and capacity.
 */
public class Event {
    private int eventID;
    private String eventName;
    private String eventDate;
    private String location;
    private int capacity;

    /**
     * Constructs a new Event with the specified details.
     * 
     * @param eventID Unique identifier for the event
     * @param eventName Name of the event
     * @param eventDate Date of the event in YYYY-MM-DD format
     * @param location Location where the event will be held
     * @param capacity Maximum number of attendees allowed at the event
     */
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
