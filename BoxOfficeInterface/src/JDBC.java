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

            List<String> venueAvailability = boxOfficeData.getVenueAvailability(connection);

            System.out.println("Venue Availability:");
            for (String availability : venueAvailability) {
                System.out.println(availability);
            }

            List<SeatingConfiguration> seatingConfigurationList = boxOfficeData.seatingConfigurations(connection, "Meeting");

            System.out.println("SeatingConfiguration:");
            for (SeatingConfiguration config : seatingConfigurationList) {
                System.out.println(config);
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
            connection.close(); // close connection

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
