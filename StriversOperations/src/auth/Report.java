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

    private JCheckBox allTimeCheckbox;
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
        setTitle("Lancaster's Music Hall Software");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setResizable(false);
        setLocationRelativeTo(null);

        //Main content pane
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(background);
        setContentPane(contentPane);

        //Header panel: contains settings and title
        contentPane.add(createHeaderPanel(), BorderLayout.NORTH);

        //Tabs for reports
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("TimesRoman", Font.BOLD, fontSize));

        tabbedPane.addTab("Venue Usage", createVenueUsageTab());
        tabbedPane.addTab("Daily Sheets", createDailySheetsTab());
        tabbedPane.addTab("Financial Summary", createFinanceTab());
        tabbedPane.addTab("Monthly Revenue", createMonthRevTab());

        contentPane.add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Creates the top section with Settings and Title.
     */
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

        //Settings button
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

        // Title Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(background);
        JLabel titleLabel = new JLabel("Reports");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 32));
        titlePanel.add(titleLabel);

        // Stack both into the header container
        headerContainer.add(topBar);
        headerContainer.add(titlePanel);

        return headerContainer;
    }

    // // Add a helper method to get the display name
    // private String getDisplayName(String dbName) {
    //     if (dbName == null) return "null";
        
    //     // Normalize room name
    //     String normalized = dbName.replace("ë", "e")  // Replace ë with e
    //                               .replace("\n", " ")  // Replace newlines with spaces
    //                               .trim();            // Remove extra spaces
        
    //     // Map of display names if needed
    //     switch (normalized) {
    //         case "Brontë Boardroom": return "Brontë Boardroom";
    //         default: return normalized;
    //     }
    // }

    //Tab 1: Venue usage report across dates/spaces/bookings
    private JPanel createVenueUsageTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(background);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create a top panel for summary statistics
        JPanel summaryPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        summaryPanel.setBackground(background);
        summaryPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Add summary cards
        summaryPanel.add(createSummaryCard("Total Bookings", "0"));
        summaryPanel.add(createSummaryCard("Most Popular Space", "Loading..."));
        summaryPanel.add(createSummaryCard("Average Booking Duration", "0 days"));
        summaryPanel.add(createSummaryCard("Peak Booking Time", "Loading..."));
        summaryPanel.add(createSummaryCard("Current Month Usage", "0%"));

        panel.add(summaryPanel, BorderLayout.NORTH);

        // Create center panel with chart and table side by side
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        centerPanel.setBackground(background);

        // Create a wrapper panel for the chart
        JPanel chartWrapper = new JPanel(new BorderLayout());
        chartWrapper.setBackground(background);
        chartWrapper.add(createSpaceUsageChart(), BorderLayout.CENTER);
        centerPanel.add(chartWrapper);

        // Create table panel with filter controls
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setBackground(background);

        // Create main filter panel
        JPanel mainFilterPanel = new JPanel(new BorderLayout());
        mainFilterPanel.setBackground(background);
        mainFilterPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Space filter panel
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

        // Add date filter panel
        JPanel dateFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dateFilterPanel.setBackground(background);

        // Date range controls
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

        // Combine filter panels
        JPanel combinedFilterPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        combinedFilterPanel.setBackground(background);
        combinedFilterPanel.add(spaceFilterPanel);
        combinedFilterPanel.add(dateFilterPanel);

        mainFilterPanel.add(combinedFilterPanel, BorderLayout.NORTH);
        tablePanel.add(mainFilterPanel, BorderLayout.NORTH);

        // Create and add the table first
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

        // Style the table header
        table.getTableHeader().setBackground(new Color(30, 50, 55));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("TimesRoman", Font.BOLD, 13));
        
        // Set table row height and font
        table.setRowHeight(28);
        table.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        table.setGridColor(new Color(45, 45, 45));
        table.setBackground(new Color(25, 40, 45));
        table.setForeground(Color.WHITE);
        
        // Set selection colors
        table.setSelectionBackground(new Color(45, 66, 75));
        table.setSelectionForeground(Color.WHITE);

        // Apply color renderer to Room column
        table.getColumnModel().getColumn(2).setCellRenderer(new RoomColorRenderer());

        // Create a custom scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(background);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45), 1));

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(tablePanel);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Add change listener to start date spinner
        startDateSpinner.addChangeListener(e -> {
            java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
            java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
            
            if (startDate.after(endDate)) {
                // If start date is after end date, set end date to start date
                endDateSpinner.setValue(startDate);
            }
            
            // Refresh data
            loadVenueUsageData(model, spaceFilter, startDateSpinner, endDateSpinner, summaryPanel, false);
        });

        // Add change listener to end date spinner
        endDateSpinner.addChangeListener(e -> {
            java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
            java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
            
            if (endDate.before(startDate)) {
                // If end date is before start date, set it to start date
                endDateSpinner.setValue(startDate);
            }
            
            // Refresh data
            loadVenueUsageData(model, spaceFilter, startDateSpinner, endDateSpinner, summaryPanel, false);
        });

        // Add confirm button listener
        confirmDates.addActionListener(e -> {
            loadVenueUsageData(model, spaceFilter, startDateSpinner, endDateSpinner, summaryPanel, false);
        });

        // Space filter listener
        spaceFilter.addActionListener(e -> {
            loadVenueUsageData(model, spaceFilter, startDateSpinner, endDateSpinner, summaryPanel, false);
        });

        // Load initial data with current date range
        loadVenueUsageData(model, spaceFilter, startDateSpinner, endDateSpinner, summaryPanel, false);

        // Add popup functionality
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
        // Get space usage data
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

        // Set up graphics
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Calculate dimensions
        int titleHeight = 30;
        int legendWidth = 143;
        int padding = 19;

        // Available space for the pie chart
        int availableWidth = getWidth() - (padding * 2) - legendWidth;
        int availableHeight = getHeight() - (padding * 2) - titleHeight;
        
        // Calculate pie chart size
        int diameter = Math.min(availableWidth, availableHeight);
        if (diameter > 380) {
            diameter = 380;
        }
        
        // Calculate positions
        int x = padding;
        int y = padding + titleHeight;

        // Draw title
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
        
        // Draw the pie chart
        int startAngle = 0;
        for (Map.Entry<String, Integer> entry : spaceUsage.entrySet()) {
            int arcAngle = (int) Math.round(360.0 * entry.getValue() / total);
            String room = entry.getKey();
            // Use the consistent color from ROOM_COLORS map
            g.setColor(spaceColors.getOrDefault(room, new Color(149, 165, 166))); // Default to gray if room not found
            g.fillArc(x, y, diameter, diameter, startAngle, arcAngle);
            startAngle += arcAngle;
        }

        // Draw legend
        int legendX = x + diameter + 14;
        int legendY = y;
        int legendSpacing = 21;
        int i = 0;

        g.setFont(new Font("TimesRoman", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        
        for (Map.Entry<String, Integer> entry : spaceUsage.entrySet()) {
            if (legendY + (i * legendSpacing) + 24 > getHeight()) {
                legendX += 152;
                i = 0;
            }
            
            String room = entry.getKey();
            g.setColor(spaceColors.getOrDefault(room, new Color(149, 165, 166)));
            g.fillRect(legendX, legendY + (i * legendSpacing), 10, 10);
            
            g.setColor(Color.WHITE);
            String normalizedRoom = normalizeRoomName(room);
            String displayName = normalizedRoom;
            String legendText = String.format("%s (%d)", displayName, entry.getValue());
            if (fm.stringWidth(legendText) > 133) {
                while (fm.stringWidth(legendText + "...") > 133 && legendText.length() > 0) {
                    legendText = legendText.substring(0, legendText.length() - 1);
                }
                legendText += "...";
            }
            g.drawString(legendText, legendX + 15, legendY + (i * legendSpacing) + 9);
            i++;
        }
    }

    private void loadVenueUsageData(DefaultTableModel model, JComboBox<String> spaceFilter, 
            JSpinner startDateSpinner, JSpinner endDateSpinner, JPanel summaryPanel, boolean isAllTime) {
        String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
        String user = "in2033t26_a";
        String password = "jLxOPuQ69Mg";

        String selectedSpace = (String) spaceFilter.getSelectedItem();
        
        // Clear existing data
        model.setRowCount(0);
        
        // Build query with date filter
        StringBuilder queryBuilder = new StringBuilder(
            "SELECT BookingName, Client, Room, BookingDate, BookingEndDate, SeatingConfig, StartTime FROM booking WHERE 1=1"
        );
        
        // Add date filter if not all time
        if (!isAllTime) {
            queryBuilder.append(" AND BookingDate >= ? AND BookingEndDate <= ?");
        }
        
        // Add space filter if specific space selected
        if (selectedSpace != null && !selectedSpace.equals("All Spaces")) {
            queryBuilder.append(" AND Room = ?");
        }
        
        // Add order clause
        queryBuilder.append(" ORDER BY BookingDate, StartTime");
        
        String query = queryBuilder.toString();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int paramIndex = 1;
            
            // Set date parameters if not all time
            if (!isAllTime) {
                java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
                java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
                
                // Debug print
                System.out.println("Filtering dates - Start: " + startDate + ", End: " + endDate);
                
                // Set date parameters
                stmt.setDate(paramIndex++, new java.sql.Date(startDate.getTime()));
                stmt.setDate(paramIndex++, new java.sql.Date(endDate.getTime()));
            }
            
            // Set space parameter if specific space selected
            if (selectedSpace != null && !selectedSpace.equals("All Spaces")) {
                stmt.setString(paramIndex, selectedSpace);
            }

            // Debug print the final query with parameters
            System.out.println("Executing query: " + query);

            // Execute query and process results
            ResultSet rs = stmt.executeQuery();
            Map<String, Integer> spaceUsage = new HashMap<>();
            Map<Integer, Integer> hourDistribution = new HashMap<>();
            int totalBookings = 0;
            long totalDuration = 0;

            while (rs.next()) {
                Date bookingDate = rs.getDate("BookingDate");
                Date bookingEndDate = rs.getDate("BookingEndDate");
                
                // Debug print
                System.out.println("Found booking: " + rs.getString("BookingName") + 
                                 " from " + bookingDate + " to " + bookingEndDate);
                
                // Add row to table
                Object[] row = {
                        rs.getString("BookingName"),
                        rs.getString("Client"),
                        rs.getString("Room"),
                        bookingDate,
                        bookingEndDate,
                        rs.getString("SeatingConfig")
                };
                model.addRow(row);

                // Update statistics
                totalBookings++;
                String room = rs.getString("Room");
                spaceUsage.put(room, spaceUsage.getOrDefault(room, 0) + 1);

                // Calculate duration
                if (bookingDate != null && bookingEndDate != null) {
                    totalDuration += (bookingEndDate.getTime() - bookingDate.getTime()) / (1000 * 60 * 60 * 24);
                }

                // Track booking hour distribution
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

            // Update summary cards
            updateSummaryCards(summaryPanel, totalBookings, spaceUsage, totalDuration, peakTime, periodUsage, 
                isAllTime ? "All Time" : "Selected Period");

        } catch (SQLException e) {
            handleDatabaseError(model, summaryPanel, e);
        }
    }

    private void updateSummaryCards(JPanel summaryPanel, int totalBookings, Map<String, Integer> spaceUsage, 
            long totalDuration, String peakTime, double periodUsage, String period) {
        // Update total bookings
        ((JLabel) ((JPanel) summaryPanel.getComponent(0)).getComponent(2)).setText(String.valueOf(totalBookings));

        // Update most popular space
        String mostPopularSpace = spaceUsage.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        ((JLabel) ((JPanel) summaryPanel.getComponent(1)).getComponent(2)).setText(mostPopularSpace);

        // Update average duration
        double avgDuration = totalBookings > 0 ? (double) totalDuration / totalBookings : 0;
        ((JLabel) ((JPanel) summaryPanel.getComponent(2)).getComponent(2))
                .setText(String.format("%.1f days", avgDuration));

        // Update peak booking time
        ((JLabel) ((JPanel) summaryPanel.getComponent(3)).getComponent(2)).setText(peakTime);

        // Update period usage with period label
        ((JLabel) ((JPanel) summaryPanel.getComponent(4)).getComponent(0)).setText(period + " Usage");
        ((JLabel) ((JPanel) summaryPanel.getComponent(4)).getComponent(2))
                .setText(String.format("%.1f%%", periodUsage));

        // Trigger repaint of the parent panel to update the pie chart
        summaryPanel.getParent().repaint();
    }

    private void handleDatabaseError(DefaultTableModel model, JPanel summaryPanel, SQLException e) {
        // Clear the table
        model.setRowCount(0);
        
        // Update summary cards with default values
        updateSummaryCards(summaryPanel, 0, new HashMap<>(), 0, "N/A", 0, "Period");
        
        // Show appropriate error message
        String errorMessage = "Error retrieving venue usage data: " + e.getMessage();
        if (e.getCause() instanceof java.net.UnknownHostException) {
            errorMessage = "Cannot connect to database. Please ensure you are connected to the City VPN.";
        }
        
        JOptionPane.showMessageDialog(null, errorMessage, "Database Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    /* To be its own page
    private JPanel createReviewsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(background);

        String[] columns = {
                "Date", "Event", "Customer", "Seat", "Review", "Stars"
        };

        JTable table = new JTable(new DefaultTableModel(columns, 0));
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

     */

    //Tab 2: Daily run sheet for operational setup
    private JPanel createDailySheetsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(background);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create top control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBackground(background);
        
        // Add date selector
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

        // Add refresh button
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

        // Create popup window
        JWindow popup = new JWindow();
        popup.setContentPane(popupPanel);
        popup.setOpacity(0.95f);

        // Add mouse motion listener for hover effect
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

                    // Position and show popup
                    Point p = e.getPoint();
                    SwingUtilities.convertPointToScreen(p, table);
                    
                    // Adjust position to not go off screen
                    Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getMaximumWindowBounds();
                    
                    int popupX = p.x + 15;
                    int popupY = p.y + 15;
                    
                    // Ensure popup stays within screen bounds
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

        // Hide popup when mouse exits table
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                popup.setVisible(false);
            }
        });

        // Style the table
        table.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setGridColor(new Color(45, 45, 45));
        table.setBackground(new Color(25, 40, 45));
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(45, 66, 75));
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        // Style the table header
        table.getTableHeader().setBackground(new Color(30, 50, 55));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("TimesRoman", Font.BOLD, 13));
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));

        // Apply room color renderer to Room column (index 1)
        table.getColumnModel().getColumn(1).setCellRenderer(new RoomColorRenderer());

        // Apply date formatter to Date column (index 3)
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

        // Apply default renderer to maintain consistent styling for other columns
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
        
        // Apply the default renderer to all columns except Room and Date
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 1 && i != 3) { // Skip Room and Date columns
                table.getColumnModel().getColumn(i).setCellRenderer(defaultRenderer);
            }
        }

        // Create scroll pane with dark styling
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));
        scrollPane.getViewport().setBackground(table.getBackground());
        
        panel.add(scrollPane, BorderLayout.CENTER);

        // Load initial data
        loadDailySheetsData(model, dateSpinner, availabilityPanel);

        // Add refresh button listener
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
            
            // Query to get bookings for the selected date
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

                // First, add all rooms with their initial availability
                for (String room : spaceColors.keySet()) {
                    model.addRow(new Object[]{
                        "09:00", room, "Available", "", "", "Standard", "", "", ""
                    });
                }

                // Then update with actual bookings
            while (rs.next()) {
                    String room = rs.getString("Room");
                    String currentConfig = rs.getString("SeatingConfig");
                    Time startTime = rs.getTime("StartTime");
                    
                    // Update existing row for this room
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

                    // Check if configuration change is needed
                    String requiredConfig = rs.getString("SeatingConfig");
                    if (!requiredConfig.equals(currentConfig)) {
                        configChanges++;
                    }
                }

                // Update availability summary
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
        // Update total spaces
        ((JLabel) ((JPanel) availabilityPanel.getComponent(0)).getComponent(2))
            .setText(String.valueOf(totalSpaces));

        // Update spaces in use
        ((JLabel) ((JPanel) availabilityPanel.getComponent(1)).getComponent(2))
            .setText(String.valueOf(spacesInUse));

        // Update available spaces
        ((JLabel) ((JPanel) availabilityPanel.getComponent(2)).getComponent(2))
            .setText(String.valueOf(totalSpaces - spacesInUse));

        // Update configuration changes
        ((JLabel) ((JPanel) availabilityPanel.getComponent(3)).getComponent(2))
            .setText(String.valueOf(configChanges));
    }

    //Tab 3: Financial summary per booking
    private JPanel createFinanceTab() throws SQLException, ClassNotFoundException {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(background);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create main panel with vertical layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(background);

        // Create a top panel for summary statistics
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        summaryPanel.setBackground(background);
        summaryPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Add summary cards
        summaryPanel.add(createSummaryCard("Total Revenue", "£0"));
        summaryPanel.add(createSummaryCard("Ticket Sales", "£0"));
        summaryPanel.add(createSummaryCard("Client Payouts", "£0"));
        summaryPanel.add(createSummaryCard("Net Income", "£0"));

        mainPanel.add(summaryPanel);

        // Add space category income panel
        JPanel categoryPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        categoryPanel.setBackground(background);
        categoryPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Add category cards
        categoryPanel.add(createSummaryCard("Meeting Rooms Income", "£0"));
        categoryPanel.add(createSummaryCard("Performance Spaces Income", "£0"));
        categoryPanel.add(createSummaryCard("Rehearsal Space Income", "£0"));
        categoryPanel.add(createSummaryCard("Entire Venue Income", "£0"));

        mainPanel.add(categoryPanel);

        // Create filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(background);
        
        // Add space filter
        JLabel spaceFilterLabel = new JLabel("Space:");
        spaceFilterLabel.setForeground(Color.WHITE);
        spaceFilterLabel.setFont(new Font("TimesRoman", Font.BOLD, 13));
        filterPanel.add(spaceFilterLabel);

        // Create space filter dropdown with all spaces
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
        
        // Add date filter
        JLabel dateFilterLabel = new JLabel("Date Range:");
        dateFilterLabel.setForeground(Color.WHITE);
        dateFilterLabel.setFont(new Font("TimesRoman", Font.BOLD, 13));
        filterPanel.add(dateFilterLabel);

        // Add date spinners
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

        // Add change listeners for date validation
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

        // Create table with the same columns as before
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
        
        // Style the table (keep existing styling code)
        table.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setGridColor(new Color(45, 45, 45));
        table.setBackground(new Color(25, 40, 45));
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(45, 66, 75));
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        // Style the table header
        table.getTableHeader().setBackground(new Color(30, 50, 55));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("TimesRoman", Font.BOLD, 13));
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));

        // Apply room color renderer to Room column (index 2)
        table.getColumnModel().getColumn(2).setCellRenderer(new RoomColorRenderer());

        // Apply date formatter to Date column (index 3)
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

        // Apply default renderer to maintain consistent styling for other columns
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
        
        // Apply the default renderer to all columns except Room and Date
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 2 && i != 3) { // Skip Room and Date columns
                table.getColumnModel().getColumn(i).setCellRenderer(defaultRenderer);
            }
        }

        // Create scroll pane with dark styling
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));
        scrollPane.getViewport().setBackground(table.getBackground());
        
        mainPanel.add(scrollPane);

        panel.add(mainPanel, BorderLayout.CENTER);

        // Load initial data with all spaces
        loadFinancialData(model, startDateSpinner, endDateSpinner, summaryPanel, categoryPanel, spaceFilter);

        // Add popup functionality
        createDetailPopup(columns, table);

        // Add filter button listener
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

        // Clear existing data
        model.setRowCount(0);

        try {
            JDBC jdbc = new JDBC();
            
            java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
            java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
            String selectedSpace = (String) spaceFilter.getSelectedItem();

            // Build query with space filter
            StringBuilder queryBuilder = new StringBuilder(
                "SELECT b.BookingName, b.Client, b.Room, b.BookingDate, " +
                "b.BookingEndDate, b.PaymentStatus, " +
                "DATEDIFF(b.BookingEndDate, b.BookingDate) + 1 as Duration " +
                "FROM booking b " +
                "WHERE b.BookingDate >= ? AND b.BookingEndDate <= ?"
            );

            // Add space filter if not "All Spaces"
            if (!"All Spaces".equals(selectedSpace)) {
                queryBuilder.append(" AND b.Room = ?");
            }

            queryBuilder.append(" ORDER BY b.BookingDate, b.Room");

            try (Connection conn = DriverManager.getConnection(url, user, password);
                 PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {

                stmt.setDate(1, new java.sql.Date(startDate.getTime()));
                stmt.setDate(2, new java.sql.Date(endDate.getTime()));

                // Set space parameter if specific space selected
                if (!"All Spaces".equals(selectedSpace)) {
                    stmt.setString(3, selectedSpace);
                }

                double totalRevenue = 0.0;
                double totalHireFees = 0.0;
                double totalTicketSales = 0.0;
                double totalClientPayouts = 0.0;
                double totalNetIncome = 0.0;

                // Initialize category totals array [meetingRooms, performanceSpaces, rehearsal, entireVenue]
                final double[] categoryTotals = new double[4];

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String room = rs.getString("Room");
                    int duration = rs.getInt("Duration");
                    Date bookingDate = rs.getDate("BookingDate");
                    
                    // Calculate hire fee based on room and duration
                    double hireFee = calculateHireFee(room, duration);
                    
                    // Update category totals
                    updateCategoryTotals(room, hireFee, categoryTotals);
                    
                    // Get ticket sales from Box Office API and convert to double
                    double ticketRevenue = (double) jdbc.getRevenueBasedOnEvent(rs.getString("BookingName"));
                    
                    // Calculate client payout (assuming 70% of ticket sales)
                    double clientPayout = ticketRevenue * 0.7;
                    
                    // Calculate net income
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

                    // Update totals
                    totalHireFees += hireFee;
                    totalTicketSales += ticketRevenue;
                    totalClientPayouts += clientPayout;
                    totalNetIncome += netIncome;
                }
                
                totalRevenue = totalHireFees + totalTicketSales;

                // Update summary cards
                updateFinancialSummary(summaryPanel, totalRevenue, totalHireFees, 
                    totalTicketSales, totalClientPayouts, totalNetIncome);

                // Update category summary cards
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
            categoryTotals[3] += hireFee;  // Entire Venue
        } else if (room.equals("Main Hall") || room.equals("Small Hall")) {
            categoryTotals[1] += hireFee;  // Performance Spaces
        } else if (room.equals("Rehearsal Space")) {
            categoryTotals[2] += hireFee;  // Rehearsal Space
        } else if (room.equals("The Green Room") || room.equals("Brontë Boardroom") ||
                  room.equals("Dickens Den") || room.equals("Poe Parlor") ||
                  room.equals("Globe Room") || room.equals("Chekhov Chamber")) {
            categoryTotals[0] += hireFee;  // Meeting Rooms
        }
    }

    private void updateCategorySummary(JPanel categoryPanel, double meetingRoomsIncome,
            double performanceSpacesIncome, double rehearsalSpaceIncome, double entireVenueIncome) {
        // Update meeting rooms income
        ((JLabel) ((JPanel) categoryPanel.getComponent(0)).getComponent(2))
            .setText(String.format("£%.2f", meetingRoomsIncome));

        // Update performance spaces income
        ((JLabel) ((JPanel) categoryPanel.getComponent(1)).getComponent(2))
            .setText(String.format("£%.2f", performanceSpacesIncome));

        // Update rehearsal space income
        ((JLabel) ((JPanel) categoryPanel.getComponent(2)).getComponent(2))
            .setText(String.format("£%.2f", rehearsalSpaceIncome));

        // Update entire venue income
        ((JLabel) ((JPanel) categoryPanel.getComponent(3)).getComponent(2))
            .setText(String.format("£%.2f", entireVenueIncome));
    }

    private double calculateHireFee(String room, int duration) {
        // Base weekly rates from the Booking class
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
        // Update total revenue
        ((JLabel) ((JPanel) summaryPanel.getComponent(0)).getComponent(2))
            .setText(String.format("£%.2f", totalRevenue));

        // Update ticket sales
        ((JLabel) ((JPanel) summaryPanel.getComponent(1)).getComponent(2))
            .setText(String.format("£%.2f", totalTicketSales));

        // Update client payouts
        ((JLabel) ((JPanel) summaryPanel.getComponent(2)).getComponent(2))
            .setText(String.format("£%.2f", totalClientPayouts));

        // Update net income
        ((JLabel) ((JPanel) summaryPanel.getComponent(3)).getComponent(2))
            .setText(String.format("£%.2f", totalNetIncome));
    }

    private JPanel createMonthRevTab() throws SQLException, ClassNotFoundException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(background);

        String[] columns = {
                "Last Sale Date", "Revenue" , "Tickets Sold", "First Sale Date"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        loadMonthlyRevenue(model);

        return panel;
    }

    private void loadMonthlyRevenue(DefaultTableModel model) throws SQLException, ClassNotFoundException {
        try {
            JDBC jdbc = new JDBC();

            // change it to be an input to see previous months
            // try and make it visual



            Map<String, Object> monthlyRevenueReport = jdbc.getMonthlyRevenueReport(2025, 4);

            // Create a single row with all 4 values in the correct order
            Object[] row = {
                    monthlyRevenueReport.get("last_sale").toString(),
                    monthlyRevenueReport.get("revenue"),
                    monthlyRevenueReport.get("tickets_sold"),
                    monthlyRevenueReport.get("first_sale").toString()
            };

            System.out.println("Monthly Revenue: \n");
            for (Map.Entry<String, Object> entry : monthlyRevenueReport.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

            model.addRow(row);


        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving Monthly Revenue" +
                    "Data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
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

    //Shared table styling
    private void styleTable(JTable table) {
        table.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("TimesRoman", Font.BOLD, 14));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
    }

    // Custom Renderer to Color Code Room/Space Columns
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
        return room.replace("ë", "e")  // Replace ë with e
                  .replace("\n", " ")  // Replace newlines with spaces
                  .trim();            // Remove extra spaces
    }
}

    // Place this method within the Report class, outside any inner classes
    private String normalizeRoomName(String room) {
        return room.replace("ë", "e")  // Replace ë with e
                  .replace("\n", " ")  // Replace newlines with spaces
                  .trim();            // Remove extra spaces
    }

    // Add utility method to create and configure the popup window
    private JWindow createDetailPopup(String[] columns, JTable table) {
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
            detailLabels[i].setFont(new Font("TimesRoman", Font.PLAIN, 13));
            detailLabels[i].setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
            popupPanel.add(detailLabels[i]);
        }

        // Create popup window
        JWindow popup = new JWindow();
        popup.setContentPane(popupPanel);
        popup.setOpacity(0.95f);

        // Add mouse motion listener for hover effect
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

                        // Style room name if it exists
                        if (columns[i].equals("Room")) {
                            String room = value != null ? value.toString() : "";
                            Color roomColor = spaceColors.getOrDefault(room, new Color(149, 165, 166));
                            detailLabels[i].setForeground(roomColor);
                        }
                        // Style monetary values
                        else if (value != null && text.contains("£")) {
                            detailLabels[i].setForeground(new Color(38, 166, 154));  // Teal color for money
                        }
                        else {
                            detailLabels[i].setForeground(Color.WHITE);
                        }
                    }

                    // Position and show popup
                    Point p = e.getPoint();
                    SwingUtilities.convertPointToScreen(p, table);
                    
                    // Adjust position to not go off screen
                    Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getMaximumWindowBounds();
                    
                    int popupX = p.x + 15;
                    int popupY = p.y + 15;
                    
                    // Ensure popup stays within screen bounds
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

        // Hide popup when mouse exits table
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                popup.setVisible(false);
            }
        });

        return popup;
    }
}
