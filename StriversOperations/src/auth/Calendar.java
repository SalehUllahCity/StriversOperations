package auth;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * A GUI application for displaying and managing a weekly calendar view of room bookings.
 * Provides functionality for viewing, filtering, and navigating through bookings.
 */
public class Calendar extends JFrame {
    /** UI components for calendar display and interaction */
    private JTable calendarTable;
    private JLabel weekLabel;
    private LocalDate currentWeekStart;
    public final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final Color darkColour = new Color(18, 32, 35);
    private final int fontSize = 18;
    private LocalDate selectedMonthDate = LocalDate.now();
    private JPanel calendarGrid;
    private JLabel monthYearLabel;
    private JTextArea bookingDetails;
    private String currentFilterRoom = null;
    private JLabel filterStatusLabel;

    /** Color mapping for different room types */
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

    /**
     * Main method to launch the Calendar application.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Calendar frame = new Calendar();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Constructs a new Calendar frame and initializes the UI components.
     * Sets up the calendar view and loads initial data.
     */
    public Calendar() {
        setTitle("Lancaster's Music Hall Weekly: Calendar");
        setSize(1440, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(darkColour);
        currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);

        createHeaderPanel();
        createSideMonthView();
        createCalendarTable();
        refreshCalendar();
        calendarTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = calendarTable.getSelectedRow();
                int col = calendarTable.getSelectedColumn();
                if (row >= 0 && col > 0) {
                    Object value = calendarTable.getValueAt(row, col);
                    if (value != null && !value.toString().isEmpty()) {
                        String time = calendarTable.getValueAt(row, 0).toString();
                        LocalDate date = currentWeekStart.plusDays(col - 1);
                        StringBuilder details = new StringBuilder();
                        details.append("Date: ").append(date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                                .append("\nTime: ").append(time)
                                .append("\n\nBookings:");

                        String[] bookings = value.toString().split("\\|");
                        for (String booking : bookings) {
                            // Extract the time range from the booking string
                            String[] parts = booking.trim().split(" ");
                            String timeRange = parts[parts.length-1]; // Get the last part which is the time range
                            String bookingWithoutTime = booking.substring(0, booking.lastIndexOf(" ")).trim();

                            details.append("\n• ").append(bookingWithoutTime)
                                    .append("\n  ").append(timeRange);
                        }
                        bookingDetails.setText(details.toString());
                    } else {
                        bookingDetails.setText("No bookings for this slot");
                    }
                }
            }});
    }

    /**
     * Creates the header panel containing navigation and control buttons.
     */
    private void createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkColour);
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton homeBtn = new JButton("← Home");
        styleTopButton(homeBtn);
        homeBtn.addActionListener(e -> {
            dispose();
            new UserHome().setVisible(true);
        });

        JButton settingsBtn = new JButton("⚙ Settings");
        styleTopButton(settingsBtn);
        settingsBtn.addActionListener(e -> new SettingScreen.SettingsDialog(this).setVisible(true));

        // Add Reset Filter button
        JButton resetFilterBtn = new JButton("Reset Filter");
        styleTopButton(resetFilterBtn);
        resetFilterBtn.addActionListener(e -> {
            currentFilterRoom = null;
            updateFilterStatus();
            refreshCalendar();
        });

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navButtons.setOpaque(false);
        navButtons.add(homeBtn);

        JPanel controlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlButtons.setOpaque(false);
        controlButtons.add(resetFilterBtn);
        controlButtons.add(settingsBtn);

        JPanel weekControl = new JPanel(new BorderLayout());
        weekControl.setOpaque(false);

        JButton prevWeek = new JButton("< Previous");
        styleTopButton(prevWeek);
        prevWeek.addActionListener(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            refreshCalendar();
        });

        JButton nextWeek = new JButton("Next >");
        styleTopButton(nextWeek);
        nextWeek.addActionListener(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            refreshCalendar();
        });

        weekLabel = new JLabel("", JLabel.CENTER);
        weekLabel.setForeground(Color.WHITE);
        weekLabel.setFont(new Font("TimesRoman", Font.BOLD, fontSize));

        weekControl.add(prevWeek, BorderLayout.WEST);
        weekControl.add(weekLabel, BorderLayout.CENTER);
        weekControl.add(nextWeek, BorderLayout.EAST);

        // Create filter status label
        filterStatusLabel = new JLabel("Showing all rooms", JLabel.CENTER);
        filterStatusLabel.setForeground(Color.LIGHT_GRAY);
        filterStatusLabel.setFont(new Font("TimesRoman", Font.ITALIC, 14));

        // Create a panel for week control and filter status
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(weekControl, BorderLayout.CENTER);
        centerPanel.add(filterStatusLabel, BorderLayout.SOUTH);

        header.add(navButtons, BorderLayout.WEST);
        header.add(centerPanel, BorderLayout.CENTER);
        header.add(controlButtons, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    /**
     * Updates the filter status label based on current room filter.
     */
    private void updateFilterStatus() {
        if (currentFilterRoom == null) {
            filterStatusLabel.setText("Showing all rooms");
        } else {
            filterStatusLabel.setText("Filtered by: " + currentFilterRoom);
        }
    }

    /**
     * Creates the side panel containing month view and booking details.
     */
    private void createSideMonthView() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setBackground(darkColour);

        // Create the month view panel
        JPanel monthViewPanel = new JPanel(new BorderLayout());
        monthViewPanel.setBackground(darkColour);
        monthViewPanel.setPreferredSize(new Dimension(250, 220)); // Fixed size for month view

        JPanel monthHeader = new JPanel(new BorderLayout());
        monthHeader.setBackground(darkColour);
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        styleTopButton(prev);
        styleTopButton(next);
        monthYearLabel = new JLabel("", JLabel.CENTER);
        monthYearLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        monthYearLabel.setForeground(Color.WHITE);

        prev.addActionListener(e -> {
            selectedMonthDate = selectedMonthDate.minusMonths(1);
            updateCalendarGrid();
        });

        next.addActionListener(e -> {
            selectedMonthDate = selectedMonthDate.plusMonths(1);
            updateCalendarGrid();
        });

        monthHeader.add(prev, BorderLayout.WEST);
        monthHeader.add(monthYearLabel, BorderLayout.CENTER);
        monthHeader.add(next, BorderLayout.EAST);

        calendarGrid = new JPanel(new GridLayout(0, 7, 5, 5));
        updateCalendarGrid();

        monthViewPanel.add(monthHeader, BorderLayout.NORTH);
        monthViewPanel.add(calendarGrid, BorderLayout.CENTER);

        // Create booking details panel with scroll
        JPanel bookingDetailsPanel = new JPanel(new BorderLayout());
        bookingDetailsPanel.setBackground(darkColour);
        bookingDetailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel detailsTitle = new JLabel("Selected Booking Details");
        detailsTitle.setForeground(Color.WHITE);
        detailsTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        detailsTitle.setHorizontalAlignment(JLabel.CENTER);

        bookingDetails = new JTextArea();
        bookingDetails.setEditable(false);
        bookingDetails.setBackground(darkColour);
        bookingDetails.setForeground(Color.WHITE);
        bookingDetails.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bookingDetails.setLineWrap(true);
        bookingDetails.setWrapStyleWord(true);

        // Add scroll to booking details with increased height
        JScrollPane bookingScroll = new JScrollPane(bookingDetails);
        bookingScroll.setBackground(darkColour);
        bookingScroll.setBorder(null);
        bookingScroll.getViewport().setBackground(darkColour);
        bookingScroll.setPreferredSize(new Dimension(230, 250)); // Increased height

        bookingDetailsPanel.add(detailsTitle, BorderLayout.NORTH);
        bookingDetailsPanel.add(bookingScroll, BorderLayout.CENTER);

        // Create color key panel without scroll
        JPanel colorKeyPanel = new JPanel(new BorderLayout());
        colorKeyPanel.setBackground(darkColour);
        colorKeyPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel keyTitle = new JLabel("Room Colors (Click to Filter)");
        keyTitle.setForeground(Color.WHITE);
        keyTitle.setFont(new Font("SansSerif", Font.BOLD, 14));

        // Create panel for color keys
        JPanel keysPanel = new JPanel();
        keysPanel.setLayout(new BoxLayout(keysPanel, BoxLayout.Y_AXIS));
        keysPanel.setBackground(darkColour);
        keysPanel.add(Box.createVerticalStrut(10));

        // Add color keys for each room
        for (Map.Entry<String, Color> entry : spaceColors.entrySet()) {
            String roomName = entry.getKey();
            Color roomColor = entry.getValue();

            JPanel keyItem = new JPanel(new BorderLayout());
            keyItem.setMaximumSize(new Dimension(230, 25));
            keyItem.setBackground(darkColour);

            JPanel colorBox = new JPanel();
            colorBox.setPreferredSize(new Dimension(20, 20));
            colorBox.setBackground(roomColor);
            colorBox.setBorder(BorderFactory.createLineBorder(Color.WHITE));

            JLabel roomLabel = new JLabel(" " + roomName);
            roomLabel.setForeground(Color.WHITE);
            roomLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

            // Add mouse listeners for filter by room functionality
            roomLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            colorBox.setCursor(new Cursor(Cursor.HAND_CURSOR));

            MouseAdapter roomFilterAdapter = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (currentFilterRoom != null && currentFilterRoom.equals(roomName)) {
                        // If clicking the already filtered room, reset the filter
                        currentFilterRoom = null;
                    } else {
                        currentFilterRoom = roomName;
                    }
                    updateFilterStatus();
                    refreshCalendar();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    roomLabel.setForeground(Color.LIGHT_GRAY);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    roomLabel.setForeground(Color.WHITE);
                }
            };

            roomLabel.addMouseListener(roomFilterAdapter);
            colorBox.addMouseListener(roomFilterAdapter);

            keyItem.add(colorBox, BorderLayout.WEST);
            keyItem.add(roomLabel, BorderLayout.CENTER);
            keyItem.setAlignmentX(Component.LEFT_ALIGNMENT);

            keysPanel.add(keyItem);
            keysPanel.add(Box.createVerticalStrut(5));
        }

        colorKeyPanel.add(keyTitle, BorderLayout.NORTH);
        colorKeyPanel.add(keysPanel, BorderLayout.CENTER);

        // Create a panel for details and key that uses all remaining space
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(darkColour);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Add the booking details with fixed size
        bottomPanel.add(bookingDetailsPanel, BorderLayout.NORTH);

        // Add the color key panel
        bottomPanel.add(colorKeyPanel, BorderLayout.CENTER);

        // Add all panels to the left panel
        leftPanel.add(monthViewPanel, BorderLayout.NORTH);
        leftPanel.add(bottomPanel, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);
    }

    /**
     * Updates the calendar grid to reflect the current month and selected date.
     */
    private void updateCalendarGrid() {
        calendarGrid.removeAll();

        String[] days = {"M", "T", "W", "T", "F", "S", "S"};
        Color bgDark = new Color(18, 32, 35);
        Color fgLight = Color.WHITE;

        calendarGrid.setBackground(bgDark);

        for (String day : days) {
            JLabel label = new JLabel(day, JLabel.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 12));
            label.setOpaque(true);
            label.setBackground(bgDark);
            label.setForeground(fgLight);
            calendarGrid.add(label);
        }

        YearMonth ym = YearMonth.of(selectedMonthDate.getYear(), selectedMonthDate.getMonth());
        monthYearLabel.setText(ym.getMonth().toString().substring(0, 1).toUpperCase() + ym.getMonth().toString().substring(1).toLowerCase() + " " + ym.getYear());

        LocalDate first = ym.atDay(1);
        int shift = (first.getDayOfWeek().getValue() + 6) % 7; // Adjust so Monday = 0, Sunday = 6

        for (int i = 0; i < shift; i++) {
            JLabel empty = new JLabel("");
            empty.setOpaque(true);
            empty.setBackground(bgDark);
            calendarGrid.add(empty);
        }

        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate date = ym.atDay(day);
            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setMargin(new Insets(2, 2, 2, 2));
            dayBtn.setBackground(Color.DARK_GRAY);
            dayBtn.setForeground(fgLight);
            dayBtn.setFocusPainted(false);
            dayBtn.setOpaque(true);
            dayBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            dayBtn.setFont(new Font("SansSerif", Font.BOLD, 12));

            // Apply consistent hover effect
            dayBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    dayBtn.setForeground(Color.LIGHT_GRAY);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    dayBtn.setForeground(fgLight);
                }
            });

            // Sync calendar header after week change
            dayBtn.addActionListener(e -> {
                currentWeekStart = date.with(DayOfWeek.MONDAY);
                refreshCalendar();
                calendarTable.getTableHeader().repaint();
            });

            calendarGrid.add(dayBtn);
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    /**
     * Creates the main calendar table for displaying weekly bookings.
     */
    private void createCalendarTable() {
        String[] columnNames = {"Time", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        calendarTable = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        calendarTable.setRowHeight(60); // Increased height to accommodate multiple bookings
        calendarTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        calendarTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        calendarTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        calendarTable.setGridColor(new Color(200, 200, 200));
        calendarTable.setShowGrid(true);

        // Set column widths
        calendarTable.getColumnModel().getColumn(0).setPreferredWidth(80); // Time column
        for (int i = 1; i <= 7; i++) {
            calendarTable.getColumnModel().getColumn(i).setPreferredWidth(160); // Day columns
        }

        RoomColorRenderer renderer = new RoomColorRenderer();
        calendarTable.setDefaultRenderer(Object.class, renderer);
        calendarTable.setRowSelectionAllowed(false);
        calendarTable.setCellSelectionEnabled(true);

        // Add mouse listener for booking clicks
        calendarTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = calendarTable.rowAtPoint(e.getPoint());
                int col = calendarTable.columnAtPoint(e.getPoint());

                if (row >= 0 && col > 0) {
                    Object value = calendarTable.getValueAt(row, col);
                    if (value != null && !value.toString().isEmpty()) {
                        Rectangle cellRect = calendarTable.getCellRect(row, col, true);
                        Point cellPoint = e.getPoint();
                        cellPoint.translate(-cellRect.x, -cellRect.y);

                        // Get the cell component from renderer
                        Component cellComp = renderer.getTableCellRendererComponent(
                                calendarTable, value, false, false, row, col);

                        if (cellComp instanceof JPanel) {
                            JPanel panel = (JPanel) cellComp;
                            panel.setSize(cellRect.width, cellRect.height);
                            panel.doLayout();

                            // Calculate which booking was clicked
                            int bookingWidth = cellRect.width / panel.getComponentCount();
                            int bookingIndex = cellPoint.x / bookingWidth;

                            if (bookingIndex >= 0 && bookingIndex < panel.getComponentCount()) {
                                String[] bookings = value.toString().split("\\|");
                                if (bookingIndex < bookings.length) {
                                    String bookingDetails = bookings[bookingIndex].trim();

                                    // Format booking details
                                    String time = (String) calendarTable.getValueAt(row, 0);
                                    LocalDate date = currentWeekStart.plusDays(col - 1);
                                    String details = String.format("""
                                        Date: %s
                                        Time: %s
                                        
                                        %s""",
                                            date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                                            time,
                                            bookingDetails
                                    );

                                    // Show popup at click location
                                    Point screenPoint = e.getLocationOnScreen();
                                    renderer.popup.showPopup(details, screenPoint);
                                }
                            }
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(calendarTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        JTableHeader header = calendarTable.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 48));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setVerticalAlignment(JLabel.TOP);
                if (column > 0) {
                    LocalDate dayDate = currentWeekStart.plusDays(column - 1);
                    label.setText("<html><div style='text-align:center;'>" + value + "<br><b>" + dayDate.getDayOfMonth() + "</b></div></html>");
                }
                return label;
            }
        });
    }

    /**
     * Refreshes the calendar display with current week's bookings.
     */
    private void refreshCalendar() {
        weekLabel.setText("Week of " + currentWeekStart.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        DefaultTableModel model = (DefaultTableModel) calendarTable.getModel();
        model.setRowCount(0);

        // Start from 10:00 and go until 00:00 (midnight)
        for (int hour = 10; hour <= 24; hour++) {
            for (int min = 0; min < 60; min += 30) {
                // Skip the last iteration at midnight (24:30)
                if (hour == 24 && min > 0) continue;

                LocalTime time = LocalTime.of(hour == 24 ? 0 : hour, min);
                Vector<String> row = new Vector<>();
                row.add(time.format(timeFormatter));
                for (int i = 0; i < 7; i++) {
                    row.add("");
                }
                model.addRow(row);
            }
        }

        for (int i = 0; i < 7; i++) {
            loadBookingsForDate(currentWeekStart.plusDays(i), model, i + 1);
        }
    }

    /**
     * Loads bookings for a specific date into the calendar table.
     * @param date The date to load bookings for
     * @param model The table model to update
     * @param dayColumnIndex The column index for the day
     */
    private void loadBookingsForDate(LocalDate date, DefaultTableModel model, int dayColumnIndex) {
        String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
        String user = "in2033t26_a";
        String password = "jLxOPuQ69Mg";

        // Modify query based on filter
        String query;
        if (currentFilterRoom == null) {
            query = "SELECT BookingName, Client, " +
                    "REPLACE(Room, 'ë', 'e') as Room, " +
                    "StartTime, EndTime " +
                    "FROM booking WHERE BookingDate = ? ORDER BY StartTime";
        } else {
            query = "SELECT BookingName, Client, " +
                    "REPLACE(Room, 'ë', 'e') as Room, " +
                    "StartTime, EndTime " +
                    "FROM booking WHERE BookingDate = ? AND REPLACE(Room, 'ë', 'e') = ? ORDER BY StartTime";
        }

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, java.sql.Date.valueOf(date));

            // Add room parameter if filtering
            if (currentFilterRoom != null) {
                stmt.setString(2, currentFilterRoom);
            }

            ResultSet rs = stmt.executeQuery();

            // Create a map to store bookings for each time slot
            Map<String, List<String>> timeSlotBookings = new HashMap<>();

            while (rs.next()) {
                String bookingName = rs.getString("BookingName");
                String client = rs.getString("Client");
                String room = rs.getString("Room");
                LocalTime start = rs.getTime("StartTime").toLocalTime();
                LocalTime end = rs.getTime("EndTime").toLocalTime();

                // Format the booking text with start and end time
                String bookingText = room + ": " + bookingName + " (" + client + ") " +
                        start.format(timeFormatter) + "-" + end.format(timeFormatter);

                // Add booking to each affected time slot
                LocalTime currentTime = start;
                while (!currentTime.isAfter(end.minusMinutes(30))) {
                    String timeKey = currentTime.format(timeFormatter);
                    timeSlotBookings.computeIfAbsent(timeKey, k -> new ArrayList<>()).add(bookingText);
                    currentTime = currentTime.plusMinutes(30);
                }
            }

            // Update the table with combined bookings
            for (int row = 0; row < model.getRowCount(); row++) {
                String timeSlot = (String) model.getValueAt(row, 0);
                List<String> bookings = timeSlotBookings.get(timeSlot);
                if (bookings != null && !bookings.isEmpty()) {
                    // Join multiple bookings with | as separator
                    String combinedBookings = String.join(" | ", bookings);
                    model.setValueAt(combinedBookings, row, dayColumnIndex);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching bookings: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Custom popup window for displaying booking details.
     * Provides a floating window that shows detailed information about a booking when clicked.
     */
    private class BookingPopup extends JWindow {
        /**
         * Constructs a new booking popup window with transparent background.
         */
        public BookingPopup() {
            setBackground(new Color(0, 0, 0, 0));

            JPanel content = new JPanel();
            content.setLayout(new BorderLayout());
            content.setBackground(darkColour);
            content.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.WHITE),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setBackground(darkColour);
            textArea.setForeground(Color.WHITE);
            textArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
            textArea.setBorder(null);

            content.add(textArea, BorderLayout.CENTER);
            setContentPane(content);

            // Add global mouse listener to close popup when clicking outside
            Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
                if (event instanceof MouseEvent) {
                    MouseEvent mouseEvent = (MouseEvent) event;
                    if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED) {
                        Point p = mouseEvent.getLocationOnScreen();
                        if (!getBounds().contains(p)) {
                            setVisible(false);
                        }
                    }
                }
            }, AWTEvent.MOUSE_EVENT_MASK);
        }

        /**
         * Displays the popup window with the specified text at the given location.
         * @param text The text to display in the popup
         * @param location The screen location where the popup should appear
         */
        public void showPopup(String text, Point location) {
            JTextArea textArea = (JTextArea) getContentPane().getComponent(0);
            textArea.setText(text);
            pack();

            // Ensure popup fits on screen
            Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration().getBounds();

            int x = location.x;
            int y = location.y;

            if (x + getWidth() > screenBounds.width) {
                x = screenBounds.width - getWidth();
            }
            if (y + getHeight() > screenBounds.height) {
                y = screenBounds.height - getHeight();
            }

            setLocation(x, y);
            setVisible(true);
        }
    }

    /**
     * Custom cell renderer for the calendar table.
     * Handles the display of multiple bookings in a single cell with appropriate colors.
     * Each booking is displayed with its room-specific color and includes hover effects.
     */
    private class RoomColorRenderer extends DefaultTableCellRenderer {
        private final BookingPopup popup = new BookingPopup();
        private int hoveredRow = -1;
        private int hoveredCol = -1;

        /**
         * Sets the currently hovered cell for hover effect display.
         * @param row The row index being hovered over
         * @param col The column index being hovered over
         */
        public void setHoveredCell(int row, int col) {
            hoveredRow = row;
            hoveredCol = col;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (value == null || column == 0 || value.toString().isEmpty()) {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }

            // Create a panel to hold multiple bookings horizontally
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(1, 0, 2, 0)); // Horizontal layout with 2px gaps
            panel.setBackground(Color.WHITE);

            // Split multiple bookings if they exist
            String[] bookings = value.toString().split("\\|");
            for (String booking : bookings) {
                String trimmedBooking = booking.trim();
                if (!trimmedBooking.isEmpty()) {
                    // Extract room name and other details
                    String[] parts = trimmedBooking.split(":");
                    String originalRoom = parts[0].trim();
                    String normalizedRoom = normalizeRoomName(originalRoom);
                    String details = parts[1].trim();

                    // Create a label for each booking with normal text alignment
                    JLabel bookingLabel = new JLabel("<html><div style='margin: 3px;'>" +
                            "<b>" + originalRoom + "</b><br>" +  // Use original room name for display
                            details + "</div></html>");
                    bookingLabel.setOpaque(true);
                    bookingLabel.setHorizontalAlignment(SwingConstants.LEFT);
                    bookingLabel.setVerticalAlignment(SwingConstants.TOP);

                    // Set the background color based on the normalized room name
                    Color bgColor = spaceColors.getOrDefault(normalizedRoom, Color.GRAY);
                    bookingLabel.setBackground(bgColor);

                    // Set text color based on background brightness
                    double luminance = (0.299 * bgColor.getRed() + 0.587 * bgColor.getGreen() + 0.114 * bgColor.getBlue()) / 255;
                    bookingLabel.setForeground(luminance > 0.5 ? Color.BLACK : Color.WHITE);

                    // Add padding
                    bookingLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

                    panel.add(bookingLabel);
                }
            }

            return panel;
        }

        private String normalizeRoomName(String room) {
            return room.replace("ë", "e")  // Replace ë with e
                    .replace("\n", " ")  // Replace newlines with spaces
                    .trim();            // Remove extra spaces
        }
    }

    /**
     * Styles a top-level button with consistent appearance.
     * @param button The button to style
     */
    private void styleTopButton(JButton button) {
        button.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        button.setBackground(darkColour);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        addHoverEffect(button);
    }

    /**
     * Adds hover effects to a button.
     * @param button The button to add hover effects to
     */
    private void addHoverEffect(JButton button) {
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
}