package auth;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class Diary extends JFrame {

    private final Color background = new Color(18, 32, 35, 255);
    private JTable scheduleTable;
    private JButton refreshBtn, confirmBtn, cancelBtn;
    private JLabel summaryLabel;

    private final Color unpaidColor = new Color(255, 200, 200); // Light red
    private final Color paidColor = new Color(200, 255, 200);   // Light green
    private final Color pendingColor = new Color(255, 230, 200); // Light orange

    public Diary() {
        setTitle("Lancaster's Music Hall - Diary Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setResizable(false);
        setLocationRelativeTo(null);

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

        JLabel titleLabel = new JLabel("Venue Schedule & Hold Management");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        scheduleTable = new JTable();
        scheduleTable.setAutoCreateRowSorter(true);

        scheduleTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String paymentStatus = (String) table.getValueAt(row, 7);

                if (isSelected) {
                    return comp;
                }



                if ("Unpaid".equals(paymentStatus)) {
                    comp.setBackground(unpaidColor);
                } else if ("Paid".equals(paymentStatus)) {
                    comp.setBackground(paidColor);
                } else if ("Pending".equals(paymentStatus)) {
                    comp.setBackground(pendingColor);
                } else {
                    comp.setBackground(table.getBackground());
                }

                return comp;
            }
        });

        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

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

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
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
        if (room == null || bookingDate == null || startTime == null || endTime == null) {
            return "N/A";
        }

        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(bookingDate);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            boolean isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
            boolean isFridayOrSaturday = dayOfWeek == Calendar.FRIDAY || dayOfWeek == Calendar.SATURDAY;

            long durationMillis = endTime.getTime() - startTime.getTime();
            if (durationMillis < 0) {
                durationMillis += 24 * 60 * 60 * 1000;
            }
            int totalMinutes = (int) (durationMillis / (60 * 1000));
            int fullHours = totalMinutes / 60;
            int remainingMinutes = totalMinutes % 60;
            boolean hasHalfHour = remainingMinutes == 30;

            boolean isEvening = startTime.getHours() >= 17;

            boolean isAllDay = fullHours >= 7;

            if ("The Green Room".equals(room)) {
                if (isAllDay) {
                    return "£130.00 + VAT";
                } else {
                    double hourlyRate = 25.0;
                    double cost = fullHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    return String.format("£%.2f + VAT", cost);
                }
            } else if ("Brontë Boardroom".equals(room)) {
                if (isAllDay) {
                    return "£200.00 + VAT";
                } else {
                    double hourlyRate = 40.0;
                    double cost = fullHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    return String.format("£%.2f + VAT", cost);
                }
            } else if ("Dickens Den".equals(room)) {
                if (isAllDay) {
                    return "£150.00 + VAT";
                } else {
                    double hourlyRate = 30.0;
                    double cost = fullHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    return String.format("£%.2f + VAT", cost);
                }
            } else if ("Poe Parlor".equals(room)) {
                if (isAllDay) {
                    return "£170.00 + VAT";
                } else {
                    double hourlyRate = 35.0;
                    double cost = fullHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    return String.format("£%.2f + VAT", cost);
                }
            } else if ("Globe Room".equals(room)) {
                if (isAllDay) {
                    return "£250.00 + VAT";
                } else {
                    double hourlyRate = 50.0;
                    double cost = fullHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    return String.format("£%.2f + VAT", cost);
                }
            } else if ("Chekhov Chamber".equals(room)) {
                if (isAllDay) {
                    return "£180.00 + VAT";
                } else {
                    double hourlyRate = 38.0;
                    double cost = fullHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    return String.format("£%.2f + VAT", cost);
                }
            } else if ("Main Hall".equals(room)) {
                if (isAllDay) {
                    return isFridayOrSaturday ? "£4,200.00 + VAT" : "£3,800.00 + VAT";
                } else if (isEvening) {
                    return isFridayOrSaturday ? "£2,200.00 + VAT" : "£1,850.00 + VAT";
                } else {
                    double hourlyRate = 325.0;
                    int effectiveHours = Math.max(3, fullHours);
                    double cost = effectiveHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    return String.format("£%.2f + VAT", cost);
                }
            } else if ("Small Hall".equals(room)) {
                if (isAllDay) {
                    return isFridayOrSaturday ? "£2,500.00 + VAT" : "£2,200.00 + VAT";
                } else if (isEvening) {
                    return isFridayOrSaturday ? "£1,300.00 + VAT" : "£950.00 + VAT";
                } else {
                    double hourlyRate = 225.0;
                    int effectiveHours = Math.max(3, fullHours);
                    double cost = effectiveHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    return String.format("£%.2f + VAT", cost);
                }
            } else if ("Rehearsal Space".equals(room)) {
                if (startTime.getHours() == 10 && endTime.getHours() == 17) {
                    return isWeekend ? "£340.00 + VAT" : "£240.00 + VAT";
                }
                else if (startTime.getHours() == 10 && endTime.getHours() == 23) {
                    return isWeekend ? "£500.00 + VAT" : "£450.00 + VAT";
                } else {
                    double hourlyRate = 60.0;
                    int effectiveHours = Math.max(3, fullHours);
                    double cost = effectiveHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    return String.format("£%.2f + VAT", cost);
                }
            } else if ("Entire Venue".equals(room)) {
                if (isAllDay) {
                    return isFridayOrSaturday ? "£8,000.00 + VAT" : "£7,000.00 + VAT";
                } else if (isEvening) {
                    return isFridayOrSaturday ? "£4,500.00 + VAT" : "£3,500.00 + VAT";
                }
                return "Custom quote required";
            }

            return "Rate not available";
        } catch (Exception e) {
            e.printStackTrace();
            return "N/A";
        }
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
                contractDue = new SimpleDateFormat("yyyy-MM-dd").parse(model.getValueAt(i, 9).toString());
            } catch (Exception e) {
                System.out.println(e);
            }

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

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(background);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));



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

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(background);
        JLabel titleLabel = new JLabel("Diary");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 36));
        titlePanel.add(titleLabel);

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


}