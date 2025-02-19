import java.sql.Connection;
import java.sql.Date;
import java.util.List;
public interface BoxOffice { // consisting of methods to allow BoxOfficeT25 to access certain data

    // These are the abstract classes that we (Operations) will implement to allow certain and enough required access
    // to your SQL Database, according to Box Office's Requirement Specification

    // Venue Calendar and Availability

    /**
     *
     * @param connection
     * @return
     */
    List<String> getVenueAvailability(Connection connection);

    // Seating Configurations

    /**
     *
     * @param connection
     * @param hallName
     * @return
     */
    List<SeatingConfiguration> seatingConfigurations(Connection connection, String hallName);

    /**
     *
     * @param connection
     * @param hallName
     * @return
     */
    List<SeatingConfiguration> isRestricted(Connection connection, String hallName);

    /**
     *
     * @param connection
     * @param hallName
     * @return
     */
    List<SeatingConfiguration> isReserved(Connection connection, String hallName);

    /**
     *
     * @param connection
     * @param hallName
     * @return
     */
    List<WheelChairSeatConfig> isAccessible(Connection connection, String hallName);


    // Test run of free calendar spots that day
    List<String> getCalendarAvailability(Connection connection, Date BookingDate);

    // Operational Updates - I have temp archived this as it seems similar to venue availability - SU
    // List<String> getOperationUpdates(Connection connection);


}
