import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/* TO-DO
- Implement column for reserved spaces? -> this is more of a seating configuration thing maybe

 */
public class BoxOfficeData implements BoxOffice {

    @Override
    public List<String> getVenueAvailability(Connection connection) {
        List<String> venueAvailability = new ArrayList<>();
        String query = "SELECT BookingID, UserID, BookingDate, StartTime, EndTime, BookingType FROM booking " +
                "WHERE PaymentStatus = 'Paid'";

        // A prepared statement is a parameterized and reusable SQL query which forces the developer to write the SQL
        // command and the user-provided data separately

        try {
            PreparedStatement stm = connection.prepareStatement(query);
            ResultSet resultSet = stm.executeQuery();

            while (resultSet.next()) { // while there is a next column
                String Booking = "BookingID: " + resultSet.getInt(1) +
                        ", UserID: " + resultSet.getInt(2) +
                        ", Date: " + resultSet.getDate(3) +
                        ", StartTime: " + resultSet.getTime(4) +
                        ", EndTime: " + resultSet.getTime(5) +
                        ", BookingType: " + resultSet.getString(6);
                venueAvailability.add(Booking);

            }


        } catch (SQLException e) {
            System.out.println(e);
        }
        return venueAvailability;
    }

    @Override
    public List<SeatingConfiguration> seatingConfigurations(Connection connection, String hallName) {
        List<SeatingConfiguration> seatingConfigurations = new ArrayList<>();

        // Might get rid of SeatNumber or SeatID as they could mean the same thing, ask Samir if there is a difference
        String query = "SELECT s.SeatNumber, s.SeatType, s.SeatStatus \n" +
                "FROM Seating s\n" +
                "JOIN Booking b ON s.BookingID = b.BookingID\n" +
                "WHERE b.BookingType = ?"; // is our BookingType the room or type of event, in this case its hallName
                    // if event type, we need a variable for hallName

        try {
            PreparedStatement stm = connection.prepareStatement(query);
            stm.setString(1, hallName);
            // setString(int parameterIndex, String x) Sets the designated parameter to the given Java String value.
            ResultSet resultSet = stm.executeQuery();

            while (resultSet.next()) {
                SeatingConfiguration configuration = new SeatingConfiguration(
                        resultSet.getString("SeatNumber"),
                        resultSet.getString("SeatType"),
                        resultSet.getString("SeatStatus")
                );
                seatingConfigurations.add(configuration);


            }

        } catch (SQLException e) {
            System.out.println(e);
        }
        return seatingConfigurations;

    }

    /* This seems similar to venue availability, so I have temporarily archived this - SU
    @Override
    public List<String> getOperationUpdates(Connection connection) {
        // Show the null days with the times that are in use, and they can work around those times or OUTER LEFT JOIN

    }

     */
}
