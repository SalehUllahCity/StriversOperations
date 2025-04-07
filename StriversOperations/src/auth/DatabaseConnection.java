package auth;

import java.sql.Connection;
import java.sql.DriverManager;

// Could've used this instead of all the duplicate JDBC connections throughout the code
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
    private static final String USER = "in2033t26_a";  // change to DDL
    private static final String PASSWORD = "jLxOPuQ69Mg";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

}
