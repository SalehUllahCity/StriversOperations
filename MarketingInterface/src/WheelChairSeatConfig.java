public class WheelChairSeatConfig {
    private String rowNumber;
    private String seatNumber;
    private boolean isAdjacentSeatTaken;

    public WheelChairSeatConfig(String rowNumber, String seatNumber, boolean isAdjacentSeatTaken) {
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.isAdjacentSeatTaken = isAdjacentSeatTaken;
    }

    @Override
    public String toString() {
        return "Wheelchair Seat: Row " + rowNumber + " Seat " + seatNumber + ", Adjacent Seat Taken: " + isAdjacentSeatTaken;
    }
}
