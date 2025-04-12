package DAO;

import auth.DatabaseConnection;
import Models.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) class for handling event-related database operations.
 * This class provides methods to interact with the events table in the database,
 * including inserting new events with proper transaction management and error handling.
 */
public class EventDAO {
    /**
     * Inserts a new event into the database.
     * This method handles the entire event insertion process, including:
     * - Transaction management (begin, commit, rollback)
     * - Parameter binding for the prepared statement
     * - Error handling and connection cleanup
     * 
     * @param event The Event object containing the event details to be inserted
     * @return true if the event was successfully inserted, false otherwise
     */
    public static boolean insertEvent(Event event) {
        String query = "INSERT INTO events (EventName, EventDate, Location, Capacity) VALUES (?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, event.getEventName());
                stmt.setString(2, event.getEventDate());
                stmt.setString(3, event.getLocation());
                stmt.setInt(4, event.getCapacity());
                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
