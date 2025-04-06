package auth;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Events extends JFrame {

    private final Color background = new Color(18, 32, 35, 255);
    private final int fontSize = 22;

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
        contentPane.add(createMainPanel(), BorderLayout.CENTER);
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(background);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create discount key panel
        JPanel discountPanel = createDiscountKeyPanel();
        mainPanel.add(discountPanel, BorderLayout.NORTH);

        // Create events table
        JScrollPane tableScrollPane = createEventsTable();
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createDiscountKeyPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1));
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

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
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        model.addColumn("Event Name");
        model.addColumn("Start Date");
        model.addColumn("End Date");
        model.addColumn("Room");
        model.addColumn("Notes");

        // Get data from database
        List<Booking> bookings = fetchBookingsFromDatabase();
        for (Booking booking : bookings) {
            model.addRow(new Object[]{
                    booking.getBookingName(),
                    booking.getBookingDate(),
                    booking.getBookingEndDate(),
                    booking.getRoom(),
                    booking.getNotes()
            });
        }

        // Create table
        JTable table = new JTable(model);
        table.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.setForeground(Color.WHITE);
        table.setBackground(background);
        table.setGridColor(Color.DARK_GRAY);
        table.setSelectionBackground(new Color(50, 70, 70));

        // Custom header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("TimesRoman", Font.BOLD, 18));
        header.setForeground(Color.WHITE);
        header.setBackground(new Color(30, 50, 50));

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(background);

        return scrollPane;
    }

    private List<Booking> fetchBookingsFromDatabase() {
        List<Booking> bookings = new ArrayList<>();
        String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
        String userName = "in2033t26_a";
        String password = "jLxOPuQ69Mg";

        try (Connection conn = DriverManager.getConnection(url, userName, password)) {
            String query = "SELECT BookingName, BookingDate, BookingEndDate, Room, Notes FROM booking";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Booking booking = new Booking(
                            rs.getString("BookingName"),
                            rs.getDate("BookingDate"),
                            rs.getDate("BookingEndDate"),
                            rs.getString("Room"),
                            rs.getString("Notes")
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
        private final String bookingName;
        private final Date bookingDate;
        private final Date bookingEndDate;
        private final String room;
        private final String notes;

        public Booking(String bookingName, Date bookingDate, Date bookingEndDate, String room, String notes) {
            this.bookingName = bookingName;
            this.bookingDate = bookingDate;
            this.bookingEndDate = bookingEndDate;
            this.room = room;
            this.notes = notes;
        }

        // Getters
        public String getBookingName() { return bookingName; }
        public Date getBookingDate() { return bookingDate; }
        public Date getBookingEndDate() { return bookingEndDate; }
        public String getRoom() { return room; }
        public String getNotes() { return notes; }
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
