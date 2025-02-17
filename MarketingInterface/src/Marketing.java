import java.sql.Connection;
import java.util.List;

public interface Marketing {
    // Venue Availability

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

    // Wheelchair Seating
    /**
     *
     * @param connection
     * @param hallName
     * @return
     */
    List<WheelChairSeatConfig> isAccessible(Connection connection, String hallName);
}
