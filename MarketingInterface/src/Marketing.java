import java.sql.Connection;
import java.util.List;

public interface Marketing {
    // Venue Availability
    List<String> getVenueAvailability(Connection connection);

    // Seating Configurations
    List<SeatingConfiguration> seatingConfigurations(Connection connection, String hallName);

    // Wheelchair Seating
    List<WheelChairSeatConfig> isAccessible(Connection connection, String hallName);
}
