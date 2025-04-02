package MarketingInterface;

import java.sql.*;
import java.util.List;

public class JDBC {
    public static void main(String[] args) {
        // URL
        String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26"; // local but this would become the university DB Server
        // String userName = "root"; // change to team username
        // String password = "root"; // default password is local password -> change to team password when it works

        String userName = "in2033t26_d"; // change to team username
        String password = "h9DHknCPLaU"; // default password is local password -> change to team password when it works
        // make sure that you are connection the city vpn beforehand

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // create the connection, git connection
            Connection connection = DriverManager.getConnection(url, userName, password);

            MarketingData marketingData = new MarketingData();

            // Venue Unavailability
            List<String> venueUnavailability = marketingData.getVenueUnavailability(connection);

            System.out.println("Venue Unavailability (Current Bookings):"); // to show only availability might need to
            // create a separate table that joins and then finds the difference or along those lines
            for (String unavailability : venueUnavailability) {
                System.out.println(unavailability);
            }

            System.out.println("\n");

            // Seating Configuration
            List<SeatingConfiguration> seatingConfigurationList = marketingData.seatingConfigurations(connection, "Meeting");

            System.out.println("Configurations.SeatingConfiguration:");
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

            // Calendar Availability
            List<String> calendarAvailability = marketingData.getCalendarAvailability(connection, Date.valueOf("2025-02-10"));

            System.out.println("Calendar Availability: (Open TimeSlots)");
            for (String configs : calendarAvailability) {
                System.out.println(configs);
            }

            connection.close(); // close connection

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
