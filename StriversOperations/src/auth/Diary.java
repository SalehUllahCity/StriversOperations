package auth;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.SimpleDateFormat;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

// Bookings that are unconfirmed/not fully paid for within the 28 days time slot we provide
public class Diary extends JFrame {


    private final Color background = new Color(18, 32, 35, 255);
    private final Color panelColor = new Color(30, 50, 55);
    private JTable scheduleTable;
    private JButton refreshBtn, confirmBtn, cancelBtn;
    private JLabel summaryLabel;





    public Diary() {
        setTitle("Lancaster's Music Hall - Diary Management");
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

        loadScheduleData();
    }


    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Diary frame = new Diary();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(background);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title panel
        JLabel titleLabel = new JLabel("Venue Schedule & Hold Management");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Schedule table
        scheduleTable = new JTable();
        scheduleTable.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(background);

        refreshBtn = createStyledButton("Refresh");
        confirmBtn = createStyledButton("Confirm Booking");
        cancelBtn = createStyledButton("Cancel Hold");

        refreshBtn.addActionListener(e -> loadScheduleData());
        confirmBtn.addActionListener(e -> confirmSelectedBooking());
        cancelBtn.addActionListener(e -> cancelSelectedHold());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);

        // Summary panel
        summaryLabel = new JLabel(" ", SwingConstants.RIGHT);
        summaryLabel.setForeground(Color.WHITE);
        summaryLabel.setFont(new Font("TimesRoman", Font.PLAIN, 16));

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(summaryLabel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        button.setBackground(new Color(50, 70, 75));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        return button;
    }

    private void loadScheduleData() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26",
                "in2033t26_a",
                "jLxOPuQ69Mg")) {

            String query = "SELECT b.BookingID, b.BookingName, b.BookingDate, " +
                    "b.StartTime, b.EndTime, b.Room, b.BookingType, " +
                    "b.PaymentStatus, b.Client, " +
                    "DATE_SUB(b.BookingDate, INTERVAL 28 DAY) AS ContractDueDate " +
                    "FROM booking b " +
                    "ORDER BY b.BookingDate, b.StartTime";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                DefaultTableModel model = new DefaultTableModel(
                        new Object[]{"ID", "Event", "Date", "Start", "End",
                                "Room", "Type", "Status", "Client", "Contract Due", "Cost"}, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                while (rs.next()) {
                    Date bookingDate = rs.getDate("BookingDate");
                    Date contractDue = rs.getDate("ContractDueDate");
                    String cost = calculateCost(
                            rs.getString("Room"),
                            bookingDate,
                            rs.getTime("StartTime"),
                            rs.getTime("EndTime")
                    );

                    model.addRow(new Object[]{
                            rs.getInt("BookingID"),
                            rs.getString("BookingName"),
                            dateFormat.format(bookingDate),
                            rs.getTime("StartTime"),
                            rs.getTime("EndTime"),
                            rs.getString("Room"),
                            rs.getString("BookingType"),
                            rs.getString("PaymentStatus"),
                            rs.getString("Client"),
                            dateFormat.format(contractDue),
                            cost
                    });
                }

                scheduleTable.setModel(model);
                updateSummary();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading schedule: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String calculateCost(String room, Date bookingDate, Time startTime, Time endTime) {
        // Simplified cost calculation based on rate card
        try {
            boolean isWeekend = isWeekend(bookingDate);
            boolean isEvening = startTime.getHours() >= 17;

            switch (room) {
                case "Main Hall":
                    if (isEvening) {
                        return isWeekend ? "£2,200 + VAT" : "£1,850 + VAT";
                    } else {
                        long hours = (endTime.getTime() - startTime.getTime()) / (1000 * 60 * 60);
                        hours = Math.max(hours, 3); // Minimum 3 hours
                        return String.format("£%.2f + VAT", hours * 325.0);
                    }
                case "Small Hall":
                    if (isEvening) {
                        return isWeekend ? "£1,300 + VAT" : "£950 + VAT";
                    } else {
                        long hours = (endTime.getTime() - startTime.getTime()) / (1000 * 60 * 60);
                        hours = Math.max(hours, 3); // Minimum 3 hours
                        return String.format("£%.2f + VAT", hours * 225.0);
                    }
                case "Rehearsal Space":
                    long hours = (endTime.getTime() - startTime.getTime()) / (1000 * 60 * 60);
                    hours = Math.max(hours, 3); // Minimum 3 hours
                    return String.format("£%.2f + VAT", hours * 60.0);
                default: // Meeting rooms
                    if (room.equals("The Green Room")) return "£25 + VAT/hr";
                    if (room.equals("Brontë Boardroom")) return "£40 + VAT/hr";
                    if (room.equals("Dickens Den")) return "£30 + VAT/hr";
                    if (room.equals("Poe Parlor")) return "£35 + VAT/hr";
                    if (room.equals("Globe Room")) return "£50 + VAT/hr";
                    if (room.equals("Chekhov Chamber")) return "£38 + VAT/hr";
                    return "Rate not available";
            }
        } catch (Exception e) {
            return "Error calculating";
        }
    }

    private boolean isWeekend(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        return day == Calendar.SATURDAY || day == Calendar.SUNDAY;
    }

    private void updateSummary() {
        int unpaidCount = 0;
        int pendingCount = 0;
        int overdueCount = 0;

        DefaultTableModel model = (DefaultTableModel) scheduleTable.getModel();
        Date today = new Date();

        for (int i = 0; i < model.getRowCount(); i++) {
            String status = (String) model.getValueAt(i, 7);
            Date contractDue = null;
            try {
                contractDue = (Date) new SimpleDateFormat("yyyy-MM-dd").parse(model.getValueAt(i, 9).toString());
            } catch (Exception e) {}

            if ("Unpaid".equals(status)) {
                unpaidCount++;
            } else if ("Pending".equals(status)) {
                pendingCount++;
                if (contractDue != null && contractDue.before(today)) {
                    overdueCount++;
                }
            }
        }

        summaryLabel.setText(String.format(
                "Summary: %d Unpaid, %d Pending (%d overdue contracts)",
                unpaidCount, pendingCount, overdueCount));
    }

    private void confirmSelectedBooking() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow >= 0) {
            int bookingId = (int) scheduleTable.getValueAt(selectedRow, 0);

            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26",
                    "in2033t26_a",
                    "jLxOPuQ69Mg")) {

                String updateQuery = "UPDATE booking SET PaymentStatus = 'Paid' WHERE BookingID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    stmt.setInt(1, bookingId);

                    int updated = stmt.executeUpdate();
                    if (updated > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Booking confirmed and marked as paid!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadScheduleData();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error confirming booking: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a booking to confirm",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cancelSelectedHold() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to cancel this booking?",
                    "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                int bookingId = (int) scheduleTable.getValueAt(selectedRow, 0);

                try (Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26",
                        "in2033t26_a",
                        "jLxOPuQ69Mg")) {

                    String updateQuery = "UPDATE booking SET PaymentStatus = 'Cancelled' WHERE BookingID = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                        stmt.setInt(1, bookingId);

                        int updated = stmt.executeUpdate();
                        if (updated > 0) {
                            JOptionPane.showMessageDialog(this,
                                    "Booking cancelled successfully",
                                    "Success", JOptionPane.INFORMATION_MESSAGE);
                            loadScheduleData();
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Error cancelling booking: " + e.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a booking to cancel",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
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
        JLabel titleLabel = new JLabel("Diary");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 36));
        titlePanel.add(titleLabel);

        // Stack both into the header container
        headerContainer.add(topBar);
        headerContainer.add(titlePanel);

        return headerContainer;
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        button.setBackground(new Color(50, 70, 75));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(70, 90, 95));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(50, 70, 75));
            }
        });
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
}
