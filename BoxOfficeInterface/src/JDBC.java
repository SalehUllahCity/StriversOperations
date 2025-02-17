import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class JDBC {
    public static void main(String[] args) {
        // URL
        String url = "jdbc:mysql://localhost:3306/in2033t26";
        String userName = "root"; // change to team username
        String password = "root"; // default password is empty but change to other

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // create the connection, git connection
            Connection connection = DriverManager.getConnection(url, userName, password);

            BoxOfficeData boxOfficeData = new BoxOfficeData();

            // Venue Availability/Schedule
            List<String> venueAvailability = boxOfficeData.getVenueAvailability(connection);

            System.out.println("Venue Availability:");
            for (String availability : venueAvailability) {
                System.out.println(availability);
            }

            System.out.println("\n");

            // Seating Configuration for a specific hall
            List<SeatingConfiguration> seatingConfigurationList = boxOfficeData.seatingConfigurations(connection, "Meeting");

            System.out.println("SeatingConfiguration:");
            for (SeatingConfiguration config : seatingConfigurationList) {
                System.out.println(config);
            }

            System.out.println("\n");

            // Seats that have a restricted view for a specific hall
            List<SeatingConfiguration> restrictedSeating = boxOfficeData.isRestricted(connection, "Meeting");

            System.out.println("Restricted Seating: ");
            for (SeatingConfiguration restricted : restrictedSeating) {
                System.out.println(restricted);
            }
            // The BookingID in the SQL database is messed up, the correct ID for meeting is '2 - Meeting' - SU
            // Created both of these, will delete or not add to the university team DB
            System.out.println("\n");

            // Reserved Seating
            List<SeatingConfiguration> reservedSeating = boxOfficeData.isReserved(connection, "Meeting");

            System.out.println("Reserved Seating: ");
            for (SeatingConfiguration reserved : reservedSeating) {
                System.out.println(reserved);
            }


            connection.close(); // close connection

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
/*
            // Statement objects
            Statement statement = connection.createStatement();

            // Execute the query method, returns all the objects that are needed to get the results
            // from the table
            ResultSet resultSet = statement.executeQuery("select * from user");

            while (resultSet.next()) {
                System.out.println(resultSet.getInt(1) + " " +
                        resultSet.getString(2) + " "
                        + resultSet.getString(3) + " " +
                        resultSet.getString(4));
            }
            */