package BoxOfficeInterface;

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
     * toString() method returns the string itself
     * @return string value of these variables
     */
    @Override
    public String toString() {
        return "Wheelchair Seat: Row " +
                rowNumber + " Seat " +
                seatNumber + ", Adjacent Seat Taken: "
                + isAdjacentSeatTaken;
    }
}
