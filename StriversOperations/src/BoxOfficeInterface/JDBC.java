package BoxOfficeInterface;

import java.sql.*;
import java.util.List;

public class JDBC {
    public static void main(String[] args) {
        // URL
        String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26"; // local but this would become the university DB Server
        // String userName = "root"; // change to team username
        // String password = "root"; // default password is local password -> change to team password when it works

        String userName = "in2033t26_a"; // change to team username
        String password = "jLxOPuQ69Mg"; // default password is local password -> change to team password when it works
        // make sure that you are connection the city vpn beforehand

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // create the connection, git connection
            Connection connection = DriverManager.getConnection(url, userName, password);

            BoxOfficeData boxOfficeData = new BoxOfficeData();

            // Venue Unavailability/Schedule
            List<String> venueUnavailability = boxOfficeData.getVenueUnavailability(connection);

            System.out.println("Venue Unavailability (Current Bookings):");
            for (String unavailability : venueUnavailability) {
                System.out.println(unavailability);
            }

            System.out.println("\n");

            // Seating Configuration for a specific hall
            List<SeatingConfiguration> seatingConfigurationList = boxOfficeData.seatingConfigurations(connection, "Meeting");

            System.out.println("Configurations.SeatingConfiguration:");
            for (SeatingConfiguration config : seatingConfigurationList) {
                System.out.println(config);
            }

            System.out.println("\n");

            // Seats that have a restricted view for a specific hall
            List<SeatingConfiguration> restrictedSeating = boxOfficeData.isRestricted(connection, "Meeting");

            System.out.println("Restricted Seating: ");
            for (SeatingConfiguration restricted : restrictedSeating) {
                System.out.println(restricted);
            }

            System.out.println("\n");

            // Reserved Seating
            List<SeatingConfiguration> reservedSeating = boxOfficeData.isReserved(connection, "Meeting");

            System.out.println("Reserved Seating: ");
            for (SeatingConfiguration reserved : reservedSeating) {
                System.out.println(reserved);
            }

            System.out.println("\n");

            // Wheelchair Seating
            List<WheelChairSeatConfig> wheelchairSeating = boxOfficeData.isAccessible(connection, "Meeting");

            System.out.println("Wheelchair Seating: ");
            for (WheelChairSeatConfig wheelchair : wheelchairSeating) {
                System.out.println(wheelchair);
            }

            System.out.println("\n");

            List<String> calendarAvailability = boxOfficeData.getCalendarAvailability(connection, Date.valueOf("2025-02-10"));

            System.out.println("Calendar Availability: (Open TimeSlots)");
            for (String configs : calendarAvailability) {
                System.out.println(configs);
            }

            connection.close(); // close connection

        } catch (Exception e) {
            System.out.println("Database Error: SQLException: " + e.getMessage());
        }
    }
}

