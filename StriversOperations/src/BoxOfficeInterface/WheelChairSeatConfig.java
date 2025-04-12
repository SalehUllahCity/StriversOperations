package BoxOfficeInterface;

/**
 * Represents the configuration of a wheelchair-accessible seat in a venue.
 * This class is used by the box office system to track and manage wheelchair-accessible
 * seating locations, including their position and the status of adjacent seats,
 * which is crucial for ensuring proper accessibility and seating arrangements.
 */
public class WheelChairSeatConfig {
    private final String rowNumber;
    private final String seatNumber;
    private final boolean isAdjacentSeatTaken;

    /**
     * Class constructor for Wheelchair configuration
     * @param rowNumber wheelChair row number
     * @param seatNumber wheelChair seat number
     * @param isAdjacentSeatTaken whether the Wheelchair adjacent seat is taken
     */
    public WheelChairSeatConfig(String rowNumber, String seatNumber, boolean isAdjacentSeatTaken) {
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.isAdjacentSeatTaken = isAdjacentSeatTaken;
    }

    /**
     * Returns a string representation of the wheelchair seat configuration.
     * The format includes the row number, seat number, and the status of the adjacent seat.
     * This representation is used for display purposes in the box office interface.
     * 
     * @return A formatted string containing the wheelchair seat's configuration details
     */
    @Override
    public String toString() {
        return "Wheelchair Seat: Row " +
                rowNumber + " Seat " +
                seatNumber + ", Adjacent Seat Taken: "
                + isAdjacentSeatTaken;
    }
}
