package auth;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Calendar extends JFrame {

    private JPanel calendarPanel;
    private JLabel monthYearLabel;
    private LocalDate currentDate;

    private Color backgroundColour = new Color(18, 32, 35, 255);
    private int fontSize = 30;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Calendar frame = new Calendar();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public Calendar(){
        setTitle("Lancaster's Music Hall Software");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(backgroundColour);
        setLayout(new BorderLayout());

        currentDate = LocalDate.now().withDayOfMonth(1);

        //Header panel: contains settings, title, and month navigation
        createHeaderPanel();

        //Calendar Grid
        calendarPanel = new JPanel();
        calendarPanel.setBackground(backgroundColour);
        calendarPanel.setLayout(new GridLayout(0, 7, 10, 10));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));
        add(calendarPanel, BorderLayout.CENTER);

        //Populate calendar with days
        refreshCalendar();
    }

    /**
     * Creates the top section with navigation buttons, month label, and settings.
     */
    private void createHeaderPanel() {
        // Main top header container
        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BorderLayout());
        topWrapper.setBackground(backgroundColour);
        topWrapper.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Left: ← Home Button
        JButton homeBtn = createStyledButton("← Home");
        homeBtn.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        topWrapper.add(homeBtn, BorderLayout.WEST);
        homeBtn.addActionListener(e -> {
            dispose();
            new UserHome().setVisible(true);
        });

        // Right: ⚙ Settings Button
        JButton settingsBtn = createStyledButton("⚙ Settings");
        settingsBtn.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        settingsBtn.setPreferredSize(new Dimension(120, 40));
        settingsBtn.addActionListener(e -> new SettingScreen.SettingsDialog(this).setVisible(true));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(settingsBtn);
        topWrapper.add(rightPanel, BorderLayout.EAST);

        // Center: Month navigation panel
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(backgroundColour);
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton prevButton = createStyledButton("<");
        JButton nextButton = createStyledButton(">");
        prevButton.addActionListener(e -> {
            currentDate = currentDate.minusMonths(1);
            refreshCalendar();
        });
        nextButton.addActionListener(e -> {
            currentDate = currentDate.plusMonths(1);
            refreshCalendar();
        });

        monthYearLabel = new JLabel("", JLabel.CENTER);
        monthYearLabel.setForeground(Color.WHITE);
        monthYearLabel.setFont(new Font("TimesRoman", Font.BOLD, fontSize));

        navPanel.add(prevButton, BorderLayout.WEST);
        navPanel.add(monthYearLabel, BorderLayout.CENTER);
        navPanel.add(nextButton, BorderLayout.EAST);

        // Stack navPanel below topWrapper
        JPanel stackedHeader = new JPanel();
        stackedHeader.setLayout(new BoxLayout(stackedHeader, BoxLayout.Y_AXIS));
        stackedHeader.setBackground(backgroundColour);
        stackedHeader.add(topWrapper);
        stackedHeader.add(navPanel);

        add(stackedHeader, BorderLayout.NORTH);
    }


    private void refreshCalendar() {
        calendarPanel.removeAll();

        Month month = currentDate.getMonth();
        int year = currentDate.getYear();

        //Update the label at the top
        monthYearLabel.setText(month.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase() + " " + year);

        //Add weekday headers (Mon - Sun)
        for (DayOfWeek dow : DayOfWeek.values()) {
            JLabel dayLabel = new JLabel(dow.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase(), JLabel.CENTER);
            dayLabel.setForeground(Color.WHITE);
            dayLabel.setFont(new Font("TimesRoman", Font.BOLD, fontSize));
            calendarPanel.add(dayLabel);
        }

        //Calculate how many blank spaces before the 1st day
        LocalDate firstDayOfMonth = currentDate;
        int startDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue(); //Monday = 1
        int shift = startDayOfWeek % 7;
        int daysInMonth = month.length(Year.isLeap(year));

        for (int i = 0; i < shift; i++) {
            calendarPanel.add(new JLabel(""));
        }

        //Fill in the actual calendar days
        for (int day = 1; day <= daysInMonth; day++) {
            final int d = day;
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("TimesRoman", Font.BOLD, fontSize));
            dayButton.setForeground(Color.WHITE);
            dayButton.setBackground(new Color(36, 50, 55));
            dayButton.setFocusPainted(false);
            dayButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            dayButton.addActionListener(e -> showBookingDialog(d));
            addHoverEffect(dayButton);
            calendarPanel.add(dayButton);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("TimesRoman", Font.BOLD, fontSize));
        button.setBackground(backgroundColour);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        addHoverEffect(button);
        return button;
    }

    private void showBookingDialog(int day) {
        // JOptionPane.showMessageDialog(this, "Booking info for " + currentDate.withDayOfMonth(day), "Booking Details", JOptionPane.INFORMATION_MESSAGE);

        LocalDate selectedDate = currentDate.withDayOfMonth(day);

        // Create a dialog window
        JDialog dialog = new JDialog(this, "Bookings for " + selectedDate, true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        // Table model
        String[] columns = {"Booking Name", "Client", "Room", "Start Time", "End Time"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

        // Load data from database
        loadBookingsForDate(selectedDate, model);

        // Apply color renderer to Room column (index 1)
        table.getColumnModel().getColumn(2).setCellRenderer(new Calendar.RoomColorRenderer());


        // Add table to dialog
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void loadBookingsForDate(LocalDate date, DefaultTableModel model) {
        String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";

        String user = "in2033t26_a"; // change to team username
        String password = "jLxOPuQ69Mg"; // default password is local password -> change to team password when it works

        String query = "SELECT BookingName, Client, Room, StartTime, EndTime FROM booking WHERE BookingDate = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getString("BookingName"),
                        rs.getString("Client"),
                        rs.getString("Room"),
                        rs.getString("StartTime"),
                        rs.getString("EndTime")
                };
                model.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching bookings: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addHoverEffect(JButton button) {
        button.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
                button.setForeground(Color.LIGHT_GRAY); // change text color on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
                button.setForeground(Color.WHITE); // revert text color
            }
        });
    }
    // Custom Renderer to Color Code Room/Space Columns
    class RoomColorRenderer extends DefaultTableCellRenderer {
        private final Map<String, Color> roomColorMap = new HashMap<>();
        private final Color[] colors = {Color.CYAN, Color.ORANGE, Color.PINK, Color.GREEN, Color.YELLOW, Color.BLUE, Color.MAGENTA, Color.LIGHT_GRAY};
        private int colorIndex = 0;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value != null) {
                String room = value.toString();
                roomColorMap.putIfAbsent(room, colors[colorIndex++ % colors.length]);
                cell.setBackground(roomColorMap.get(room));
            }

            return cell;
        }

}
}
