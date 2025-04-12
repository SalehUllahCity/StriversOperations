package BoxOfficeInterface;
/**
 * Represents the configuration of a single seat in a venue.
 * Stores details about the seat number, whether it has a restricted view,
 * and whether it is currently available.
 */
public class SeatingConfiguration {
    private final String seatNumber;
    private final String isRestrictedView;
    private final String isAvailable;

    /**
     * Class constructor for Seating Configuration
     * @param seatNumber seat number
     * @param isRestrictedView whether the seat has a restricted view
     * @param isAvailable whether the seat is available
     */
    public SeatingConfiguration(String seatNumber, String isRestrictedView, String isAvailable) {
        this.seatNumber = seatNumber;
        this.isRestrictedView = isRestrictedView;
        this.isAvailable = isAvailable;
    }

    // The toString() method returns the string itself. This method may seem redundant, but its purpose is to
    // allow code that is treating the string as a more generalized object to know its string value without
    // casting it to String type.

    /**
     * Returns a string representation of the seat configuration.
     * The format includes the seat number, type (restricted view status),
     * and current availability status.
     * 
     * @return A formatted string containing the seat's configuration details
     */
    @Override
    public String toString() {
        return "Seat Number: " + seatNumber +
                ", Seat Type: " + isRestrictedView +
                ", Seat Status: " + isAvailable;
    }
}
