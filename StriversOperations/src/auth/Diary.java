package auth;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

// Bookings that are unconfirmed/not fully paid for within the 28 days time slot we provide
public class Diary extends JFrame {


    private final Color background = new Color(18, 32, 35, 255);
    private final Color panelColor = new Color(30, 50, 55);
    private final int fontSize = 22;
    private JTable unpaidBookingsTable;




    public Diary() {
        setTitle("Lancaster's Music Hall Software");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setResizable(false);
        setLocationRelativeTo(null);

        //Main content pane
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(background);
        setContentPane(contentPane);
        contentPane.add(createHeaderPanel(), BorderLayout.NORTH);
        contentPane.add(createMainPanel(), BorderLayout.CENTER);

        loadUnpaidBookings();
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

        // Create title label
        JLabel titleLabel = new JLabel("Unpaid/Pending Bookings");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Create table for unpaid bookings
        unpaidBookingsTable = new JTable();
        unpaidBookingsTable.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(unpaidBookingsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        // Create refresh button
        JButton refreshBtn = new JButton("Refresh");
        styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadUnpaidBookings());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(background);
        buttonPanel.add(refreshBtn);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private void loadUnpaidBookings() {
        try {
            String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
            String user = "in2033t26_a";
            String password = "jLxOPuQ69Mg";

            // Modified query to properly join with client table
            String query = "SELECT b.BookingID, b.BookingName, b.BookingDate, " +
                    "b.StartTime, b.EndTime, b.Room, b.BookingType, " +
                    "b.PaymentStatus, b.Client AS CompanyName " +  // Using the Client field directly
                    "FROM booking b " +
                    "WHERE b.PaymentStatus IS NULL OR " +
                    "LOWER(b.PaymentStatus) IN ('unpaid', 'pending', '') " +
                    "ORDER BY b.BookingDate, b.StartTime";

            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"ID", "Booking Name", "Date", "Start", "End",
                            "Room", "Type", "Status", "Client"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("BookingID"),
                        rs.getString("BookingName"),
                        rs.getDate("BookingDate"),
                        rs.getTime("StartTime"),
                        rs.getTime("EndTime"),
                        rs.getString("Room"),
                        rs.getString("BookingType"),
                        rs.getString("PaymentStatus"),
                        rs.getString("CompanyName")  // This now uses the Client field directly
                });
            }

            unpaidBookingsTable.setModel(model);

            // If no results, show a message
            if (model.getRowCount() == 0) {
                model.addRow(new Object[]{"No unpaid or pending bookings found", "", "", "", "", "", "", "", ""});
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading unpaid bookings: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);

            // Show error in table
            DefaultTableModel errorModel = new DefaultTableModel(
                    new Object[]{"Error"}, 0);
            errorModel.addRow(new Object[]{"Failed to load data. Check console for details."});
            unpaidBookingsTable.setModel(errorModel);
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
