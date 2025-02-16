public class SeatingConfiguration {
    private String seatNumber;
    private String isRestrictedView; // could change to boolean AND change to seatType
    private String isAvailable; // could change to boolean type AND change to seatStatus

    public SeatingConfiguration(String seatNumber, String isRestrictedView, String isAvailable) {
        this.seatNumber = seatNumber;
        this.isRestrictedView = isRestrictedView;
        this.isAvailable = isAvailable;

    }

    //The toString() method returns the string itself. This method may seem redundant, but its purpose is to
    // allow code that is treating the string as a more generalized object to know its string value without
    // casting it to String type.

    @Override
    public String toString() {
        return "Seat Number: " + seatNumber +
                ", Seat Type: " + isRestrictedView +
                ", Seat Status: " + isAvailable;
    }





}
