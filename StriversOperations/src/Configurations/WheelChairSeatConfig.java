package Configurations;

/**
 * Represents the configuration of a wheelchair-accessible seat in a venue.
 * Stores details about the seat's location (row and seat number) and whether
 * the adjacent seat is taken, which is important for accessibility considerations.
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
