package auth;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rooms extends JFrame {
    private final Color background = new Color(18, 32, 35, 255);
    private final int fontSize = 22;
    private JTable roomsTable;
    private DefaultTableModel tableModel;

    public Rooms() {
        setTitle("Lancaster's Music Hall Software: Room Information");
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
        centralContent.add(createRoomsTable(), BorderLayout.CENTER);

        // Add accessibility note at the bottom
        centralContent.add(createAccessibilityNote(), BorderLayout.SOUTH);

        contentPane.add(centralContent, BorderLayout.CENTER);
    }

    private JScrollPane createRoomsTable() {
        // Table model with columns
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only make the "View Calendar" column editable
            }
        };

        // Add columns
        tableModel.addColumn("Room Name");
        tableModel.addColumn("Capacity");
        tableModel.addColumn("Layouts");
        tableModel.addColumn("Hourly Rate");
        tableModel.addColumn("Evening Rate");
        tableModel.addColumn("Daily Rate");
        tableModel.addColumn("View Calendar");

        // Add room data
        addRoomData();

        // Create table
        roomsTable = new JTable(tableModel) {
            // Override to add tool tips for each cell
            public String getToolTipText(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row >= 0 && col >= 0) {
                    Object value = getValueAt(row, col);
                    if (value != null) {
                        return value.toString();
                    }
                }
                return null;
            }

            // Override the prepareRenderer to maintain text readability when selected
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                if (isRowSelected(row) && column != 6) { // Don't change button column
                    c.setBackground(new Color(40, 60, 65)); // Slightly lighter than background
                    c.setForeground(Color.WHITE); // Keep text white for readability
                } else if (column != 6) { // Don't change button column styling
                    c.setBackground(getBackground());
                    c.setForeground(getForeground());
                }

                return c;
            }
        };

        roomsTable.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        roomsTable.setRowHeight(40);
        roomsTable.setForeground(Color.WHITE);
        roomsTable.setBackground(background);
        roomsTable.setGridColor(Color.DARK_GRAY);
        roomsTable.setSelectionBackground(new Color(40, 60, 65)); // Lighter than background but not too bright
        roomsTable.setSelectionForeground(Color.WHITE); // Keep text white when selected

        // Set column widths
        roomsTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Room Name
        roomsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Capacity
        roomsTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Layouts
        roomsTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Hourly Rate
        roomsTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Evening Rate
        roomsTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Daily Rate
        roomsTable.getColumnModel().getColumn(6).setPreferredWidth(150); // View Calendar

        // Add button renderer and editor for the "View Calendar" column
        roomsTable.getColumn("View Calendar").setCellRenderer(new ButtonRenderer());
        roomsTable.getColumn("View Calendar").setCellEditor(new ButtonEditor(new JCheckBox(), this));

        // Custom header
        JTableHeader header = roomsTable.getTableHeader();
        header.setFont(new Font("TimesRoman", Font.BOLD, 16));
        header.setForeground(Color.WHITE);
        header.setBackground(new Color(30, 50, 50));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(roomsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(background);

        return scrollPane;
    }

    private JPanel createAccessibilityNote() {
        JPanel accessibilityPanel = new JPanel();
        accessibilityPanel.setLayout(new BoxLayout(accessibilityPanel, BoxLayout.Y_AXIS));
        accessibilityPanel.setBackground(background);
        accessibilityPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JLabel titleLabel = new JLabel("Accessibility Seats:");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel smallHallLabel = new JLabel("Small Hall: Seats A1 to A8, and the first seat of rows B to N");
        smallHallLabel.setForeground(Color.WHITE);
        smallHallLabel.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        smallHallLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel mainHallLabel = new JLabel("Main Hall: Seats A1 to A19, L1 to L19 and the first and last seat of rows B to L, M to Q");
        mainHallLabel.setForeground(Color.WHITE);
        mainHallLabel.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        mainHallLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        accessibilityPanel.add(titleLabel);
        accessibilityPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        accessibilityPanel.add(smallHallLabel);
        accessibilityPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        accessibilityPanel.add(mainHallLabel);

        return accessibilityPanel;
    }

    private void addRoomData() {
        // Performance Spaces
        addRoom("Main Hall",
                "374 (285 Stalls, 89 Balcony)",
                "Stalls & Balcony, Stalls",
                "£325 + VAT (min 3 hours, Mon-Fri 10:00-17:00)",
                "£1,850-£2,200 + VAT (Mon-Thu/Fri-Sat 17:00-00:00)",
                "£3,800-£4,200 + VAT (Mon-Thu/Fri-Sat 10:00-00:00)");

        addRoom("Small Hall",
                "85",
                "Stalls",
                "£225 + VAT (min 3 hours, Mon-Fri 10:00-17:00)",
                "£950-£1,300 + VAT (Mon-Thu/Fri-Sat 17:00-00:00)",
                "£2,200-£2,500 + VAT (Mon-Thu/Fri-Sat 10:00-00:00)");

        // Rehearsal Space
        addRoom("Rehearsal Space",
                "N/A",
                "Open space, no fixed seating",
                "£60 + VAT (min 3 hours, Mon-Fri 10:00-17:00)",
                "N/A",
                "£240-£500 + VAT (Mon-Fri/Sat-Sun 10:00-17:00/23:00)");

        // Meeting Rooms
        addRoom("The Green Room",
                "12 Classroom, 10 Boardroom, 20 Presentation",
                "Classroom, Boardroom, Presentation",
                "£25 + VAT",
                "N/A",
                "£130 + VAT");

        addRoom("Brontë Boardroom",
                "25 Classroom, 18 Boardroom, 40 Presentation",
                "Classroom, Boardroom, Presentation",
                "£40 + VAT",
                "N/A",
                "£200 + VAT");

        addRoom("Dickens Den",
                "15 Classroom, 12 Boardroom, 25 Presentation",
                "Classroom, Boardroom, Presentation",
                "£30 + VAT",
                "N/A",
                "£150 + VAT");

        addRoom("Poe Parlor",
                "20 Classroom, 14 Boardroom, 30 Presentation",
                "Classroom, Boardroom, Presentation",
                "£35 + VAT",
                "N/A",
                "£170 + VAT");

        addRoom("Globe Room",
                "30 Classroom, 20 Boardroom, 50 Presentation",
                "Classroom, Boardroom, Presentation",
                "£50 + VAT",
                "N/A",
                "£250 + VAT");

        addRoom("Chekhov Chamber",
                "18 Classroom, 16 Boardroom, 35 Presentation",
                "Classroom, Boardroom, Presentation",
                "£38 + VAT",
                "N/A",
                "£180 + VAT");
    }

    private void addRoom(String name, String capacity, String layouts, String hourlyRate, String eveningRate, String dailyRate) {
        tableModel.addRow(new Object[]{name, capacity, layouts, hourlyRate, eveningRate, dailyRate, "View Calendar"});
    }

    // Method to open calendar for a specific room
    public void openRoomCalendar(String roomName) {
        // Create a filtered calendar view for the selected room
        Calendar roomCalendar = new Calendar() {
            protected void loadBookingsForDate(LocalDate date, DefaultTableModel model, int dayColumnIndex) {
                String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
                String user = "in2033t26_a";
                String password = "jLxOPuQ69Mg";
                String query = "SELECT BookingName, Client, " +
                        "REPLACE(Room, 'ë', 'e') as Room, " +
                        "StartTime, EndTime " +
                        "FROM booking WHERE BookingDate = ? AND REPLACE(Room, 'ë', 'e') = ? " +
                        "ORDER BY StartTime";

                try (Connection conn = DriverManager.getConnection(url, user, password);
                     PreparedStatement stmt = conn.prepareStatement(query)) {

                    stmt.setDate(1, java.sql.Date.valueOf(date));
                    stmt.setString(2, roomName);
                    ResultSet rs = stmt.executeQuery();

                    Map<String, List<String>> timeSlotBookings = new HashMap<>();

                    while (rs.next()) {
                        String bookingName = rs.getString("BookingName");
                        String client = rs.getString("Client");
                        String room = rs.getString("Room");
                        LocalTime start = rs.getTime("StartTime").toLocalTime();
                        LocalTime end = rs.getTime("EndTime").toLocalTime();

                        String bookingText = bookingName + " (" + client + ") " +
                                start.format(timeFormatter) + "-" + end.format(timeFormatter);

                        LocalTime currentTime = start;
                        while (!currentTime.isAfter(end.minusMinutes(30))) {
                            String timeKey = currentTime.format(timeFormatter);
                            timeSlotBookings.computeIfAbsent(timeKey, k -> new ArrayList<>()).add(bookingText);
                            currentTime = currentTime.plusMinutes(30);
                        }
                    }

                    for (int row = 0; row < model.getRowCount(); row++) {
                        String timeSlot = (String) model.getValueAt(row, 0);
                        List<String> bookings = timeSlotBookings.get(timeSlot);
                        if (bookings != null && !bookings.isEmpty()) {
                            String combinedBookings = String.join(" | ", bookings);
                            model.setValueAt(combinedBookings, row, dayColumnIndex);
                        }
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error fetching bookings: " + e.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        roomCalendar.setTitle("Lancaster's Music Hall - " + roomName + " Calendar");
        roomCalendar.setVisible(true);
    }

    // Button renderer for the View Calendar column
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setForeground(Color.WHITE);
            setBackground(new Color(50, 80, 80));
            setFont(new Font("TimesRoman", Font.PLAIN, 14));
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Button editor for the View Calendar column
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private Rooms parentFrame;

        public ButtonEditor(JCheckBox checkBox, Rooms parent) {
            super(checkBox);
            this.parentFrame = parent;
            button = new JButton();
            button.setOpaque(true);
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(50, 80, 80));
            button.setFont(new Font("TimesRoman", Font.PLAIN, 14));
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                // Get the room name from the first column of the selected row
                String roomName = (String) roomsTable.getValueAt(roomsTable.getSelectedRow(), 0);
                parentFrame.openRoomCalendar(roomName);
            }
            isPushed = false;
            return label;
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
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

        // Settings button
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
        JLabel titleLabel = new JLabel("Room Information");
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

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Rooms frame = new Rooms();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}