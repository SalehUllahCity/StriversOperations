package BoxOfficeInterface;

import java.sql.*;
import java.util.List;

public class JDBC {
    private final Connection connection;
    private BoxOfficeData boxOfficeData;


    public JDBC() throws SQLException, ClassNotFoundException {
        String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
        String userName = "in2033t26_a";
        String password = "jLxOPuQ69Mg";

        Class.forName("com.mysql.cj.jdbc.Driver");
        this.connection = DriverManager.getConnection(url, userName, password);
        this.boxOfficeData = new BoxOfficeData();
    }

    /**
     * This class demonstrates how to use JDBC to connect to the City University
     * MySQL database and retrieve various box office-related data using the
     * BoxOfficeData class.
     * The data includes:
     * - Venue unavailability (existing bookings)
     * - Seating configurations
     * - Restricted seats
     * - Reserved seats
     * - Wheelchair accessible seats
     * - Calendar availability
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        /*
        // URL
        String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26"; // local but this would become the university DB Server
        // String userName = "root"; // change to team username
        // String password = "root"; // default password is local password -> change to team password when it works

        String userName = "in2033t26_d"; // change to team username
        String password = "h9DHknCPLaU"; // default password is local password -> change to team password when it works
        // make sure that you are connection the city vpn beforehand

         */
        // JDBC accessor = new JDBC();

        try {
            // Class.forName("com.mysql.cj.jdbc.Driver");

            // create the connection, git connection
            // Connection connection = DriverManager.getConnection(url, userName, password);

            BoxOfficeData boxOfficeData = new BoxOfficeData();

            // Venue Unavailability/Schedule
            // List<String> venueUnavailability = boxOfficeData.getVenueUnavailability(connection);
            // List<String> venueUnavailability = getVenueUnavailability();

            /*
            System.out.println("Venue Unavailability (Current Bookings):");
            for (String unavailability : venueUnavailability) {
                System.out.println(unavailability);
            }

            System.out.println("\n");

             */

            /*
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

             */
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // Wrapper methods that delegate to BoxOfficeData
    public List<String> getVenueUnavailability() {
        return boxOfficeData.getVenueUnavailability(connection);
    }

    public List<SeatingConfiguration> getSeatingConfigurations(String hallName) {
        return boxOfficeData.seatingConfigurations(connection, hallName);
    }

    public List<SeatingConfiguration> getRestrictedSeats(String hallName) {
        return boxOfficeData.isRestricted(connection, hallName);
    }

    public List<SeatingConfiguration> getReservedSeats(String hallName) {
        return boxOfficeData.isReserved(connection, hallName);
    }

    public List<WheelChairSeatConfig> getWheelchairSeats(String hallName) {
        return boxOfficeData.isAccessible(connection, hallName);
    }

    public List<String> getCalendarAvailability(Date date) {
        return boxOfficeData.getCalendarAvailability(connection, date);
    }

    /**
     * For SELECT queries that return results
     */
    public ResultSet executeQuery(String query) throws SQLException {
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }

    /**
     * For parameterized SELECT queries (prevent SQL injection)
     */
    public ResultSet executeQuery(String query, Object... params) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        return pstmt.executeQuery();
    }

    /**
     * For INSERT, UPDATE, DELETE operations
     */
    public int executeUpdate(String query) throws SQLException {
        Statement stmt = connection.createStatement();
        return stmt.executeUpdate(query);
    }

    /**
     * For parameterized UPDATE queries (prevent SQL injection)
     */
    public void executeUpdate(String query, Object... params) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        pstmt.executeUpdate();
    }


    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}

