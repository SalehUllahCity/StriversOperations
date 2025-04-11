package auth;

import java.sql.Connection;
import java.sql.DriverManager;

// Could've used this instead of all the duplicate JDBC connections throughout the code

/**
 * A utility class for managing database connections.
 * Provides a centralized way to establish connections to the MySQL database.
 * This class uses a singleton pattern to maintain consistent database connection parameters.
 */
public class DatabaseConnection {
    /** The URL for connecting to the MySQL database */
    private static final String URL = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
    
    /** The username for database authentication */
    private static final String USER = "in2033t26_a";  // change to DDL
    
    /** The password for database authentication */
    private static final String PASSWORD = "jLxOPuQ69Mg";

    /**
     * Establishes and returns a connection to the database.
     * Uses the predefined URL, username, and password to create the connection.
     * 
     * @return A Connection object representing the database connection
     * @throws Exception If there is an error establishing the database connection
     */
    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
