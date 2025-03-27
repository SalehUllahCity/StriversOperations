package auth;

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

public class Report extends JFrame {

    private final Color background = new Color(18, 32, 35, 255);
    private final int fontSize = 22;

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
    public Report(){
        setTitle("Lancaster's Music Hall Software");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
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
        tabbedPane.addTab("Reviews", createReviewsTab());
        tabbedPane.addTab("Data by Day", createDailyDataTab());

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
        JLabel titleLabel = new JLabel("Reports");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 36));
        titlePanel.add(titleLabel);

        // Stack both into the header container
        headerContainer.add(topBar);
        headerContainer.add(titlePanel);

        return headerContainer;
    }


    //Tab 1: Venue usage report across dates/spaces/bookings
    private JPanel createVenueUsageTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(background);

        String[] columns = {
                "Booking Name", "Client", "Room", "Start Date", "End Date", "Configuration"
        };
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Apply color renderer to Room column (index 1)
        table.getColumnModel().getColumn(2).setCellRenderer(new RoomColorRenderer());

        loadVenueUsageData(model);


        return panel;
    }

    private void loadVenueUsageData(DefaultTableModel model) {
        String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";

        String user = "in2033t26_a";
        String password = "jLxOPuQ69Mg";

        String query = "SELECT BookingName, Client, Room, BookingDate, BookingEndDate, SeatingConfig " +
                "FROM booking"; // Fetches only today's records

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = {
                        rs.getString("BookingName"),
                        rs.getString("Client"),
                        rs.getString("Room"),
                        rs.getDate("BookingDate"),
                        rs.getDate("BookingEndDate"),
                        rs.getString("SeatingConfig")
                };
                model.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving daily sheets data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Review columns will change based on the API that Martin is yet to provide

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

    //Tab 2: Daily run sheet for operational setup
    private JPanel createDailySheetsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(background);

        String[] columns = {
                "Date", "Room", "Used By", "Seating Config", "Accessibility Notes"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        styleTable(table);

        // Apply color renderer to Room column (index 1)
        table.getColumnModel().getColumn(1).setCellRenderer(new RoomColorRenderer());

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        loadDailySheetsData(model);

        return panel;
    }

    // method to load daily sheets data from the DB
    private void loadDailySheetsData(DefaultTableModel model) {
        String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";

        String user = "in2033t26_a";
        String password = "jLxOPuQ69Mg";

        String query = "SELECT BookingDate, Room, Client, SeatingConfig, Notes " +
                "FROM booking WHERE BookingDate = CURDATE()"; // Fetches only today's records

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = {
                        rs.getDate("BookingDate"),
                        rs.getString("Room"),
                        rs.getString("Client"),
                        rs.getString("SeatingConfig"),
                        rs.getString("Notes")
                };
                model.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving daily sheets data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Tab 3: Financial summary per booking
    private JPanel createFinanceTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(background);

        String[] columns = {
                "Booking Name", "Client", "Hire Fee (£)", "Ticket Revenue (£)", "Payable to Client (£)", "Net Income (£)"
        };

        JTable table = new JTable(new DefaultTableModel(columns, 0));
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
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

    private JPanel createDailyDataTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(background);

        String[] columns = {
                "BookingDate", "Client", "Room", "Booking Type" // add tickets sold
        };
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

         // loadDailyUsageData(model); method is commented below


        return panel;
    }

    /* Keeps giving an error of not recognising BookingDate: Unknown column 'BookingDate' in 'field list'
    private void loadDailyUsageData(DefaultTableModel model) {
        String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";

        String user = "in2033t26_a";
        String password = "jLxOPuQ69Mg";

        String query = "SELECT BookingDate, Client, Room, BookingType" +
                "FROM booking"; // Fetches only today's records

        /*
        String query = "SELECT BookingDate, Client, Room, BookingType" + // add tickets sold from the other team API
                "FROM booking"; // Add a roll date button to allow the staff to pick a date



        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = {
                        rs.getDate("BookingDate"),
                        rs.getString("Client"),
                        rs.getString("Room"),
                        rs.getString("BookingType"),
                        // add tickets sold here from the other team API
                };
                model.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving daily sheets data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

     */




    //Shared table styling
    private void styleTable(JTable table) {
        table.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("TimesRoman", Font.BOLD, 20));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
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
