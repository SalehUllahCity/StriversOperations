import java.sql.Connection;
import java.util.List;
public interface BoxOffice { // consisting of methods to allow BoxOfficeT25 to access certain data

    // These are the abstract classes that we (Operations) will implement to allow certain and enough required access
    // to your SQL Database, according to Box Office's Requirement Specification

    // Venue Calendar and Availability
    List<String> getVenueAvailability(Connection connection);

    // Seating Configurations
    List<SeatingConfiguration> seatingConfigurations(Connection connection, String hallName);

    List<SeatingConfiguration> isRestricted(Connection connection, String hallName);

    // Operational Updates - I have temp archived this as it seems similar to venue availability - SU
    // List<String> getOperationUpdates(Connection connection);


}
