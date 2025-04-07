package auth;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Events extends JFrame {

    private final Color background = new Color(18, 32, 35, 255);
    private final int fontSize = 22;
    private JTable eventsTable;
    private DefaultTableModel tableModel;
    private List<Booking> allBookings;

    public Events() {
        setTitle("Lancaster's Music Hall Software: Events");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setResizable(false);
        setLocationRelativeTo(null);

        // Main content pane
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(background);
        setContentPane(contentPane);
        contentPane.add(createHeaderPanel(), BorderLayout.NORTH);

        // Create main content with proper layout
        JPanel centralContent = new JPanel(new BorderLayout(0, 10));
        centralContent.setBackground(background);
        centralContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add components to central content
        centralContent.add(createDiscountKeyPanel(), BorderLayout.NORTH);

        // Panel that contains search and table
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBackground(background);
        contentPanel.add(createSearchPanel(), BorderLayout.NORTH);
        contentPanel.add(createEventsTable(), BorderLayout.CENTER);

        centralContent.add(contentPanel, BorderLayout.CENTER);
        contentPane.add(centralContent, BorderLayout.CENTER);
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(background);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JLabel searchLabel = new JLabel("Search Events:");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(new Font("TimesRoman", Font.PLAIN, 16));

        JTextField searchField = new JTextField(30);
        searchField.setFont(new Font("TimesRoman", Font.PLAIN, 16));

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        searchButton.setBackground(new Color(50, 80, 80));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        addHoverEffect(searchButton);

        JButton resetButton = new JButton("Reset");
        resetButton.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        resetButton.setBackground(new Color(70, 50, 50));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        addHoverEffect(resetButton);

        // Add action listeners for search and reset
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().toLowerCase().trim();
            filterEvents(searchText);
        });

        resetButton.addActionListener(e -> {
            searchField.setText("");
            resetTableData();
        });

        // Add components to panel
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(resetButton);

        return searchPanel;
    }

    private void filterEvents(String searchText) {
        if (searchText.isEmpty()) {
            resetTableData();
            return;
        }

        // Clear the table
        tableModel.setRowCount(0);

        // Filter bookings and add matching ones to the table
        for (Booking booking : allBookings) {
            if (matchesSearch(booking, searchText)) {
                tableModel.addRow(new Object[]{
                        booking.getClientName(),
                        booking.getBookingName(),
                        booking.getBookingDate(),
                        booking.getBookingEndDate(),
                        booking.getStartTime(),
                        booking.getEndTime(),
                        booking.getRoom(),
                        booking.getMaxDiscount() + "%",
                        booking.getDescription()
                });
            }
        }
    }

    private boolean matchesSearch(Booking booking, String searchText) {
        // Check if any field contains the search text
        return booking.getBookingName().toLowerCase().contains(searchText)
                || booking.getRoom().toLowerCase().contains(searchText)
                || booking.getDescription().toLowerCase().contains(searchText)
                || booking.getClientName().toLowerCase().contains(searchText)
                || (booking.getBookingDate() != null && booking.getBookingDate().toString().contains(searchText))
                || (booking.getBookingEndDate() != null && booking.getBookingEndDate().toString().contains(searchText))
                || booking.getStartTime().toLowerCase().contains(searchText)
                || booking.getEndTime().toLowerCase().contains(searchText)
                || String.valueOf(booking.getMaxDiscount()).contains(searchText);
    }

    private void resetTableData() {
        // Clear the table
        tableModel.setRowCount(0);

        // Add all bookings back to the table
        for (Booking booking : allBookings) {
            tableModel.addRow(new Object[]{
                    booking.getBookingName(),
                    booking.getBookingDate(),
                    booking.getBookingEndDate(),
                    booking.getStartTime(),
                    booking.getEndTime(),
                    booking.getRoom(),
                    booking.getClientName(),
                    booking.getMaxDiscount() + "%",
                    booking.getDescription()
            });
        }
    }

    private JPanel createDiscountKeyPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1));
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Title
        JLabel title = new JLabel("Maximum Discounts Per Hall (Discounts do not stack)");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("TimesRoman", Font.BOLD, 18));
        panel.add(title);

        // Small Hall
        JLabel smallHall = new JLabel("Small Hall: Overall 15% | Students 10% | NHS 10% | Friends of Lancasters 15%");
        smallHall.setForeground(Color.WHITE);
        panel.add(smallHall);

        // Main Hall
        JLabel mainHall = new JLabel("Main Hall: Overall 10% | Students 10% | NHS 10% | Friends of Lancasters 10%");
        mainHall.setForeground(Color.WHITE);
        panel.add(mainHall);

        // Rehearsal Space
        JLabel rehearsal = new JLabel("Rehearsal Space: Overall 20% | Students 10% | NHS 10% | Friends of Lancasters 15%");
        rehearsal.setForeground(Color.WHITE);
        panel.add(rehearsal);

        return panel;
    }

    private JScrollPane createEventsTable() {
        // Table model with columns
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        // Add columns with appropriate widths
        tableModel.addColumn("Client");
        tableModel.addColumn("Booking Name");
        tableModel.addColumn("Start Date");
        tableModel.addColumn("End Date");
        tableModel.addColumn("Start Time");
        tableModel.addColumn("End Time");
        tableModel.addColumn("Room");
        tableModel.addColumn("Max Discount");
        tableModel.addColumn("Description");

        // Get data from database
        allBookings = fetchBookingsFromDatabase();
        for (Booking booking : allBookings) {
            tableModel.addRow(new Object[]{
                    booking.getBookingName(),
                    booking.getBookingDate(),
                    booking.getBookingEndDate(),
                    booking.getStartTime(),
                    booking.getEndTime(),
                    booking.getRoom(),
                    booking.getClientName(),
                    booking.getMaxDiscount() + "%",
                    booking.getDescription()
            });
        }

        // Create table
        eventsTable = new JTable(tableModel);
        eventsTable.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        eventsTable.setRowHeight(30);
        eventsTable.setForeground(Color.WHITE);
        eventsTable.setBackground(background);
        eventsTable.setGridColor(Color.DARK_GRAY);
        eventsTable.setSelectionBackground(new Color(50, 70, 70));

        // Set column widths to prevent Max Discount from being obscured
        eventsTable.getColumnModel().getColumn(0).setPreferredWidth(120); // Client
        eventsTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Booking Name
        eventsTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Start Date
        eventsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // End Date
        eventsTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Start Time
        eventsTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // End Time
        eventsTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Room
        eventsTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Max Discount
        eventsTable.getColumnModel().getColumn(8).setPreferredWidth(200); // Description

        // Add tooltip for cells to show full text on hover
        eventsTable.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                int row = eventsTable.rowAtPoint(p);
                int col = eventsTable.columnAtPoint(p);

                if (row >= 0 && col >= 0) {
                    Object value = eventsTable.getValueAt(row, col);
                    if (value != null) {
                        eventsTable.setToolTipText(value.toString());
                    } else {
                        eventsTable.setToolTipText(null);
                    }
                }
            }
        });

        // Ensure the header is always visible
        eventsTable.getTableHeader().setReorderingAllowed(false);
        eventsTable.getTableHeader().setResizingAllowed(true);

        // Custom header
        JTableHeader header = eventsTable.getTableHeader();
        header.setFont(new Font("TimesRoman", Font.BOLD, 16));
        header.setForeground(Color.WHITE);
        header.setBackground(new Color(30, 50, 50));
        header.setPreferredSize(new Dimension(header.getWidth(), 40)); // Make header taller

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(eventsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(background);

        // Make sure the horizontal scrollbar appears if needed
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        return scrollPane;
    }

    private List<Booking> fetchBookingsFromDatabase() {
        List<Booking> bookings = new ArrayList<>();
        String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
        String userName = "in2033t26_a";
        String password = "jLxOPuQ69Mg";

        try (Connection conn = DriverManager.getConnection(url, userName, password)) {
            // Modified query to match the new column order (client first)
            String query = "SELECT c.CompanyName, b.BookingName, b.BookingDate, b.BookingEndDate, " +
                    "b.StartTime, b.EndTime, b.Room, b.MaxDiscount, b.Description " +
                    "FROM booking b " +
                    "LEFT JOIN client c ON b.ClientID = c.ClientID";

            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Booking booking = new Booking(
                            rs.getString("CompanyName"),    // Client (now first)
                            rs.getString("BookingName"),     // Booking Name (now second)
                            rs.getDate("BookingDate"),       // Start Date
                            rs.getDate("BookingEndDate"),    // End Date
                            rs.getString("StartTime"),       // Start Time
                            rs.getString("EndTime"),         // End Time
                            rs.getString("Room"),            // Room
                            rs.getInt("MaxDiscount"),        // Max Discount
                            rs.getString("Description")
                    );
                    bookings.add(booking);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading events from database", "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        return bookings;
    }

    // Inner class to hold booking data
    private static class Booking {
        private final String client;          // Now first
        private final String bookingName;     // Now second
        private final Date bookingDate;
        private final Date bookingEndDate;
        private final String startTime;
        private final String endTime;
        private final String room;
        private final int maxDiscount;
        private final String description;

        public Booking(String client, String bookingName, Date bookingDate, Date bookingEndDate,
                       String startTime, String endTime, String room, int maxDiscount,
                       String description) {
            this.client = client;
            this.bookingName = bookingName;
            this.bookingDate = bookingDate;
            this.bookingEndDate = bookingEndDate;
            this.startTime = startTime;
            this.endTime = endTime;
            this.room = room;
            this.maxDiscount = maxDiscount;
            this.description = description;
        }

        // Getters
        public String getBookingName() { return bookingName != null ? bookingName : ""; }
        public Date getBookingDate() { return bookingDate; }
        public Date getBookingEndDate() { return bookingEndDate; }
        public String getStartTime() { return startTime != null ? startTime : ""; }
        public String getEndTime() { return endTime != null ? endTime : ""; }
        public String getRoom() { return room != null ? room : ""; }
        public int getMaxDiscount() { return maxDiscount; }
        public String getDescription() { return description != null ? description : ""; }
        public String getClientName() { return client != null ? client : ""; }
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

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(settingsBtn);
        topBar.add(rightPanel, BorderLayout.EAST);

        // Title Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(background);
        JLabel titleLabel = new JLabel("Events");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 36));
        titlePanel.add(titleLabel);

        // Stack both into the header container
        headerContainer.add(topBar);
        headerContainer.add(titlePanel);

        return headerContainer;
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

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Events frame = new Events();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}