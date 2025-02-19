import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BoxOfficeData implements BoxOffice {

    /**
     * Collects that data of events that are booked
     * @param connection connection to the SQL DB
     * @return the current bookings
     */
    @Override
    public List<String> getVenueUnavailability(Connection connection) {
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
     * Collects data on the seating in a hall and prints it
     * @param connection connection to the SQL DB
     * @param hallName name of the hall for the seating arrangements
     * @return seating arrangement for that hall
     */
    @Override
    public List<SeatingConfiguration> seatingConfigurations(Connection connection, String hallName) {
        List<SeatingConfiguration> seatingConfigurations = new ArrayList<>();

        String query = "SELECT s.SeatNumber, s.SeatType, s.SeatStatus \n" +
                "FROM Seating s\n" +
                "JOIN Booking b ON s.BookingID = b.BookingID\n" +
                "WHERE b.BookingType = ?";

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
     * Collects data on the seats that have a restricted view
     * @param connection connection to the SQL DB
     * @param hallName name of the hall required
     * @return seats that have a restricted view
     */
    @Override
    public List<SeatingConfiguration> isRestricted(Connection connection, String hallName) {
        List<SeatingConfiguration> restrictedSeating = new ArrayList<>();

        String query = "SELECT s.SeatNumber, s.SeatType, s.SeatStatus \n" +
                "FROM Seating s\n" +
                "JOIN Booking b ON s.BookingID = b.BookingID\n" +
                "WHERE b.BookingType = ? AND s.SeatType = 'Restricted'";

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
     * Collects data on seats that are of type 'Reserved'
     * @param connection connection to the SQL DB
     * @param hallName name of the hall required
     * @return the seats that are reserved
     */
    @Override
    public List<SeatingConfiguration> isReserved(Connection connection, String hallName) {
        List<SeatingConfiguration> reservedSeating = new ArrayList<>();

        String query = "SELECT s.SeatNumber, s.SeatType, s.SeatStatus \n" +
                "FROM Seating s\n" +
                "JOIN Booking b ON s.BookingID = b.BookingID\n" +
                "WHERE b.BookingType = ? AND s.SeatType = 'Reserved'";

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
     * Seats that are of type wheelChair
     * @param connection connection the SQL DB
     * @param hallName name of the hall required
     * @return seats that are for wheelChair users
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

    /**
     * Calendar TimeSlots that are available for a specific date
     * @param connection connection the SQL DB
     * @param BookingDate date that is required
     * @return TimeSlots that are available on that BookingDate
     */
    @Override
    public List<String> getCalendarAvailability(Connection connection, Date BookingDate) {
        List<String> calendarAvailability = new ArrayList<>();
        String query = "SELECT ts.SlotTime \n" +
                "FROM TimeSlots ts\n" +
                "LEFT JOIN Booking b \n" +
                "ON ts.SlotTime BETWEEN b.StartTime AND b.EndTime\n" +
                "AND b.BookingDate = ?\n" +
                "WHERE b.BookingID IS NULL";

        // A prepared statement is a parameterized and reusable SQL query which forces the developer to write the SQL
        // command and the user-provided data separately

        try {
            // set a stm string here for the dates
            PreparedStatement stm = connection.prepareStatement(query);
            stm.setDate(1, BookingDate);
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

    /**
     * Checks if the adjacent seat of a wheelchair seat is taken (if true, then need to change to false)
     * @param connection connection to the SQL DB
     * @param rowNumber Row of the seat
     * @param seatNumber Number of the seat
     * @param hallName name of the hall for that seat
     * @return returns true if adjacent seat is taken, and false if not
     * @throws SQLException error
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
     * Helper method that checks whether the adjacent seat is 'Taken' or 'Reserved'
     * @param connection connection to the SQL DB
     * @param query the query that is run to get this particular seat
     * @param rowNumber row number of the seat
     * @param seatNumber seat number
     * @param hallName name of the hall for that seat
     * @return whether that seat is taken e.g. if the adjacent seat is 'Reserved' or 'Taken'
     * @throws SQLException error
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


