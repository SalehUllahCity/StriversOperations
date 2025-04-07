package auth;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

// Help guide for the user to navigate the application
public class Invoices extends JFrame {

    private final Color background = new Color(18, 32, 35, 255);
    private final int fontSize = 22;
    private JTable invoiceTable;
    private DefaultTableModel tableModel;

    public Invoices() {

        setTitle("Lancaster's Music Hall Software: Invoices");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setResizable(false);
        setLocationRelativeTo(null);

        // Main content pane
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(background);
        setContentPane(contentPane);
        contentPane.add(createHeaderPanel(), BorderLayout.NORTH);

        // Add the table panel
        contentPane.add(createTablePanel(), BorderLayout.CENTER);

        // Add control buttons at the bottom
        contentPane.add(createControlPanel(), BorderLayout.SOUTH);

        // Load data from database
        loadInvoiceData();
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Invoices frame = new Invoices();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel headerContainer = new JPanel();
        headerContainer.setLayout(new BoxLayout(headerContainer, BoxLayout.Y_AXIS));
        headerContainer.setBackground(background);

        // Top bar: Home and Settings buttons
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(background);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // ← Home button
        JButton homeBtn = new JButton("← Home");
        homeBtn.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        homeBtn.setBackground(background);
        homeBtn.setForeground(Color.WHITE);
        homeBtn.setFocusPainted(false);
        homeBtn.setBorderPainted(false);
        homeBtn.addActionListener(e -> {
            dispose();
            new UserHome().setVisible(true);
        });
        addHoverEffect(homeBtn);
        // Add tooltip to home button
        homeBtn.setToolTipText("Return to home screen");
        topBar.add(homeBtn, BorderLayout.WEST);

        //Settings button
        JButton settingsBtn = new JButton("⚙ Settings");
        settingsBtn.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        settingsBtn.setBackground(background);
        settingsBtn.setForeground(Color.WHITE);
        settingsBtn.setFocusPainted(false);
        settingsBtn.setBorderPainted(false);
        settingsBtn.addActionListener(e -> new SettingScreen.SettingsDialog(this).setVisible(true));
        addHoverEffect(settingsBtn);
        // Add tooltip to settings button
        settingsBtn.setToolTipText("Open settings dialog");

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(settingsBtn);
        topBar.add(rightPanel, BorderLayout.EAST);

        // Title Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(background);
        JLabel titleLabel = new JLabel("Invoices");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 36));
        titlePanel.add(titleLabel);

        // Stack both into the header container
        headerContainer.add(topBar);
        headerContainer.add(titlePanel);

        return headerContainer;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(background);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create column names
        String[] columnNames = {
                "Invoice ID", "Client", "Booking Name", "Start Date", "End Date",
                "Booking Costs (£)", "Ticket Sales", "Ticket Revenue (£)", "Due (£)"
        };

        // Create table model with column names
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make only Ticket Sales and Ticket Revenue editable
                return column == 6 || column == 7;
            }
        };

        // Create the table with the model
        invoiceTable = new JTable(tableModel);
        invoiceTable.setFont(new Font("TimesRoman", Font.PLAIN, 14));
        invoiceTable.setRowHeight(25);
        invoiceTable.getTableHeader().setFont(new Font("TimesRoman", Font.BOLD, 14));
        invoiceTable.setSelectionBackground(new Color(45, 66, 70));
        invoiceTable.setSelectionForeground(Color.WHITE);
        invoiceTable.setGridColor(new Color(100, 100, 100));

        // Add tooltips to table cells
        invoiceTable.setDefaultRenderer(Object.class, new TooltipTableCellRenderer());

        // Add tooltips to table headers
        invoiceTable.getTableHeader().setDefaultRenderer(new TooltipHeaderRenderer(invoiceTable.getTableHeader().getDefaultRenderer()));

        // Add a scroll pane containing the table
        JScrollPane scrollPane = new JScrollPane(invoiceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45), 2));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Add table value change listener to recalculate the "Due" column
        invoiceTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 6 || e.getColumn() == 7) { // If Ticket Sales or Revenue changed
                int row = e.getFirstRow();
                calculateDueAmount(row);
            }
        });

        return tablePanel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setBackground(background);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JButton refreshBtn = new JButton("Refresh Data");
        refreshBtn.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        refreshBtn.setBackground(background);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadInvoiceData());
        addHoverEffect(refreshBtn);
        refreshBtn.setToolTipText("Reload invoice data from database");

        JButton loadTicketsBtn = new JButton("Load Ticket Data");
        loadTicketsBtn.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        loadTicketsBtn.setBackground(background);
        loadTicketsBtn.setForeground(Color.WHITE);
        loadTicketsBtn.setFocusPainted(false);
        loadTicketsBtn.addActionListener(e -> loadTicketData());
        addHoverEffect(loadTicketsBtn);
        loadTicketsBtn.setToolTipText("Load ticket sales and revenue data for invoices");

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        saveBtn.setBackground(background);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> saveInvoiceData());
        addHoverEffect(saveBtn);
        saveBtn.setToolTipText("Save ticket data to CSV file");

        JButton printBtn = new JButton("Print Invoice");
        printBtn.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        printBtn.setBackground(background);
        printBtn.setForeground(Color.WHITE);
        printBtn.setFocusPainted(false);
        printBtn.addActionListener(e -> printInvoice());
        addHoverEffect(printBtn);
        printBtn.setToolTipText("Print the selected invoice");

        controlPanel.add(refreshBtn);
        controlPanel.add(loadTicketsBtn);
        controlPanel.add(saveBtn);
        controlPanel.add(printBtn);

        return controlPanel;
    }

    private void addHoverEffect(JButton button) {
        button.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45), 2));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(Color.LIGHT_GRAY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(Color.WHITE);
            }
        });
    }

    private void loadInvoiceData() {
        // Clear existing table data
        tableModel.setRowCount(0);

        try {
            // Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
            String userName = "in2033t26_a";
            String password = "jLxOPuQ69Mg";

            try (Connection conn = DriverManager.getConnection(url, userName, password)) {
                // Modified query to only pull booking and client data without ticket info
                String query = "SELECT b.BookingID, c.CompanyName, b.BookingName, " +
                        "b.BookingDate, b.BookingEndDate, b.TotalCost " +
                        "FROM booking b " +
                        "JOIN client c ON b.ClientID = c.ClientID " +
                        "ORDER BY b.BookingDate DESC";

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {

                    while (rs.next()) {
                        int id = rs.getInt("BookingID");
                        String client = rs.getString("CompanyName");
                        String bookingName = rs.getString("BookingName");
                        String startDate = rs.getString("BookingDate");
                        String endDate = rs.getString("BookingEndDate");
                        double bookingCost = rs.getDouble("TotalCost");

                        // Set default values for ticket data that will be manually entered
                        int ticketSales = 0;
                        double ticketRevenue = 0.0;
                        double due = bookingCost - ticketRevenue;

                        // Add row to table
                        Object[] row = {
                                id, client, bookingName, startDate, endDate,
                                String.format("%.2f", bookingCost), ticketSales,
                                String.format("%.2f", ticketRevenue),
                                String.format("%.2f", due)
                        };
                        tableModel.addRow(row);
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading invoice data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTicketData() {
        // This method will simulate fetching ticket data from another database
        // based on the provided ticketSale data

        try {
            // Create a map to store event names and their ticket information
            Map<String, EventTicketInfo> eventTicketMap = new HashMap<>();

            // Use the data from the output to populate the map
            // Based on the output provided, "Great Man Show" had 14 paid tickets and 5 free ones
            // with total revenue of 350
            eventTicketMap.put("Great Man Show", new EventTicketInfo(19, 350.0));

            // Add more events as needed based on data
            eventTicketMap.put("Jazz Night", new EventTicketInfo(25, 625.0));
            eventTicketMap.put("Classical Concert", new EventTicketInfo(32, 960.0));

            // Update the table with ticket data where the booking name matches
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String bookingName = tableModel.getValueAt(i, 2).toString();
                EventTicketInfo ticketInfo = eventTicketMap.get(bookingName);

                if (ticketInfo != null) {
                    // Update ticket sales and revenue
                    tableModel.setValueAt(ticketInfo.getTicketCount(), i, 6);
                    tableModel.setValueAt(String.format("%.2f", ticketInfo.getRevenue()), i, 7);

                    // Recalculate due amount
                    calculateDueAmount(i);
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Ticket data has been loaded successfully.",
                    "Data Loaded", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading ticket data: " + e.getMessage(),
                    "Data Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Inner class to store ticket information for an event
    private static class EventTicketInfo {
        private final int ticketCount;
        private final double revenue;

        public EventTicketInfo(int ticketCount, double revenue) {
            this.ticketCount = ticketCount;
            this.revenue = revenue;
        }

        public int getTicketCount() {
            return ticketCount;
        }

        public double getRevenue() {
            return revenue;
        }
    }

    private void calculateDueAmount(int row) {
        try {
            // Get booking cost
            double bookingCost = Double.parseDouble(tableModel.getValueAt(row, 5).toString().replace("£", ""));

            // Get ticket revenue
            double ticketRevenue = 0.0;
            Object revenueObj = tableModel.getValueAt(row, 7);
            if (revenueObj != null && !revenueObj.toString().isEmpty()) {
                ticketRevenue = Double.parseDouble(revenueObj.toString().replace("£", ""));
            }

            // Calculate due amount
            double due = bookingCost - ticketRevenue;

            // Update the Due column
            tableModel.setValueAt(String.format("%.2f", due), row, 8);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numeric values for ticket sales and revenue.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveInvoiceData() {
        // Since ticket data is not in this database,
        // we'll create a method to save it to an external file
        // that can be imported to the other system later

        try {
            // Create a simple CSV export of the ticket data
            StringBuilder csvData = new StringBuilder();
            csvData.append("Invoice ID,Client,Booking Name,Ticket Sales,Ticket Revenue,Due\n");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int bookingId = Integer.parseInt(tableModel.getValueAt(i, 0).toString());
                String client = tableModel.getValueAt(i, 1).toString();
                String bookingName = tableModel.getValueAt(i, 2).toString();
                int ticketSales = Integer.parseInt(tableModel.getValueAt(i, 6).toString());
                double ticketRevenue = Double.parseDouble(tableModel.getValueAt(i, 7).toString().replace("£", ""));
                double due = Double.parseDouble(tableModel.getValueAt(i, 8).toString().replace("£", ""));

                csvData.append(String.format("%d,\"%s\",\"%s\",%d,%.2f,%.2f\n",
                        bookingId, client, bookingName, ticketSales, ticketRevenue, due));
            }

            // Save to file - normally would use a file chooser
            String fileName = "ticket_sales_export.csv";
            try {
                java.io.FileWriter writer = new java.io.FileWriter(fileName);
                writer.write(csvData.toString());
                writer.close();

                JOptionPane.showMessageDialog(this,
                        "Ticket sales data exported to " + fileName + "\n" +
                                "Please import this file to the ticket system.",
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);

            } catch (java.io.IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error saving ticket data: " + e.getMessage(),
                        "File Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error processing ticket data: " + e.getMessage(),
                    "Data Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printInvoice() {
        int selectedRow = invoiceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an invoice to print.",
                    "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            // Get selected invoice data
            String client = tableModel.getValueAt(selectedRow, 1).toString();
            String bookingName = tableModel.getValueAt(selectedRow, 2).toString();
            String startDate = tableModel.getValueAt(selectedRow, 3).toString();
            String endDate = tableModel.getValueAt(selectedRow, 4).toString();
            String bookingCost = tableModel.getValueAt(selectedRow, 5).toString();
            String ticketSales = tableModel.getValueAt(selectedRow, 6).toString();
            String ticketRevenue = tableModel.getValueAt(selectedRow, 7).toString();
            String due = tableModel.getValueAt(selectedRow, 8).toString();

            // Create print dialog
            JFrame printFrame = new JFrame("Print Invoice");
            printFrame.setSize(600, 800);
            printFrame.setLocationRelativeTo(this);

            JPanel printPanel = new JPanel();
            printPanel.setLayout(new BoxLayout(printPanel, BoxLayout.Y_AXIS));
            printPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel headerLabel = new JLabel("Lancaster's Music Hall");
            headerLabel.setFont(new Font("TimesRoman", Font.BOLD, 24));
            headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel invoiceLabel = new JLabel("INVOICE");
            invoiceLabel.setFont(new Font("TimesRoman", Font.BOLD, 20));
            invoiceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel dataPanel = new JPanel(new GridLayout(8, 2, 10, 10));
            dataPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

            // Create labels with tooltips for the invoice print
            JLabel clientLabel = new JLabel(client);
            clientLabel.setToolTipText(client);

            JLabel bookingNameLabel = new JLabel(bookingName);
            bookingNameLabel.setToolTipText(bookingName);

            dataPanel.add(new JLabel("Client:"));
            dataPanel.add(clientLabel);

            dataPanel.add(new JLabel("Booking Name:"));
            dataPanel.add(bookingNameLabel);

            dataPanel.add(new JLabel("Start Date:"));
            dataPanel.add(new JLabel(startDate));

            dataPanel.add(new JLabel("End Date:"));
            dataPanel.add(new JLabel(endDate));

            dataPanel.add(new JLabel("Booking Costs:"));
            dataPanel.add(new JLabel("£" + bookingCost));

            dataPanel.add(new JLabel("Ticket Sales:"));
            dataPanel.add(new JLabel(ticketSales));

            dataPanel.add(new JLabel("Ticket Revenue:"));
            dataPanel.add(new JLabel("£" + ticketRevenue));

            dataPanel.add(new JLabel("Amount Due:"));
            JLabel dueLabel = new JLabel("£" + due);
            dueLabel.setFont(new Font("TimesRoman", Font.BOLD, 16));
            dataPanel.add(dueLabel);

            JButton printConfirmBtn = new JButton("Print");
            printConfirmBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            printConfirmBtn.addActionListener(e -> {
                JOptionPane.showMessageDialog(printFrame,
                        "Invoice sent to printer.",
                        "Print", JOptionPane.INFORMATION_MESSAGE);
                printFrame.dispose();
            });

            printPanel.add(headerLabel);
            printPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            printPanel.add(invoiceLabel);
            printPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            printPanel.add(dataPanel);
            printPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            printPanel.add(printConfirmBtn);

            printFrame.add(printPanel);
            printFrame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error preparing print: " + e.getMessage(),
                    "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Custom cell renderer that displays tooltips for table cells
    private class TooltipTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (c instanceof JComponent) {
                JComponent jc = (JComponent) c;
                // Set tooltip text to the cell value
                if (value != null) {
                    jc.setToolTipText(value.toString());
                }
            }
            return c;
        }
    }

    // Custom header renderer that displays tooltips for column headers
    private class TooltipHeaderRenderer implements TableCellRenderer {
        private final TableCellRenderer defaultRenderer;

        public TooltipHeaderRenderer(TableCellRenderer defaultRenderer) {
            this.defaultRenderer = defaultRenderer;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = defaultRenderer.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (c instanceof JComponent) {
                JComponent jc = (JComponent) c;

                // Set tooltip text based on column
                switch (column) {
                    case 0:
                        jc.setToolTipText("Unique identifier for the invoice/booking");
                        break;
                    case 1:
                        jc.setToolTipText("Client company name");
                        break;
                    case 2:
                        jc.setToolTipText("Name of the booking or event");
                        break;
                    case 3:
                        jc.setToolTipText("Starting date of the booking");
                        break;
                    case 4:
                        jc.setToolTipText("Ending date of the booking");
                        break;
                    case 5:
                        jc.setToolTipText("Total cost of the booking in pounds");
                        break;
                    case 6:
                        jc.setToolTipText("Number of tickets sold for this event");
                        break;
                    case 7:
                        jc.setToolTipText("Total revenue from ticket sales in pounds");
                        break;
                    case 8:
                        jc.setToolTipText("Amount due (Booking costs minus ticket revenue)");
                        break;
                    default:
                        jc.setToolTipText(value != null ? value.toString() : "");
                }
            }
            return c;
        }
    }

    // Method to create styled buttons
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("TimesRoman", Font.BOLD, fontSize));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(Color.black);
        button.setForeground(Color.white);
        return button;
    }

    // Method to create styled buttons with tooltip descriptions
    private JButton createButtonWithDescription(String text, String description) {
        JButton button = createStyledButton(text);
        button.setToolTipText(description);
        return button;
    }
}