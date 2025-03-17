package BoxOfficeInterface;

import java.sql.Connection;
import java.sql.Date;
import java.util.List;
public interface BoxOffice { // consisting of methods to allow BoxOfficeT25 to access certain data

    // These are the abstract classes that we (Operations) will implement to allow certain and enough required access
    // to your SQL Database, according to Box Office's Requirement Specification

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

    // Restricted View Seats
    /**
     * Collects data on the seats that have a restricted view
     * @param connection connection to the SQL DB
     * @param hallName name of the hall required
     * @return seats that have a restricted view
     */
    List<SeatingConfiguration> isRestricted(Connection connection, String hallName);

    // Reserved Seating
    /**
     * Collects data on seats that are of type 'Reserved'
     * @param connection connection to the SQL DB
     * @param hallName name of the hall required
     * @return the seats that are reserved
     */
    List<SeatingConfiguration> isReserved(Connection connection, String hallName);

    // Wheelchair Seating
    /**
     * Seats that are of type wheelChair
     * @param connection connection the SQL DB
     * @param hallName name of the hall required
     * @return seats that are for wheelChair users
     */
    List<WheelChairSeatConfig> isAccessible(Connection connection, String hallName);

    // Calendar Availability for a specific date
    /**
     * Calendar TimeSlots that are available for a specific date
     * @param connection connection the SQL DB
     * @param BookingDate date that is required
     * @return TimeSlots that are available on that BookingDate
     */
    List<String> getCalendarAvailability(Connection connection, Date BookingDate);

}
