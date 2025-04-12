package api;

import boxoffice.database.TicketSale;
import operations.implementation.JDBC;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * API class for handling box office operations and ticket sales.
 * Provides functionality for retrieving ticket sales data, calculating revenue,
 * and generating revenue reports for events and performances.
 */
public class BoxOfficeAPI {
    /**
     * Main method demonstrating the usage of BoxOfficeAPI functionality.
     * Retrieves and displays:
     * - Ticket sales for a specific event
     * - Revenue for a specific performance
     * - Monthly revenue report
     * 
     * @param args Command line arguments (not used)
     * @throws SQLException If a database access error occurs
     * @throws ClassNotFoundException If the JDBC driver class cannot be found
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        JDBC jdbc = new JDBC();


        List<TicketSale> ticketSales = jdbc.getTicketSalesBasedOnEvent("Great Man Show");

        for (TicketSale ticketSale : ticketSales) {
            System.out.println(ticketSale);
        }

        System.out.println("\n");


        String performanceName = "Great Man Show";
        int revenueBasedOnEvent = jdbc.getRevenueBasedOnEvent("Great Man Show");

        System.out.println("Revenue for " + performanceName + ": " + revenueBasedOnEvent + "\n");

        Map<String, Object> monthlyRevenueReport = jdbc.getMonthlyRevenueReport(2025, 4);

        System.out.println("Monthly Revenue: \n");
        for (Map.Entry<String, Object> entry : monthlyRevenueReport.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }


    }
}
