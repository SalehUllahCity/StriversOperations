package BoxOfficeInterface;

import java.sql.*;
import java.util.List;

/**
 * Database access class for the box office system.
 * Provides methods for connecting to and interacting with the MySQL database,
 * including operations for venue availability, seating configurations,
 * and calendar management.
 */
public class JDBC {
    /** Database connection instance */
    private final Connection connection;
    /** Box office data handler */
    private BoxOfficeData boxOfficeData;

    /**
     * Constructs a new JDBC instance and establishes a database connection.
     * Initializes the connection using predefined credentials and sets up
     * the BoxOfficeData handler.
     * 
     * @throws SQLException If a database access error occurs
     * @throws ClassNotFoundException If the JDBC driver class cannot be found
     */
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
     * 
     * @param args Command line arguments (not used)
     * @throws SQLException If a database access error occurs
     * @throws ClassNotFoundException If the JDBC driver class cannot be found
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


    /* Wrapper methods that delegate to BoxOfficeData */

    /**
     * Retrieves a list of venue unavailability periods.
     * @return List of strings representing unavailable time slots
     */
    public List<String> getVenueUnavailability() {
        return boxOfficeData.getVenueUnavailability(connection);
    }

    /**
     * Retrieves seating configurations for a specific hall.
     * @param hallName The name of the hall to get configurations for
     * @return List of seating configurations
     */
    public List<SeatingConfiguration> getSeatingConfigurations(String hallName) {
        return boxOfficeData.seatingConfigurations(connection, hallName);
    }

    /**
     * Retrieves a list of restricted seats for a specific hall.
     * @param hallName The name of the hall to get restricted seats for
     * @return List of restricted seating configurations
     */
    public List<SeatingConfiguration> getRestrictedSeats(String hallName) {
        return boxOfficeData.isRestricted(connection, hallName);
    }

    /**
     * Retrieves a list of reserved seats for a specific hall.
     * @param hallName The name of the hall to get reserved seats for
     * @return List of reserved seating configurations
     */
    public List<SeatingConfiguration> getReservedSeats(String hallName) {
        return boxOfficeData.isReserved(connection, hallName);
    }

    /**
     * Retrieves a list of wheelchair accessible seats for a specific hall.
     * @param hallName The name of the hall to get wheelchair seats for
     * @return List of wheelchair seat configurations
     */
    public List<WheelChairSeatConfig> getWheelchairSeats(String hallName) {
        return boxOfficeData.isAccessible(connection, hallName);
    }

    /**
     * Retrieves calendar availability for a specific date.
     * @param date The date to check availability for
     * @return List of available time slots
     */
    public List<String> getCalendarAvailability(Date date) {
        return boxOfficeData.getCalendarAvailability(connection, date);
    }

    /**
     * For SELECT queries that return results
     * @param query The SQL query to execute
     * @return ResultSet containing the query results
     * @throws SQLException If a database access error occurs
     */
    public ResultSet executeQuery(String query) throws SQLException {
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }

    /**
     * For parameterized SELECT queries (prevent SQL injection)
     * Prevents SQL injection by using prepared statements.
     * @param query The SQL query with parameter placeholders
     * @param params The parameters to substitute in the query
     * @return ResultSet containing the query results
     * @throws SQLException If a database access error occurs
     */
    public ResultSet executeQuery(String query, Object... params) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        return pstmt.executeQuery();
    }

    /**
     * Executes an INSERT, UPDATE, or DELETE operation.
     * @param query The SQL query to execute
     * @return The number of rows affected
     * @throws SQLException If a database access error occurs
     */
    public int executeUpdate(String query) throws SQLException {
        Statement stmt = connection.createStatement();
        return stmt.executeUpdate(query);
    }

    /**
     * For parameterized UPDATE queries (prevent SQL injection)
     * Prevents SQL injection by using prepared statements.
     * @param query The SQL query with parameter placeholders
     * @param params The parameters to substitute in the query
     * @throws SQLException If a database access error occurs
     */
    public void executeUpdate(String query, Object... params) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        pstmt.executeUpdate();
    }

    /**
     * Closes the database connection.
     * @throws SQLException If a database access error occurs
     */
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}

