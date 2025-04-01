package auth;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


public class Calendar extends JFrame {
    private JTable calendarTable;
    private JLabel weekLabel;
    private LocalDate currentWeekStart;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final Color darkColour = new Color(18, 32, 35);
    private final int fontSize = 18;
    private LocalDate selectedMonthDate = LocalDate.now();
    private JPanel calendarGrid;
    private JLabel monthYearLabel;

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

    public Calendar() {
        setTitle("Lancaster's Music Hall Weekly Calendar");
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
    }

    private void createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkColour);
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton homeBtn = new JButton("← Home");
        homeBtn.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        homeBtn.addActionListener(e -> {
            dispose();
            new UserHome().setVisible(true);
        });

        JButton settingsBtn = new JButton("⚙ Settings");
        settingsBtn.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        settingsBtn.addActionListener(e -> new SettingScreen.SettingsDialog(this).setVisible(true));

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navButtons.setOpaque(false);
        navButtons.add(homeBtn);

        JPanel controlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlButtons.setOpaque(false);
        controlButtons.add(settingsBtn);

        JPanel weekControl = new JPanel(new BorderLayout());
        weekControl.setOpaque(false);

        JButton prevWeek = new JButton("< Previous");
        prevWeek.addActionListener(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            refreshCalendar();
        });

        JButton nextWeek = new JButton("Next >");
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

        header.add(navButtons, BorderLayout.WEST);
        header.add(weekControl, BorderLayout.CENTER);
        header.add(controlButtons, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    private void createSideMonthView() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setBackground(darkColour);

        JPanel monthHeader = new JPanel(new BorderLayout());
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        monthYearLabel = new JLabel("", JLabel.CENTER);
        monthYearLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

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

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(monthHeader, BorderLayout.NORTH);
        wrapper.add(calendarGrid, BorderLayout.CENTER);

        leftPanel.add(wrapper, BorderLayout.NORTH);
        add(leftPanel, BorderLayout.WEST);
    }

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

    private void createCalendarTable() {
        String[] columnNames = {"Time", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        calendarTable = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        calendarTable.setRowHeight(40);
        calendarTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        calendarTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        calendarTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        //Set fixed column widths
        calendarTable.getColumnModel().getColumn(0).setPreferredWidth(80); // Time
        for (int i = 1; i <= 7; i++) {
            calendarTable.getColumnModel().getColumn(i).setPreferredWidth(160); // Days
        }

        calendarTable.setDefaultRenderer(Object.class, new RoomColorRenderer());

        JScrollPane scrollPane = new JScrollPane(calendarTable);
        add(scrollPane, BorderLayout.CENTER);

        JTableHeader header = calendarTable.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 48)); // Ensure space for wrapped text
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

    private void refreshCalendar() {
        weekLabel.setText("Week of " + currentWeekStart.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        DefaultTableModel model = (DefaultTableModel) calendarTable.getModel();
        model.setRowCount(0);

        for (int hour = 8; hour <= 20; hour++) {
            for (int min = 0; min < 60; min += 30) {
                LocalTime time = LocalTime.of(hour, min);
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

    private void loadBookingsForDate(LocalDate date, DefaultTableModel model, int dayColumnIndex) {
        String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";

        String user = "in2033t26_a"; // change to team username
        String password = "jLxOPuQ69Mg"; // default password is local password -> change to team password when it works

        String query = "SELECT BookingName, Client, Room, StartTime, EndTime FROM booking WHERE BookingDate = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String bookingName = rs.getString("BookingName");
                String client = rs.getString("Client");
                String room = rs.getString("Room");
                LocalTime start = rs.getTime("StartTime").toLocalTime();
                LocalTime end = rs.getTime("EndTime").toLocalTime();

                for (int row = 0; row < model.getRowCount(); row++) {
                    LocalTime slotTime = LocalTime.parse((String) model.getValueAt(row, 0), timeFormatter);
                    if (!slotTime.isBefore(start) && slotTime.isBefore(end)) {
                        model.setValueAt(room + ": " + bookingName + " (" + client + ")", row, dayColumnIndex);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching bookings: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Custom Renderer to Color Code Room/Space Columns
    class RoomColorRenderer extends DefaultTableCellRenderer {
        private final Map<String, Color> roomColorMap = new HashMap<>();
        private final Color[] colors = {Color.CYAN, Color.ORANGE, Color.PINK, Color.GREEN, Color.YELLOW, Color.BLUE, Color.MAGENTA, Color.LIGHT_GRAY};
        private int colorIndex = 0;

        //Adjust font colour for readability, using luminance formula
        private Color getContrastingTextColor(Color bg) {
            double luminance = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue()) / 255;
            return luminance > 0.5 ? Color.BLACK : Color.WHITE;
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null && column != 0 && !value.toString().isEmpty()) {
                String content = value.toString();
                String room = content.split(":")[0];
                roomColorMap.putIfAbsent(room, colors[colorIndex++ % colors.length]);
                Color bgColor = roomColorMap.get(room);
                cell.setBackground(bgColor);
                cell.setForeground(getContrastingTextColor(bgColor));
            } else {
                cell.setBackground(Color.WHITE);
                cell.setForeground(Color.BLACK);
            }
            return cell;
        }
    }
}
