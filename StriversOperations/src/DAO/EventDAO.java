package DAO;

import auth.DatabaseConnection;
import Models.Event;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EventDAO {
    public static void insertEvent(Event event) {
        String query = "INSERT INTO events (EventName, EventDate, Location, Capacity) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, event.getEventName());
            stmt.setString(2, event.getEventDate());
            stmt.setString(3, event.getLocation());
            stmt.setInt(4, event.getCapacity());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
