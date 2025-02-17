import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class JDBC {
    public static void main(String[] args) {
        // URL
        String url = "jdbc:mysql://localhost:3306/in2033t26";
        String userName = "root"; // change to team username
        String password = "root"; // default password is empty but change to other

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // create the connection, git connection
            Connection connection = DriverManager.getConnection(url, userName, password);

            MarketingData marketingData = new MarketingData();

            // Venue Accessibility
            List<String> venueAvailability = marketingData.getVenueAvailability(connection);

            System.out.println("Venue Availability (Current Bookings:"); // to show only availability might need to
            // create a separate table that joins and then finds the difference or along those lines
            for (String availability : venueAvailability) {
                System.out.println(availability);
            }

            System.out.println("\n");


            // Seating Configuration
            List<SeatingConfiguration> seatingConfigurationList = marketingData.seatingConfigurations(connection, "Meeting");

            System.out.println("SeatingConfiguration:");
            for (SeatingConfiguration config : seatingConfigurationList) {
                System.out.println(config);
            }

            System.out.println("\n");


            //  Wheelchair Seating
            List<WheelChairSeatConfig> wheelchairSeating = marketingData.isAccessible(connection, "Meeting");

            System.out.println("Wheelchair Seating: ");
            for (WheelChairSeatConfig wheelchair : wheelchairSeating) {
                System.out.println(wheelchair);
            }

            System.out.println("\n");

            connection.close(); // close connection

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
