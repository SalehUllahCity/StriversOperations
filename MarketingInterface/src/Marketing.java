import java.sql.Connection;
import java.sql.Date;
import java.util.List;

public interface Marketing {
    // Venue Unavailability
    /**
     * Collects that data of events that are booked
     * @param connection connection to the SQL DB
     * @return the current bookings
     */
    List<String> getVenueUnavailability(Connection connection);

    // Seating Configurations
    /**
     * Collects data on the seating in a hall and prints it
     * @param connection connection to the SQL DB
     * @param hallName name of the hall for the seating arrangements
     * @return seating arrangement for that hall
     */
    List<SeatingConfiguration> seatingConfigurations(Connection connection, String hallName);

    // Wheelchair Seating
    /**
     * Collects data of all the wheelchair type seats and whether they have seats adjacent to them taken
     * @param connection connection to the SQL DB
     * @param hallName name of hall for the seating arrangements
     * @return wheelchair seats in the hallName and whether that adjacent seat is taken or not
     */
    List<WheelChairSeatConfig> isAccessible(Connection connection, String hallName);

    // Calendar Availability for a specific date
    /**
     * Collects the data from the DB of empty TimeSlots on a specific BookingDate
     * @param connection connection to the SQL DB
     * @param BookingDate date for available TimeSlots
     * @return TimeSlots on a date that is available
     */
    List<String> getCalendarAvailability(Connection connection, Date BookingDate);
}
