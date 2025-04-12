package api;

import com.lancaster.database.Films;
import com.lancaster.database.OperationsInterface.JDBC;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * API class for handling marketing-related database operations.
 * Provides functionality for retrieving and managing:
 * - Group bookings
 * - Show information
 * - Film schedules by date
 * 
 * Note: Some functionality may be limited due to database connection issues
 * with the marketing environment configuration.
 */
public class MkJDBC {
    /**
     * Main method demonstrating the usage of MkJDBC functionality.
     * Retrieves and displays:
     * - Group booking details
     * - Show information by ID
     * - Film schedules for a specific date
     * 
     * @param args Command line arguments (not used)
     * @throws SQLException If a database access error occurs
     * @throws ClassNotFoundException If the JDBC driver class cannot be found
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        JDBC jdbc = new JDBC();

        // Map<String, Object> groupBookingbyDate = jdbc.getGroupBookingByDate("2025-04-03", connection);
        Map<String, Object> groupBooking = jdbc.getGroupBooking(2);

        // Group

        for (Map.Entry<String, Object> entry : groupBooking.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("\n");


        Map<String, Object> show = jdbc.getShowById(2);

        for (Map.Entry<String, Object> entry : show.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("\n");

        List<Films.FilmInformation> showsbyDate = jdbc.getShowsByDate("2025-04-06");

        for (Films.FilmInformation film : showsbyDate) {
            System.out.println(film);
        }
        System.out.println("\n");

        // Marketing's .env file is not found on the classpath and so the data can not be retrieved from their Database
        // The methods are valid and are found as shown above, but the JDBC connection is not working due to this .env
        // file.



    }
}
