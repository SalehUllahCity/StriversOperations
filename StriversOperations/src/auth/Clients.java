package auth;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
// Bookings that are unconfirmed/not fully paid for within the 28 days time slot we provide
public class Clients extends JFrame {

    private final Color background = new Color(18, 32, 35, 255);
    private final Color panelColor = new Color(30, 50, 55);
    private final int fontSize = 22;
    private JTable clientTable;
    private JTable bookingHistoryTable;
    private JTable paymentHistoryTable;
    private JTabbedPane tabbedPane;

    public Clients() {
        setTitle("Lancaster's Music Hall Software: Clients");
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

        loadClientData();

    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(background);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(background);
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setFont(new Font("TimesRoman", Font.BOLD, 16));

        // Create client list tab
        createClientListTab();

        // Create the other tabs
        createBookingHistoryTab();
        createPaymentHistoryTab();

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        return mainPanel;
    }

    private void createClientListTab() {
        JPanel clientPanel = new JPanel(new BorderLayout());
        clientPanel.setBackground(panelColor);
        clientPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create search panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(panelColor);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Create search field
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.setForeground(Color.WHITE);
        searchField.setBackground(new Color(50, 70, 75));
        searchField.setCaretColor(Color.WHITE);
        searchField.putClientProperty("JTextField.placeholderText", "Search by company name...");

        // Search button
        JButton searchBtn = new JButton("Search");
        styleButton(searchBtn);
        searchBtn.addActionListener(e -> filterClients(searchField.getText()));

        // Search field listener for Enter key
        searchField.addActionListener(e -> filterClients(searchField.getText()));

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        // Client table
        clientTable = new JTable();
        clientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = clientTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String clientId = clientTable.getValueAt(selectedRow, 0).toString();
                    loadBookingHistory(clientId);
                    loadPaymentHistory(clientId);
                    tabbedPane.setSelectedIndex(1);
                }
            }
        });

        JScrollPane clientScrollPane = new JScrollPane(clientTable);
        clientScrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        // Add client button
        JButton addClientBtn = new JButton("Add New Client");
        styleButton(addClientBtn);
        addClientBtn.addActionListener(e -> showAddClientDialog());

        // Clear search button
        JButton clearSearchBtn = new JButton("Clear");
        styleButton(clearSearchBtn);
        clearSearchBtn.addActionListener(e -> {
            searchField.setText("");
            loadClientData();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(panelColor);
        buttonPanel.add(clearSearchBtn);
        buttonPanel.add(addClientBtn);

        clientPanel.add(searchPanel, BorderLayout.NORTH);
        clientPanel.add(clientScrollPane, BorderLayout.CENTER);
        clientPanel.add(buttonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Client List", clientPanel);
    }

    private void loadClientData() {
        try {
            String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
            String userName = "in2033t26_a";
            String password = "jLxOPuQ69Mg";
            String query = "SELECT ClientID, CompanyName, ContactName, ContactEmail, PhoneNumber, City FROM client";

            Connection conn = DriverManager.getConnection(url, userName, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"ID", "Company", "Contact", "Email", "Phone", "City"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("ClientID"),
                        rs.getString("CompanyName"),
                        rs.getString("ContactName"),
                        rs.getString("ContactEmail"),
                        rs.getString("PhoneNumber"),
                        rs.getString("City")
                });
            }

            clientTable.setModel(model);
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading client data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBookingHistory(String clientId) {
        try {
            String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
            String user = "in2033t26_a";
            String password = "jLxOPuQ69Mg";
            // Changed to query bookings where Client matches the company name
            String query = "SELECT b.BookingID, b.BookingDate, b.StartTime, b.EndTime, b.Room, " +
                    "b.BookingType, b.PaymentStatus " +
                    "FROM booking b " +
                    "JOIN client c ON b.Client = c.CompanyName " +
                    "WHERE c.ClientID = ?";

            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(clientId));
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"Booking ID", "Date", "Start", "End", "Room", "Type", "Payment Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                model.addRow(new Object[]{
                        rs.getInt("BookingID"),
                        rs.getDate("BookingDate"),
                        rs.getTime("StartTime"),
                        rs.getTime("EndTime"),
                        rs.getString("Room"),
                        rs.getString("BookingType"),
                        rs.getString("PaymentStatus")
                });
            }

            if (hasResults) {
                paymentHistoryTable.setModel(model);
            } else {
                JOptionPane.showMessageDialog(this, "No booking history found");
            }

            bookingHistoryTable.setModel(model);
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading booking history: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createBookingHistoryTab() {
        JPanel bookingPanel = new JPanel(new BorderLayout());
        bookingPanel.setBackground(panelColor);
        bookingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table and scroll pane
        bookingHistoryTable = new JTable();
        bookingHistoryTable.setAutoCreateRowSorter(true);
        JScrollPane bookingScrollPane = new JScrollPane(bookingHistoryTable);
        bookingScrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        // Add refresh button
        JButton refreshBtn = new JButton("Refresh Bookings");
        styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> {
            int selectedRow = clientTable.getSelectedRow();
            if (selectedRow >= 0) {
                String clientId = clientTable.getValueAt(selectedRow, 0).toString();
                loadBookingHistory(clientId);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(panelColor);
        buttonPanel.add(refreshBtn);

        bookingPanel.add(bookingScrollPane, BorderLayout.CENTER);
        bookingPanel.add(buttonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Booking History", bookingPanel);
    }

    private void createPaymentHistoryTab() {
        JPanel paymentPanel = new JPanel(new BorderLayout());
        paymentPanel.setBackground(panelColor);
        paymentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table and scroll pane
        paymentHistoryTable = new JTable();
        paymentHistoryTable.setAutoCreateRowSorter(true);
        JScrollPane paymentScrollPane = new JScrollPane(paymentHistoryTable);
        paymentScrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        // Add refresh button
        JButton refreshBtn = new JButton("Refresh Payments");
        styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> {
            int selectedRow = clientTable.getSelectedRow();
            if (selectedRow >= 0) {
                String clientId = clientTable.getValueAt(selectedRow, 0).toString();
                loadPaymentHistory(clientId);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(panelColor);
        buttonPanel.add(refreshBtn);

        paymentPanel.add(paymentScrollPane, BorderLayout.CENTER);
        paymentPanel.add(buttonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Payment History", paymentPanel);
    }

    private void loadPaymentHistory(String clientId) {
        try {
            String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
            String user = "in2033t26_a";
            String password = "jLxOPuQ69Mg";
            String query = "SELECT p.PaymentID, p.PaymentDate, p.Amount, p.PaymentMethod, p.Status, " +
                    "b.BookingName FROM payment p " +
                    "JOIN booking b ON p.BookingID = b.BookingID " +
                    "JOIN client c ON b.ClientID = c.ClientID " +  // Changed this line
                    "WHERE c.ClientID = ?";

            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(clientId));
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"Payment ID", "Date", "Amount", "Method", "Status", "Booking"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                model.addRow(new Object[]{
                        rs.getInt("PaymentID"),
                        rs.getDate("PaymentDate"),
                        rs.getBigDecimal("Amount"),
                        rs.getString("PaymentMethod"),
                        rs.getString("Status"),
                        rs.getString("BookingName")
                });
            }

            if (hasResults) {
                paymentHistoryTable.setModel(model);
            } else {
                JOptionPane.showMessageDialog(this, "No payment history found");
            }

            paymentHistoryTable.setModel(model);
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading payment history: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddClientDialog() {
        JDialog dialog = new JDialog(this, "Add New Client", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(9, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(panelColor);

        JTextField companyField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField postcodeField = new JTextField();
        JTextField billingNameField = new JTextField();
        JTextField billingEmailField = new JTextField();

        addFormField(formPanel, "Company Name:", companyField);
        addFormField(formPanel, "Contact Name:", contactField);
        addFormField(formPanel, "Contact Email:", emailField);
        addFormField(formPanel, "Phone Number:", phoneField);
        addFormField(formPanel, "Street Address:", addressField);
        addFormField(formPanel, "City:", cityField);
        addFormField(formPanel, "Postcode:", postcodeField);
        addFormField(formPanel, "Billing Name:", billingNameField);
        addFormField(formPanel, "Billing Email:", billingEmailField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(panelColor);

        JButton saveBtn = new JButton("Save");
        styleButton(saveBtn);

        saveBtn.addActionListener(e -> {
            saveNewClient(

                    companyField.getText(),
                    contactField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    addressField.getText(),
                    cityField.getText(),
                    postcodeField.getText(),
                    billingNameField.getText(),
                    billingEmailField.getText()
            );

            dialog.dispose();
        });


        JButton cancelBtn = new JButton("Cancel");
        styleButton(cancelBtn);
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void filterClients(String searchText) {
        try {
            String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
            String user = "in2033t26_a";
            String password = "jLxOPuQ69Mg";
            String query = "SELECT ClientID, CompanyName, ContactName, ContactEmail, PhoneNumber, City " +
                    "FROM client WHERE CompanyName LIKE ?";

            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, "%" + searchText + "%");
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"ID", "Company", "Contact", "Email", "Phone", "City"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("ClientID"),
                        rs.getString("CompanyName"),
                        rs.getString("ContactName"),
                        rs.getString("ContactEmail"),
                        rs.getString("PhoneNumber"),
                        rs.getString("City")
                });
            }

            clientTable.setModel(model);
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching clients: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void addFormField(JPanel panel, String label, JTextField field) {
        JLabel jLabel = new JLabel(label);
        jLabel.setForeground(Color.WHITE);
        panel.add(jLabel);
        panel.add(field);
    }

    private void saveNewClient(String company, String contact, String email, String phone,
                               String address, String city, String postcode,
                               String billingName, String billingEmail) {
        try {
            String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
            String user = "in2033t26_a";
            String password = "jLxOPuQ69Mg";
            String query = "INSERT INTO client (CompanyName, ContactName, ContactEmail, PhoneNumber, " +
                    "StreetAddress, City, Postcode, BillingName, BillingEmail) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, company);
            stmt.setString(2, contact);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setString(5, address);
            stmt.setString(6, city);
            stmt.setString(7, postcode);
            stmt.setString(8, billingName.isEmpty() ? null : billingName);
            stmt.setString(9, billingEmail.isEmpty() ? null : billingEmail);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Client added successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadClientData();
            }

            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving client: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
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
                Clients frame = new Clients();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
        JLabel titleLabel = new JLabel("Client Data");
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
}
