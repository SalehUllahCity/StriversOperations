package auth;

import boxoffice.database.TicketSale;
import operations.implementation.JDBC;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.time.LocalDate;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

public class Report extends JFrame {

    private final Color background = new Color(18, 32, 35, 255);
    private final int fontSize = 22;

    // Space categories and their colors
    private final Map<String, Color> spaceColors = new HashMap<>() {{
        // Meeting Rooms
        put("The Green Room", new Color(76, 175, 80));    // Green
        put("Brontë Boardroom", new Color(121, 85, 72));  // Brown
        put("Dickens Den", new Color(255, 152, 0));       // Orange
        put("Poe Parlor", new Color(156, 39, 176));       // Purple
        put("Globe Room", new Color(3, 169, 244));        // Light Blue
        put("Chekhov Chamber", new Color(233, 30, 99));   // Pink

        // Performance Spaces
        put("Main Hall", new Color(244, 67, 54));         // Red
        put("Small Hall", new Color(255, 87, 34));        // Deep Orange

        // Rehearsal Space
        put("Rehearsal Space", new Color(0, 150, 136));   // Teal

        // Entire Venue
        put("Entire Venue", new Color(63, 81, 181));      // Indigo
    }};
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Report frame = new Report();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create the frame.
     */
    public Report() throws SQLException, ClassNotFoundException {
        setTitle("Lancaster's Music Hall Software: Reports");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setResizable(false);
        setLocationRelativeTo(null);


        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(background);
        setContentPane(contentPane);


        contentPane.add(createHeaderPanel(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("TimesRoman", Font.BOLD, fontSize));

        tabbedPane.addTab("Venue Usage", createVenueUsageTab());
        tabbedPane.addTab("Daily Sheets", createDailySheetsTab());
        tabbedPane.addTab("Financial Summary", createFinanceTab());
        tabbedPane.addTab("Monthly Revenue", createMonthRevTab());
        tabbedPane.addTab("Ticket Sales", createTicketSalesTab());

        contentPane.add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Creates the top section with Settings and Title.
     */
    private JPanel createHeaderPanel() {
        JPanel headerContainer = new JPanel();
        headerContainer.setLayout(new BoxLayout(headerContainer, BoxLayout.Y_AXIS));
        headerContainer.setBackground(background);


        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(background);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));


        JButton homeBtn = new JButton("← Home");
        homeBtn.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        homeBtn.setBackground(background);
        homeBtn.setForeground(Color.WHITE);
        homeBtn.setFocusPainted(false);
        homeBtn.setBorderPainted(false);
        homeBtn.addActionListener(e -> {
            dispose();
            new UserHome().setVisible(true);
        });
        addHoverEffect(homeBtn);
        topBar.add(homeBtn, BorderLayout.WEST);


        JButton settingsBtn = new JButton("⚙ Settings");
        settingsBtn.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        settingsBtn.setBackground(background);
        settingsBtn.setForeground(Color.WHITE);
        settingsBtn.setFocusPainted(false);
        settingsBtn.setBorderPainted(false);
        settingsBtn.addActionListener(e -> new SettingScreen.SettingsDialog(this).setVisible(true));
        addHoverEffect(settingsBtn);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(settingsBtn);
        topBar.add(rightPanel, BorderLayout.EAST);


        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(background);
        JLabel titleLabel = new JLabel("Reports");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 32));
        titlePanel.add(titleLabel);

        headerContainer.add(topBar);
        headerContainer.add(titlePanel);

        return headerContainer;
    }

    private JPanel createVenueUsageTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(background);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));


        JPanel summaryPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        summaryPanel.setBackground(background);
        summaryPanel.setBorder(new EmptyBorder(0, 0, 15, 0));


        summaryPanel.add(createSummaryCard("Total Bookings", "0"));
        summaryPanel.add(createSummaryCard("Most Popular Space", "Loading..."));
        summaryPanel.add(createSummaryCard("Average Booking Duration", "0 days"));
        summaryPanel.add(createSummaryCard("Peak Booking Time", "Loading..."));
        summaryPanel.add(createSummaryCard("Current Month Usage", "0%"));

        panel.add(summaryPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        centerPanel.setBackground(background);


        JPanel chartWrapper = new JPanel(new BorderLayout());
        chartWrapper.setBackground(background);
        chartWrapper.add(createSpaceUsageChart(), BorderLayout.CENTER);
        centerPanel.add(chartWrapper);


        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setBackground(background);


        JPanel mainFilterPanel = new JPanel(new BorderLayout());
        mainFilterPanel.setBackground(background);
        mainFilterPanel.setBorder(new EmptyBorder(10, 10, 10, 10));


        JPanel spaceFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        spaceFilterPanel.setBackground(background);
        
        JLabel spaceFilterLabel = new JLabel("Filter by Space:");
        spaceFilterLabel.setForeground(Color.WHITE);
        spaceFilterLabel.setFont(new Font("TimesRoman", Font.BOLD, 13));
        spaceFilterPanel.add(spaceFilterLabel);
        
        JComboBox<String> spaceFilter = new JComboBox<>(new String[]{"All Spaces", "The Green Room", "Brontë Boardroom",
                "Dickens Den", "Poe Parlor", "Globe Room", "Chekhov Chamber", "Main Hall", "Small Hall", 
                "Rehearsal Space", "Entire Venue"});
        spaceFilter.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        spaceFilter.setBackground(new Color(30, 50, 55));
        spaceFilter.setForeground(Color.WHITE);
        ((JLabel)spaceFilter.getRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        spaceFilterPanel.add(spaceFilter);


        JPanel dateFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dateFilterPanel.setBackground(background);

        JLabel startDateLabel = new JLabel("Start Date:");
        startDateLabel.setForeground(Color.WHITE);
        startDateLabel.setFont(new Font("TimesRoman", Font.BOLD, 14));
        dateFilterPanel.add(startDateLabel);

        SpinnerDateModel startDateModel = new SpinnerDateModel();
        startDateModel.setValue(new java.util.Date());
        startDateSpinner = new JSpinner(startDateModel);
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(startDateSpinner, "dd-MM-yyyy");
        startDateSpinner.setEditor(startDateEditor);
        startDateSpinner.setPreferredSize(new Dimension(120, 25));
        dateFilterPanel.add(startDateSpinner);

        dateFilterPanel.add(Box.createHorizontalStrut(10));

        JLabel endDateLabel = new JLabel("End Date:");
        endDateLabel.setForeground(Color.WHITE);
        endDateLabel.setFont(new Font("TimesRoman", Font.BOLD, 14));
        dateFilterPanel.add(endDateLabel);

        SpinnerDateModel endDateModel = new SpinnerDateModel();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        endDateModel.setValue(cal.getTime());
        endDateSpinner = new JSpinner(endDateModel);
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "dd-MM-yyyy");
        endDateSpinner.setEditor(endDateEditor);
        endDateSpinner.setPreferredSize(new Dimension(120, 25));
        dateFilterPanel.add(endDateSpinner);

        JButton confirmDates = new JButton("Apply Date Range");
        confirmDates.setFont(new Font("TimesRoman", Font.BOLD, 14));
        confirmDates.setBackground(new Color(30, 50, 55));
        confirmDates.setForeground(Color.WHITE);
        confirmDates.setFocusPainted(false);
        dateFilterPanel.add(confirmDates);

        JPanel combinedFilterPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        combinedFilterPanel.setBackground(background);
        combinedFilterPanel.add(spaceFilterPanel);
        combinedFilterPanel.add(dateFilterPanel);

        mainFilterPanel.add(combinedFilterPanel, BorderLayout.NORTH);
        tablePanel.add(mainFilterPanel, BorderLayout.NORTH);

        String[] columns = {
                "Booking Name", "Client", "Room", "Start Date", "End Date", "Configuration"
        };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells uneditable
            }
        };
        JTable table = new JTable(model);
        table.setDefaultEditor(Object.class, null); // Additional protection against editing
        styleTable(table);

        table.getTableHeader().setBackground(new Color(30, 50, 55));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("TimesRoman", Font.BOLD, 13));

        table.setRowHeight(28);
        table.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        table.setGridColor(new Color(45, 45, 45));
        table.setBackground(new Color(25, 40, 45));
        table.setForeground(Color.WHITE);

        table.setSelectionBackground(new Color(45, 66, 75));
        table.setSelectionForeground(Color.WHITE);

        table.getColumnModel().getColumn(2).setCellRenderer(new RoomColorRenderer());


        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(background);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45), 1));

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(tablePanel);
        panel.add(centerPanel, BorderLayout.CENTER);

        startDateSpinner.addChangeListener(e -> {
            java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
            java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
            
            if (startDate.after(endDate)) {
                endDateSpinner.setValue(startDate);
            }

            loadVenueUsageData(model, spaceFilter, startDateSpinner, endDateSpinner, summaryPanel, false);
        });

        endDateSpinner.addChangeListener(e -> {
            java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
            java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
            
            if (endDate.before(startDate)) {
                endDateSpinner.setValue(startDate);
            }
            
            // Refresh data
            loadVenueUsageData(model, spaceFilter, startDateSpinner, endDateSpinner, summaryPanel, false);
        });

        confirmDates.addActionListener(e -> {
            loadVenueUsageData(model, spaceFilter, startDateSpinner, endDateSpinner, summaryPanel, false);
        });

        spaceFilter.addActionListener(e -> {
            loadVenueUsageData(model, spaceFilter, startDateSpinner, endDateSpinner, summaryPanel, false);
        });

        loadVenueUsageData(model, spaceFilter, startDateSpinner, endDateSpinner, summaryPanel, false);

        createDetailPopup(columns, table);

        return panel;
    }



    private JPanel createSummaryCard(String title, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(30, 50, 55));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(45, 45, 45), 2),
            new EmptyBorder(8, 8, 8, 8)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 13));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("TimesRoman", Font.BOLD, 16));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);

        return card;
    }

    private JPanel createSpaceUsageChart() {
        JPanel chartPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPieChart((Graphics2D) g, startDateSpinner, endDateSpinner);
            }
        };
        chartPanel.setBackground(new Color(30, 50, 55));
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(45, 45, 45), 2),
            new EmptyBorder(10, 10, 10, 10)
        ));
        chartPanel.setPreferredSize(new Dimension(475, 475));
        return chartPanel;
    }

    private void drawPieChart(Graphics2D g, JSpinner startDateSpinner, JSpinner endDateSpinner) {
        Map<String, Integer> spaceUsage = new HashMap<>();
        boolean hasError = false;
        String errorMessage = "";
        
        try {
            String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
            String user = "in2033t26_a";
            String password = "jLxOPuQ69Mg";

            String query = "SELECT Room, COUNT(*) as count FROM booking WHERE BookingDate >= ? AND BookingEndDate <= ? GROUP BY Room";

            try (Connection conn = DriverManager.getConnection(url, user, password);
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
                java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
                stmt.setDate(1, new java.sql.Date(startDate.getTime()));
                stmt.setDate(2, new java.sql.Date(endDate.getTime()));

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String roomName = rs.getString("Room");
                    spaceUsage.put(roomName, rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            hasError = true;
            errorMessage = "Database Error: Please check your connection and try again.";
            if (e.getCause() instanceof java.net.UnknownHostException) {
                errorMessage = "Cannot connect to database. Please ensure you are connected to the City VPN.";
            }
            e.printStackTrace();
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int titleHeight = 30;
        int legendWidth = 143;
        int padding = 19;

        int availableWidth = getWidth() - (padding * 2) - legendWidth;
        int availableHeight = getHeight() - (padding * 2) - titleHeight;

        // Calculate pie chart size
        int diameter = Math.min(availableWidth, availableHeight);
        if (diameter > 380) {
            diameter = 380;
        }

        int y = padding + titleHeight;

        g.setColor(Color.WHITE);
        g.setFont(new Font("TimesRoman", Font.BOLD, 16));
        g.drawString("Space Usage Distribution", padding, padding + 20);

        if (hasError) {
            g.setFont(new Font("TimesRoman", Font.PLAIN, 12));
            g.setColor(new Color(255, 100, 100));
            g.drawString(errorMessage, padding, y + 20);
            return;
        }

        if (spaceUsage.isEmpty()) {
            g.setFont(new Font("TimesRoman", Font.PLAIN, 12));
            g.setColor(Color.WHITE);
            g.drawString("No booking data available", padding, y + 20);
            return;
        }

        int total = spaceUsage.values().stream().mapToInt(Integer::intValue).sum();

        int startAngle = 0;
        for (Map.Entry<String, Integer> entry : spaceUsage.entrySet()) {
            int arcAngle = (int) Math.round(360.0 * entry.getValue() / total);
            String room = entry.getKey();
            g.setColor(spaceColors.getOrDefault(room, new Color(149, 165, 166))); // Default to gray if room not found
            g.fillArc(padding, y, diameter, diameter, startAngle, arcAngle);
            startAngle += arcAngle;
        }

        int legendX = padding + diameter + 14;
        int legendSpacing = 21;
        int i = 0;

        g.setFont(new Font("TimesRoman", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        
        for (Map.Entry<String, Integer> entry : spaceUsage.entrySet()) {
            if (y + (i * legendSpacing) + 24 > getHeight()) {
                legendX += 152;
                i = 0;
            }
            
            String room = entry.getKey();
            g.setColor(spaceColors.getOrDefault(room, new Color(149, 165, 166)));
            g.fillRect(legendX, y + (i * legendSpacing), 10, 10);
            
            g.setColor(Color.WHITE);
            String displayName = normalizeRoomName(room);
            String legendText = String.format("%s (%d)", displayName, entry.getValue());
            if (fm.stringWidth(legendText) > 133) {
                while (fm.stringWidth(legendText + "...") > 133 && !legendText.isEmpty()) {
                    legendText = legendText.substring(0, legendText.length() - 1);
                }
                legendText += "...";
            }
            g.drawString(legendText, legendX + 15, y + (i * legendSpacing) + 9);
            i++;
        }
    }

    private JPanel createTicketSalesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(background);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(background);

        JLabel searchLabel = new JLabel("Search by Performance Name:");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(new Font("TimesRoman", Font.BOLD, 13));
        searchPanel.add(searchLabel);

        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 25));
        searchField.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        searchField.setBackground(new Color(30, 50, 55));
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("TimesRoman", Font.BOLD, 13));
        searchButton.setBackground(new Color(30, 50, 55));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchPanel.add(searchButton);

        JButton clearButton = new JButton("Clear");
        clearButton.setFont(new Font("TimesRoman", Font.BOLD, 13));
        clearButton.setBackground(new Color(30, 50, 55));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        searchPanel.add(clearButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {
                "Ticket ID", "Price", "Customer ID", "Performance ID",
                "Seat ID", "Discount ID", "Group ID", "Staff ID"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);

        table.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setGridColor(new Color(45, 45, 45));
        table.setBackground(new Color(25, 40, 45));
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(45, 66, 75));
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        table.getTableHeader().setBackground(new Color(30, 50, 55));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("TimesRoman", Font.BOLD, 13));
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));
        scrollPane.getViewport().setBackground(table.getBackground());

        panel.add(scrollPane, BorderLayout.CENTER);

        searchButton.addActionListener(e -> {
            String performanceId = searchField.getText().trim();
            if (!performanceId.isEmpty()) {
                loadTicketSalesData(model, performanceId);
            } else {
                JOptionPane.showMessageDialog(panel,
                        "Please enter a Performance ID to search",
                        "Search Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        clearButton.addActionListener(e -> {
            searchField.setText("");
            model.setRowCount(0);
        });

        createDetailPopup(columns, table);

        return panel;
    }

    private void loadTicketSalesData(DefaultTableModel model, String performanceId) {
        try {
            JDBC jdbc = new JDBC();
            List<TicketSale> ticketSales = jdbc.getTicketSalesBasedOnEvent("Great Man Show");

            model.setRowCount(0);

            if (ticketSales.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "No ticket sales found for Performance ID: " + performanceId,
                        "No Results",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            for (TicketSale sale : ticketSales) {
                Object[] row = {
                        sale.getTicketSaleId(),
                        String.format("£%.2f", sale.getPrice()),
                        sale.getCustomerID(),
                        sale.getPerformanceID(),
                        sale.getSeatID(),
                        sale.getDiscountID() != 0 ? sale.getDiscountID() : "N/A",
                        sale.getGroupBookingID() != 0 ? sale.getGroupBookingID() : "N/A",
                        sale.getStaffID()
                };
                model.addRow(row);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving ticket sales data: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadVenueUsageData(DefaultTableModel model, JComboBox<String> spaceFilter, 
            JSpinner startDateSpinner, JSpinner endDateSpinner, JPanel summaryPanel, boolean isAllTime) {
        String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
        String user = "in2033t26_a";
        String password = "jLxOPuQ69Mg";

        String selectedSpace = (String) spaceFilter.getSelectedItem();
        
        model.setRowCount(0);
        
        StringBuilder queryBuilder = new StringBuilder(
            "SELECT BookingName, Client, Room, BookingDate, BookingEndDate, SeatingConfig, StartTime FROM booking WHERE 1=1"
        );
        
        if (!isAllTime) {
            queryBuilder.append(" AND BookingDate >= ? AND BookingEndDate <= ?");
        }
        
        if (selectedSpace != null && !selectedSpace.equals("All Spaces")) {
            queryBuilder.append(" AND Room = ?");
        }
        
        queryBuilder.append(" ORDER BY BookingDate, StartTime");
        
        String query = queryBuilder.toString();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int paramIndex = 1;
            
            if (!isAllTime) {
                java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
                java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
                
                System.out.println("Filtering dates - Start: " + startDate + ", End: " + endDate);
                
                stmt.setDate(paramIndex++, new java.sql.Date(startDate.getTime()));
                stmt.setDate(paramIndex++, new java.sql.Date(endDate.getTime()));
            }
            
            if (selectedSpace != null && !selectedSpace.equals("All Spaces")) {
                stmt.setString(paramIndex, selectedSpace);
            }

            System.out.println("Executing query: " + query);

            ResultSet rs = stmt.executeQuery();
            Map<String, Integer> spaceUsage = new HashMap<>();
            Map<Integer, Integer> hourDistribution = new HashMap<>();
            int totalBookings = 0;
            long totalDuration = 0;

            while (rs.next()) {
                Date bookingDate = rs.getDate("BookingDate");
                Date bookingEndDate = rs.getDate("BookingEndDate");
                
                System.out.println("Found booking: " + rs.getString("BookingName") +
                                 " from " + bookingDate + " to " + bookingEndDate);
                
                Object[] row = {
                        rs.getString("BookingName"),
                        rs.getString("Client"),
                        rs.getString("Room"),
                        bookingDate,
                        bookingEndDate,
                        rs.getString("SeatingConfig")
                };
                model.addRow(row);

                totalBookings++;
                String room = rs.getString("Room");
                spaceUsage.put(room, spaceUsage.getOrDefault(room, 0) + 1);

                if (bookingDate != null && bookingEndDate != null) {
                    totalDuration += (bookingEndDate.getTime() - bookingDate.getTime()) / (1000 * 60 * 60 * 24);
                }

                Time startTime = rs.getTime("StartTime");
                if (startTime != null) {
                    int hour = startTime.toLocalTime().getHour();
                    hourDistribution.put(hour, hourDistribution.getOrDefault(hour, 0) + 1);
                }
            }

            // Find peak booking time
            String peakTime = "N/A";
            if (!hourDistribution.isEmpty()) {
                int peakHour = hourDistribution.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(-1);

                if (peakHour != -1) {
                    String period = peakHour >= 12 ? "PM" : "AM";
                    int displayHour = peakHour > 12 ? peakHour - 12 : (peakHour == 0 ? 12 : peakHour);
                    peakTime = String.format("%d:00 %s", displayHour, period);
                }
            }

            // Calculate usage percentage for the selected period
            double periodUsage;
            if (!isAllTime && startDateSpinner.getValue() != null && endDateSpinner.getValue() != null) {
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.ofInstant(((java.util.Date) startDateSpinner.getValue()).toInstant(), java.time.ZoneId.systemDefault()),
                    LocalDate.ofInstant(((java.util.Date) endDateSpinner.getValue()).toInstant(), java.time.ZoneId.systemDefault())
                );
                periodUsage = daysBetween > 0 ? (double) totalBookings / (daysBetween + 1) * 10 : totalBookings * 10;
            } else {
                periodUsage = totalBookings > 0 ? (double) totalBookings / 30 * 10 : 0;
            }

            updateSummaryCards(summaryPanel, totalBookings, spaceUsage, totalDuration, peakTime, periodUsage,
                isAllTime ? "All Time" : "Selected Period");

        } catch (SQLException e) {
            handleDatabaseError(model, summaryPanel, e);
        }
    }

    private void updateSummaryCards(JPanel summaryPanel, int totalBookings, Map<String, Integer> spaceUsage, 
            long totalDuration, String peakTime, double periodUsage, String period) {
        ((JLabel) ((JPanel) summaryPanel.getComponent(0)).getComponent(2)).setText(String.valueOf(totalBookings));

        String mostPopularSpace = spaceUsage.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        ((JLabel) ((JPanel) summaryPanel.getComponent(1)).getComponent(2)).setText(mostPopularSpace);

        double avgDuration = totalBookings > 0 ? (double) totalDuration / totalBookings : 0;
        ((JLabel) ((JPanel) summaryPanel.getComponent(2)).getComponent(2))
                .setText(String.format("%.1f days", avgDuration));

        ((JLabel) ((JPanel) summaryPanel.getComponent(3)).getComponent(2)).setText(peakTime);

        ((JLabel) ((JPanel) summaryPanel.getComponent(4)).getComponent(0)).setText(period + " Usage");
        ((JLabel) ((JPanel) summaryPanel.getComponent(4)).getComponent(2))
                .setText(String.format("%.1f%%", periodUsage));

        summaryPanel.getParent().repaint();
    }

    private void handleDatabaseError(DefaultTableModel model, JPanel summaryPanel, SQLException e) {
        model.setRowCount(0);
        
        updateSummaryCards(summaryPanel, 0, new HashMap<>(), 0, "N/A", 0, "Period");
        
        String errorMessage = "Error retrieving venue usage data: " + e.getMessage();
        if (e.getCause() instanceof java.net.UnknownHostException) {
            errorMessage = "Cannot connect to database. Please ensure you are connected to the City VPN.";
        }
        
        JOptionPane.showMessageDialog(null, errorMessage, "Database Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    private JPanel createDailySheetsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(background);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBackground(background);
        
        JLabel dateLabel = new JLabel("Select Date:");
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setFont(new Font("TimesRoman", Font.BOLD, 13));
        controlPanel.add(dateLabel);

        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateModel.setValue(new java.util.Date()); // Set to current date
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd-MM-yyyy");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setPreferredSize(new Dimension(120, 25));
        controlPanel.add(dateSpinner);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("TimesRoman", Font.BOLD, 13));
        refreshButton.setBackground(new Color(30, 50, 55));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        controlPanel.add(refreshButton);

        // Add availability summary
        JPanel availabilityPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        availabilityPanel.setBackground(background);
        availabilityPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        availabilityPanel.add(createSummaryCard("Total Spaces", "10"));
        availabilityPanel.add(createSummaryCard("Spaces In Use", "0"));
        availabilityPanel.add(createSummaryCard("Available Spaces", "10"));
        availabilityPanel.add(createSummaryCard("Configuration Changes", "0"));

        // Create main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBackground(background);
        contentPanel.add(controlPanel, BorderLayout.NORTH);
        contentPanel.add(availabilityPanel, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.NORTH);

        // Create table
        String[] columns = {
            "Time", "Room", "Status", "Client", "Event", "Current Config", "Required Config", 
            "Setup Time", "Notes"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        
        // Create popup panel for hover details
        JPanel popupPanel = new JPanel();
        popupPanel.setLayout(new BoxLayout(popupPanel, BoxLayout.Y_AXIS));
        popupPanel.setBackground(new Color(30, 50, 55));
        popupPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(45, 45, 45), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Create labels for popup content
        JLabel[] detailLabels = new JLabel[columns.length];
        for (int i = 0; i < columns.length; i++) {
            detailLabels[i] = new JLabel();
            detailLabels[i].setForeground(Color.WHITE);
            detailLabels[i].setFont(new Font("TimesRoman", Font.PLAIN, 14));
            detailLabels[i].setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
            popupPanel.add(detailLabels[i]);
        }

        JWindow popup = new JWindow();
        popup.setContentPane(popupPanel);
        popup.setOpacity(0.95f);

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    // Update popup content
                    for (int i = 0; i < columns.length; i++) {
                        Object value = table.getValueAt(row, i);
                        String text = columns[i] + ": " + (value != null ? value.toString() : "");
                        detailLabels[i].setText(text);

                        // Style room name if it exists (column index may vary)
                        if (columns[i].equals("Room")) {
                            String room = value != null ? value.toString() : "";
                            Color roomColor = spaceColors.getOrDefault(room, new Color(149, 165, 166));
                            detailLabels[i].setForeground(roomColor);
                        }
                        // Style status if it exists (column index may vary)
                        else if (columns[i].equals("Status")) {
                            String status = value != null ? value.toString() : "";
                            switch (status) {
                                case "Available":
                                    detailLabels[i].setForeground(new Color(46, 125, 50));
                                    break;
                                case "In Use":
                                    detailLabels[i].setForeground(new Color(211, 47, 47));
                                    break;
                                case "Setup Required":
                                    detailLabels[i].setForeground(new Color(255, 152, 0));
                                    break;
                                default:
                                    detailLabels[i].setForeground(Color.WHITE);
                            }
                        }
                        // Style monetary values
                        else if (value != null && text.contains("£")) {
                            detailLabels[i].setForeground(new Color(38, 166, 154));  // Teal color for money
                        }
                        else {
                            detailLabels[i].setForeground(Color.WHITE);
                        }
                    }

                    Point p = e.getPoint();
                    SwingUtilities.convertPointToScreen(p, table);
                    
                    Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getMaximumWindowBounds();
                    
                    int popupX = p.x + 15;
                    int popupY = p.y + 15;
                    
                    if (popupX + popup.getWidth() > screen.width) {
                        popupX = screen.width - popup.getWidth();
                    }
                    if (popupY + popup.getHeight() > screen.height) {
                        popupY = screen.height - popup.getHeight();
                    }
                    
                    popup.setLocation(popupX, popupY);
                    popup.pack();
                    popup.setVisible(true);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                popup.setVisible(false);
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                popup.setVisible(false);
            }
        });

        table.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setGridColor(new Color(45, 45, 45));
        table.setBackground(new Color(25, 40, 45));
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(45, 66, 75));
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        table.getTableHeader().setBackground(new Color(30, 50, 55));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("TimesRoman", Font.BOLD, 13));
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));

        table.getColumnModel().getColumn(1).setCellRenderer(new RoomColorRenderer());

        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Date) {
                    setText(dateFormat.format(value));
                }
                if (!isSelected) {
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                }
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, table.getGridColor()));
                return c;
            }
        });

        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                }
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, table.getGridColor()));
                return c;
            }
        };
        defaultRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 1 && i != 3) { // Skip Room and Date columns
                table.getColumnModel().getColumn(i).setCellRenderer(defaultRenderer);
            }
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));
        scrollPane.getViewport().setBackground(table.getBackground());
        
        panel.add(scrollPane, BorderLayout.CENTER);

        loadDailySheetsData(model, dateSpinner, availabilityPanel);

        refreshButton.addActionListener(e -> {
            loadDailySheetsData(model, dateSpinner, availabilityPanel);
        });

        return panel;
    }

    private void loadDailySheetsData(DefaultTableModel model, JSpinner dateSpinner, JPanel availabilityPanel) {
        String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
        String user = "in2033t26_a";
        String password = "jLxOPuQ69Mg";

        // Clear existing data
        model.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
            
            String query = "SELECT Room, Client, BookingName, SeatingConfig, Notes, " +
                         "StartTime, EndTime, PaymentStatus " +
                         "FROM booking WHERE BookingDate = ? " +
                         "ORDER BY StartTime, Room";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setDate(1, new java.sql.Date(selectedDate.getTime()));
                
                ResultSet rs = stmt.executeQuery();
                
                int spacesInUse = 0;
                int configChanges = 0;
                Set<String> usedRooms = new HashSet<>();

                for (String room : spaceColors.keySet()) {
                    model.addRow(new Object[]{
                        "09:00", room, "Available", "", "", "Standard", "", "", ""
                    });
                }

            while (rs.next()) {
                    String room = rs.getString("Room");
                    String currentConfig = rs.getString("SeatingConfig");
                    Time startTime = rs.getTime("StartTime");
                    
                    for (int i = 0; i < model.getRowCount(); i++) {
                        if (model.getValueAt(i, 1).equals(room)) {
                            model.setValueAt(startTime.toString(), i, 0);
                            model.setValueAt("In Use", i, 2);
                            model.setValueAt(rs.getString("Client"), i, 3);
                            model.setValueAt(rs.getString("BookingName"), i, 4);
                            model.setValueAt(currentConfig, i, 5);
                            model.setValueAt(rs.getString("SeatingConfig"), i, 6);
                            model.setValueAt(rs.getTime("StartTime").toString(), i, 7);
                            model.setValueAt(rs.getString("Notes"), i, 8);
                            break;
                        }
                    }

                    if (!usedRooms.contains(room)) {
                        spacesInUse++;
                        usedRooms.add(room);
                    }

                    String requiredConfig = rs.getString("SeatingConfig");
                    if (!requiredConfig.equals(currentConfig)) {
                        configChanges++;
                    }
                }

                updateAvailabilitySummary(availabilityPanel, spaceColors.size(),
                    spacesInUse, configChanges);

            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Error retrieving daily sheets data: " + e.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAvailabilitySummary(JPanel availabilityPanel, int totalSpaces, 
            int spacesInUse, int configChanges) {
        ((JLabel) ((JPanel) availabilityPanel.getComponent(0)).getComponent(2))
            .setText(String.valueOf(totalSpaces));

        ((JLabel) ((JPanel) availabilityPanel.getComponent(1)).getComponent(2))
            .setText(String.valueOf(spacesInUse));

        ((JLabel) ((JPanel) availabilityPanel.getComponent(2)).getComponent(2))
            .setText(String.valueOf(totalSpaces - spacesInUse));

        ((JLabel) ((JPanel) availabilityPanel.getComponent(3)).getComponent(2))
            .setText(String.valueOf(configChanges));
    }

    private JPanel createFinanceTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(background);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(background);

        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        summaryPanel.setBackground(background);
        summaryPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        summaryPanel.add(createSummaryCard("Total Revenue", "£0"));
        summaryPanel.add(createSummaryCard("Ticket Sales", "£0"));
        summaryPanel.add(createSummaryCard("Client Payouts", "£0"));
        summaryPanel.add(createSummaryCard("Net Income", "£0"));

        mainPanel.add(summaryPanel);

        JPanel categoryPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        categoryPanel.setBackground(background);
        categoryPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        categoryPanel.add(createSummaryCard("Meeting Rooms Income", "£0"));
        categoryPanel.add(createSummaryCard("Performance Spaces Income", "£0"));
        categoryPanel.add(createSummaryCard("Rehearsal Space Income", "£0"));
        categoryPanel.add(createSummaryCard("Entire Venue Income", "£0"));

        mainPanel.add(categoryPanel);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(background);
        
        JLabel spaceFilterLabel = new JLabel("Space:");
        spaceFilterLabel.setForeground(Color.WHITE);
        spaceFilterLabel.setFont(new Font("TimesRoman", Font.BOLD, 13));
        filterPanel.add(spaceFilterLabel);

        String[] spaces = new String[] {
            "All Spaces",
            // Meeting Rooms
            "The Green Room",
            "Brontë Boardroom",
            "Dickens Den",
            "Poe Parlor",
            "Globe Room",
            "Chekhov Chamber",
            // Performance Spaces
            "Main Hall",
            "Small Hall",
            // Rehearsal Space
            "Rehearsal Space",
            // Entire Venue
            "Entire Venue"
        };

        JComboBox<String> spaceFilter = new JComboBox<>(spaces);
        spaceFilter.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        spaceFilter.setBackground(new Color(30, 50, 55));
        spaceFilter.setForeground(Color.WHITE);
        ((JLabel)spaceFilter.getRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        filterPanel.add(spaceFilter);

        filterPanel.add(Box.createHorizontalStrut(20));
        
        JLabel dateFilterLabel = new JLabel("Date Range:");
        dateFilterLabel.setForeground(Color.WHITE);
        dateFilterLabel.setFont(new Font("TimesRoman", Font.BOLD, 13));
        filterPanel.add(dateFilterLabel);

        SpinnerDateModel startDateModel = new SpinnerDateModel();
        JSpinner startDateSpinner = new JSpinner(startDateModel);
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(startDateSpinner, "dd-MM-yyyy");
        startDateSpinner.setEditor(startDateEditor);
        startDateSpinner.setPreferredSize(new Dimension(120, 25));
        
        SpinnerDateModel endDateModel = new SpinnerDateModel();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        endDateModel.setValue(cal.getTime());
        JSpinner endDateSpinner = new JSpinner(endDateModel);
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "dd-MM-yyyy");
        endDateSpinner.setEditor(endDateEditor);
        endDateSpinner.setPreferredSize(new Dimension(120, 25));

        startDateSpinner.addChangeListener(e -> {
            java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
            java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
            if (startDate.after(endDate)) {
                endDateSpinner.setValue(startDate);
            }
        });

        filterPanel.add(startDateSpinner);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(endDateSpinner);

        JButton applyFilter = new JButton("Apply Filter");
        applyFilter.setFont(new Font("TimesRoman", Font.BOLD, 13));
        applyFilter.setBackground(new Color(30, 50, 55));
        applyFilter.setForeground(Color.WHITE);
        applyFilter.setFocusPainted(false);
        filterPanel.add(applyFilter);

        mainPanel.add(filterPanel);

        String[] columns = {
            "Booking Name", "Client", "Room", "Date", "Duration", 
            "Hire Fee (£)", "Ticket Revenue (£)", "Payable to Client (£)", 
            "Net Income (£)", "Payment Status"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        
        table.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setGridColor(new Color(45, 45, 45));
        table.setBackground(new Color(25, 40, 45));
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(45, 66, 75));
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        table.getTableHeader().setBackground(new Color(30, 50, 55));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("TimesRoman", Font.BOLD, 13));
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));

        table.getColumnModel().getColumn(2).setCellRenderer(new RoomColorRenderer());

        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Date) {
                    setText(dateFormat.format(value));
                }
                if (!isSelected) {
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                }
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, table.getGridColor()));
                return c;
            }
        });

        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                }
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, table.getGridColor()));
                return c;
            }
        };
        defaultRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 2 && i != 3) { // Skip Room and Date columns
                table.getColumnModel().getColumn(i).setCellRenderer(defaultRenderer);
            }
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));
        scrollPane.getViewport().setBackground(table.getBackground());
        
        mainPanel.add(scrollPane);

        panel.add(mainPanel, BorderLayout.CENTER);

        loadFinancialData(model, startDateSpinner, endDateSpinner, summaryPanel, categoryPanel, spaceFilter);

        createDetailPopup(columns, table);

        applyFilter.addActionListener(e -> {
            loadFinancialData(model, startDateSpinner, endDateSpinner, summaryPanel, categoryPanel, spaceFilter);
        });

        return panel;
    }

    private void loadFinancialData(DefaultTableModel model, JSpinner startDateSpinner, 
            JSpinner endDateSpinner, JPanel summaryPanel, JPanel categoryPanel, JComboBox<String> spaceFilter) {
        String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
        String user = "in2033t26_a";
        String password = "jLxOPuQ69Mg";

        model.setRowCount(0);

        try {
            JDBC jdbc = new JDBC();
            
            java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
            java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
            String selectedSpace = (String) spaceFilter.getSelectedItem();

            StringBuilder queryBuilder = new StringBuilder(
                "SELECT b.BookingName, b.Client, b.Room, b.BookingDate, " +
                "b.BookingEndDate, b.PaymentStatus, " +
                "DATEDIFF(b.BookingEndDate, b.BookingDate) + 1 as Duration " +
                "FROM booking b " +
                "WHERE b.BookingDate >= ? AND b.BookingEndDate <= ?"
            );

            if (!"All Spaces".equals(selectedSpace)) {
                queryBuilder.append(" AND b.Room = ?");
            }

            queryBuilder.append(" ORDER BY b.BookingDate, b.Room");

            try (Connection conn = DriverManager.getConnection(url, user, password);
                 PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {

                stmt.setDate(1, new java.sql.Date(startDate.getTime()));
                stmt.setDate(2, new java.sql.Date(endDate.getTime()));

                if (!"All Spaces".equals(selectedSpace)) {
                    stmt.setString(3, selectedSpace);
                }

                double totalRevenue = 0.0;
                double totalHireFees = 0.0;
                double totalTicketSales = 0.0;
                double totalClientPayouts = 0.0;
                double totalNetIncome = 0.0;

                final double[] categoryTotals = new double[4];

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String room = rs.getString("Room");
                    int duration = rs.getInt("Duration");
                    Date bookingDate = rs.getDate("BookingDate");
                    

                    double hireFee = calculateHireFee(room, duration);

                    updateCategoryTotals(room, hireFee, categoryTotals);

                    double ticketRevenue = jdbc.getRevenueBasedOnEvent(rs.getString("BookingName"));

                    double clientPayout = ticketRevenue * 0.7;

                    double netIncome = hireFee + (ticketRevenue - clientPayout);

                    Object[] row = {
                        rs.getString("BookingName"),
                        rs.getString("Client"),
                        room,
                        rs.getDate("BookingDate"),
                        duration + " days",
                        String.format("%.2f", hireFee),
                        String.format("%.2f", ticketRevenue),
                        String.format("%.2f", clientPayout),
                        String.format("%.2f", netIncome),
                        rs.getString("PaymentStatus")
                    };
                    model.addRow(row);

                    totalHireFees += hireFee;
                    totalTicketSales += ticketRevenue;
                    totalClientPayouts += clientPayout;
                    totalNetIncome += netIncome;
                }
                
                totalRevenue = totalHireFees + totalTicketSales;

                updateFinancialSummary(summaryPanel, totalRevenue, totalHireFees, 
                    totalTicketSales, totalClientPayouts, totalNetIncome);

                updateCategorySummary(categoryPanel, categoryTotals[0], categoryTotals[1], 
                    categoryTotals[2], categoryTotals[3]);

            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Error retrieving financial data: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCategoryTotals(String room, double hireFee, double[] categoryTotals) {
        if (room.equals("Entire Venue")) {
            categoryTotals[3] += hireFee;
        } else if (room.equals("Main Hall") || room.equals("Small Hall")) {
            categoryTotals[1] += hireFee;
        } else if (room.equals("Rehearsal Space")) {
            categoryTotals[2] += hireFee;
        } else if (room.equals("The Green Room") || room.equals("Brontë Boardroom") ||
                  room.equals("Dickens Den") || room.equals("Poe Parlor") ||
                  room.equals("Globe Room") || room.equals("Chekhov Chamber")) {
            categoryTotals[0] += hireFee;
        }
    }

    private void updateCategorySummary(JPanel categoryPanel, double meetingRoomsIncome,
            double performanceSpacesIncome, double rehearsalSpaceIncome, double entireVenueIncome) {

        ((JLabel) ((JPanel) categoryPanel.getComponent(0)).getComponent(2))
            .setText(String.format("£%.2f", meetingRoomsIncome));


        ((JLabel) ((JPanel) categoryPanel.getComponent(1)).getComponent(2))
            .setText(String.format("£%.2f", performanceSpacesIncome));


        ((JLabel) ((JPanel) categoryPanel.getComponent(2)).getComponent(2))
            .setText(String.format("£%.2f", rehearsalSpaceIncome));

        ((JLabel) ((JPanel) categoryPanel.getComponent(3)).getComponent(2))
            .setText(String.format("£%.2f", entireVenueIncome));
    }

    private double calculateHireFee(String room, int duration) {
        Map<String, Double> weeklyRates = new HashMap<>() {{
            put("The Green Room", 600.0);
            put("Brontë Boardroom", 900.0);
            put("Dickens Den", 700.0);
            put("Poe Parlor", 800.0);
            put("Globe Room", 1100.0);
            put("Chekhov Chamber", 850.0);
            put("Main Hall", 2500.0);
            put("Small Hall", 1500.0);
            put("Rehearsal Space", 1000.0);
            put("Entire Venue", 5000.0);
        }};

        double weeklyRate = weeklyRates.getOrDefault(room, 0.0);
        double dailyRate = weeklyRate / 7.0;

        if (duration >= 7) {
            int weeks = duration / 7;
            int remainingDays = duration % 7;
            return (weeks * weeklyRate) + (remainingDays * dailyRate);
        } else {
            return duration * dailyRate;
        }
    }

    private void updateFinancialSummary(JPanel summaryPanel, double totalRevenue,
            double totalHireFees, double totalTicketSales, double totalClientPayouts,
            double totalNetIncome) {
        ((JLabel) ((JPanel) summaryPanel.getComponent(0)).getComponent(2))
            .setText(String.format("£%.2f", totalRevenue));

        ((JLabel) ((JPanel) summaryPanel.getComponent(1)).getComponent(2))
            .setText(String.format("£%.2f", totalTicketSales));

        ((JLabel) ((JPanel) summaryPanel.getComponent(2)).getComponent(2))
            .setText(String.format("£%.2f", totalClientPayouts));

        ((JLabel) ((JPanel) summaryPanel.getComponent(3)).getComponent(2))
            .setText(String.format("£%.2f", totalNetIncome));
    }

    private JPanel createMonthRevTab() throws SQLException, ClassNotFoundException {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(background);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBackground(background);

        JLabel monthLabel = new JLabel("Month:");
        monthLabel.setForeground(Color.WHITE);
        monthLabel.setFont(new Font("TimesRoman", Font.BOLD, 13));
        controlPanel.add(monthLabel);

        SpinnerNumberModel monthModel = new SpinnerNumberModel(4, 1, 12, 1);
        JSpinner monthSpinner = new JSpinner(monthModel);
        monthSpinner.setPreferredSize(new Dimension(60, 25));
        monthSpinner.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        controlPanel.add(monthSpinner);

        JLabel yearLabel = new JLabel("Year:");
        yearLabel.setForeground(Color.WHITE);
        yearLabel.setFont(new Font("TimesRoman", Font.BOLD, 13));
        controlPanel.add(yearLabel);

        SpinnerNumberModel yearModel = new SpinnerNumberModel(2025, 2000, 2100, 1);
        JSpinner yearSpinner = new JSpinner(yearModel);
        JSpinner.NumberEditor yearEditor = new JSpinner.NumberEditor(yearSpinner, "#");
        yearSpinner.setEditor(yearEditor);
        yearEditor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        yearSpinner.setPreferredSize(new Dimension(80, 25));
        yearSpinner.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        controlPanel.add(yearSpinner);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("TimesRoman", Font.BOLD, 13));
        refreshButton.setBackground(new Color(30, 50, 55));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        controlPanel.add(refreshButton);

        panel.add(controlPanel, BorderLayout.NORTH);

        String[] columns = {
                "Last Sale Date", "Revenue", "Tickets Sold", "First Sale Date"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);

        table.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setGridColor(new Color(45, 45, 45));
        table.setBackground(new Color(25, 40, 45));
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(45, 66, 75));
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        table.getTableHeader().setBackground(new Color(30, 50, 55));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("TimesRoman", Font.BOLD, 13));
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));
        scrollPane.getViewport().setBackground(table.getBackground());

        panel.add(scrollPane, BorderLayout.CENTER);

        refreshButton.addActionListener(e -> {
            int year = (Integer) yearSpinner.getValue();
            int month = (Integer) monthSpinner.getValue();
            try {
                loadMonthlyRevenue(model, year, month);
            } catch (SQLException | ClassNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel,
                        "Error loading monthly revenue data: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        loadMonthlyRevenue(model, (Integer) yearSpinner.getValue(), (Integer) monthSpinner.getValue());

        return panel;
    }

    private void loadMonthlyRevenue(DefaultTableModel model, int year, int month) throws SQLException, ClassNotFoundException {
        try {
            JDBC jdbc = new JDBC();
            model.setRowCount(0);

            Map<String, Object> monthlyRevenueReport = jdbc.getMonthlyRevenueReport(year, month);

            Object[] row = {
                    monthlyRevenueReport.get("last_sale").toString(),
                    String.format("£%.2f", monthlyRevenueReport.get("revenue")),
                    monthlyRevenueReport.get("tickets_sold"),
                    monthlyRevenueReport.get("first_sale").toString()
            };

            System.out.println("Monthly Revenue for " + month + "/" + year + ":");
            for (Map.Entry<String, Object> entry : monthlyRevenueReport.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

            model.addRow(row);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving Monthly Revenue" +
                    "Data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
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

    private void styleTable(JTable table) {
        table.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("TimesRoman", Font.BOLD, 14));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
    }


class RoomColorRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null || value.toString().isEmpty()) {
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

        String room = value.toString();
        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (isSelected) {
            cell.setBackground(table.getSelectionBackground());
            cell.setForeground(table.getSelectionForeground());
        } else {
            String normalizedRoom = normalizeRoomName(room);
            Color bgColor = spaceColors.getOrDefault(normalizedRoom, new Color(149, 165, 166));
            cell.setBackground(bgColor);

            double luminance = (0.299 * bgColor.getRed() + 0.587 * bgColor.getGreen() + 0.114 * bgColor.getBlue()) / 255;
            cell.setForeground(luminance > 0.5 ? Color.BLACK : Color.WHITE);
        }

        return cell;
    }

    private String normalizeRoomName(String room) {
        return room.replace("ë", "e")
                  .replace("\n", " ")
                  .trim();
    }
}


    private String normalizeRoomName(String room) {
        return room.replace("ë", "e")  // Replace ë with e
                  .replace("\n", " ")  // Replace newlines with spaces
                  .trim();            // Remove extra spaces
    }


    private JWindow createDetailPopup(String[] columns, JTable table) {
        JPanel popupPanel = new JPanel();
        popupPanel.setLayout(new BoxLayout(popupPanel, BoxLayout.Y_AXIS));
        popupPanel.setBackground(new Color(30, 50, 55));
        popupPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(45, 45, 45), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));


        JLabel[] detailLabels = new JLabel[columns.length];
        for (int i = 0; i < columns.length; i++) {
            detailLabels[i] = new JLabel();
            detailLabels[i].setForeground(Color.WHITE);
            detailLabels[i].setFont(new Font("TimesRoman", Font.PLAIN, 13));
            detailLabels[i].setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
            popupPanel.add(detailLabels[i]);
        }




        JWindow popup = new JWindow();
        popup.setContentPane(popupPanel);
        popup.setOpacity(0.95f);

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    for (int i = 0; i < columns.length; i++) {
                        Object value = table.getValueAt(row, i);
                        String text = columns[i] + ": " + (value != null ? value.toString() : "");
                        detailLabels[i].setText(text);

                        if (columns[i].equals("Room")) {
                            String room = value != null ? value.toString() : "";
                            Color roomColor = spaceColors.getOrDefault(room, new Color(149, 165, 166));
                            detailLabels[i].setForeground(roomColor);
                        }
                        else if (value != null && text.contains("£")) {
                            detailLabels[i].setForeground(new Color(38, 166, 154));  // Teal color for money
                        }
                        else {
                            detailLabels[i].setForeground(Color.WHITE);
                        }
                    }

                    Point p = e.getPoint();
                    SwingUtilities.convertPointToScreen(p, table);
                    
                    Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getMaximumWindowBounds();
                    
                    int popupX = p.x + 15;
                    int popupY = p.y + 15;
                    
                    if (popupX + popup.getWidth() > screen.width) {
                        popupX = screen.width - popup.getWidth();
                    }
                    if (popupY + popup.getHeight() > screen.height) {
                        popupY = screen.height - popup.getHeight();
                    }
                    
                    popup.setLocation(popupX, popupY);
                    popup.pack();
                    popup.setVisible(true);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                popup.setVisible(false);
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                popup.setVisible(false);
            }
        });

        return popup;
    }
}
