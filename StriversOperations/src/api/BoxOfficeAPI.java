package api;

import boxoffice.database.TicketSale;
import operations.implementation.JDBC;



import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class BoxOfficeAPI {
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
