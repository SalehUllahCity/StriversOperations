import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/* TO-DO
- Wheelchair seating locations are not adjacent DONE - BO can do what they will with that data
- Base seating configuration could be done through just having enough data entries in the DB
    so that if BO pull the data, they pull all the seats
- DB needs some work -> change seatNumber to int?
 */
public class BoxOfficeData implements BoxOffice {

    /**
     *
     * @param connection
     * @return
     */
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

    /**
     *
     * @param connection
     * @param hallName
     * @return
     */
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

    /**
     *
     * @param connection
     * @param hallName
     * @return
     */
    @Override
    public List<SeatingConfiguration> isRestricted(Connection connection, String hallName) {
        List<SeatingConfiguration> restrictedSeating = new ArrayList<>();

        // Might get rid of SeatNumber or SeatID as they could mean the same thing, ask Samir if there is a difference
        String query = "SELECT s.SeatNumber, s.SeatType, s.SeatStatus \n" +
                "FROM Seating s\n" +
                "JOIN Booking b ON s.BookingID = b.BookingID\n" +
                "WHERE b.BookingType = ? AND s.SeatType = 'Restricted'"; // is our BookingType the room or type of event, in this case its hallName
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
                restrictedSeating.add(configuration);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return restrictedSeating;
    }

    /**
     *
     * @param connection
     * @param hallName
     * @return
     */
    @Override
    public List<SeatingConfiguration> isReserved(Connection connection, String hallName) {
        List<SeatingConfiguration> reservedSeating = new ArrayList<>();

        // Might get rid of SeatNumber or SeatID as they could mean the same thing, ask Samir if there is a difference
        String query = "SELECT s.SeatNumber, s.SeatType, s.SeatStatus \n" +
                "FROM Seating s\n" +
                "JOIN Booking b ON s.BookingID = b.BookingID\n" +
                "WHERE b.BookingType = ? AND s.SeatType = 'Reserved'"; // is our BookingType the room or type of event, in this case its hallName
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
                reservedSeating.add(configuration);
            }

        } catch (SQLException e) {
            System.out.println(e);
        }
        return reservedSeating;
    }

    /**
     *
     * @param connection
     * @param hallName
     * @return
     */
    @Override
    public List<WheelChairSeatConfig> isAccessible(Connection connection, String hallName) {
        List<WheelChairSeatConfig> wheelchairSeatsConfig = new ArrayList<>();

        String query = "SELECT RowNumber, SeatNumber FROM seating WHERE SeatType = 'WheelChair' AND hallName = ?";

        try {
            PreparedStatement stm = connection.prepareStatement(query);
            stm.setString(1, hallName);

            ResultSet resultSet = stm.executeQuery();

            while (resultSet.next()) {
                String rowNumber = resultSet.getString("RowNumber");
                String seatNumber = resultSet.getString("SeatNumber");

                // Check if adjacent seats are taken
                boolean isAdjacentTaken = isAdjacentTaken(connection, rowNumber, seatNumber, hallName);

                wheelchairSeatsConfig.add(new WheelChairSeatConfig(rowNumber, seatNumber, isAdjacentTaken));
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return wheelchairSeatsConfig;
    }

    @Override
    public List<String> getCalendarAvailability(Connection connection) {
        List<String> calendarAvailability = new ArrayList<>();
        String query = "SELECT ts.SlotTime \n" +
                "FROM TimeSlots ts\n" +
                "LEFT JOIN Booking b \n" +
                "ON ts.SlotTime BETWEEN b.StartTime AND b.EndTime\n" +
                "AND b.BookingDate = '2025-02-18'\n" +
                "WHERE b.BookingID IS NULL";

        // A prepared statement is a parameterized and reusable SQL query which forces the developer to write the SQL
        // command and the user-provided data separately

        try {
            PreparedStatement stm = connection.prepareStatement(query);
            ResultSet resultSet = stm.executeQuery();

            while (resultSet.next()) { // while there is a next column
                String slots =
                        "SlotTime: " + resultSet.getTime("SlotTime");
                calendarAvailability.add(slots);

            }

        } catch (SQLException e) {
            System.out.println(e);
        }
        return calendarAvailability;
    }

    /* This seems similar to venue availability, so I have temporarily archived this - SU
    @Override
    public List<String> getOperationUpdates(Connection connection) {
        // Show the null days with the times that are in use, and they can work around those times or OUTER LEFT JOIN

    }

     */

    // To check is adjacent seat is taken, if the seat is of type 'Wheelchair'

    /**
     *
     * @param connection
     * @param rowNumber
     * @param seatNumber
     * @param hallName
     * @return
     * @throws SQLException
     */
    private boolean isAdjacentTaken(Connection connection, String rowNumber, String seatNumber, String hallName) throws SQLException {
        // Check the adjacent seats (left and right)
        int leftSeat = Integer.parseInt(seatNumber) - 1;  // Seat to the left
        int rightSeat = Integer.parseInt(seatNumber) + 1;  // Seat to the right

        // Query to check if an adjacent seat is taken based on hall name
        String seatStatusQuery = "SELECT SeatStatus FROM Seating WHERE RowNumber = ? AND SeatNumber = ? AND hallName = ?";

        // Check if the left seat is taken
        if (isSeatTaken(connection, seatStatusQuery, rowNumber, leftSeat, hallName)) {
            return true;  // Left seat is taken
        }

        // Check if the right seat is taken
        if (isSeatTaken(connection, seatStatusQuery, rowNumber, rightSeat, hallName)) {
            return true;  // Right seat is taken
        }

        // If neither adjacent seat is taken
        return false;
    }

    /**
     *
     * @param connection
     * @param query
     * @param rowNumber
     * @param seatNumber
     * @param hallName
     * @return
     * @throws SQLException
     */
    // Helper method to check if a seat is taken based on SeatStatus and hall name
    private boolean isSeatTaken(Connection connection, String query, String rowNumber, int seatNumber, String hallName) throws SQLException {
        try (PreparedStatement stm = connection.prepareStatement(query)) {
            stm.setString(1, rowNumber);    // Set the row number
            stm.setInt(2, seatNumber);      // Set the seat number
            stm.setString(3, hallName);     // Set the hall name
            try (ResultSet resultSet = stm.executeQuery()) {
                if (resultSet.next()) {
                    String seatStatus = resultSet.getString("SeatStatus");
                    return "Taken".equals(seatStatus) || "Reserved".equals(seatStatus);
                }
            }
        }
        return false;
    }
}


