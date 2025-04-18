package auth;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

/**
 * A GUI application for managing client information, including client details,
 * booking history, and payment history. Provides functionality for adding new clients,
 * searching existing clients, and viewing their associated records.
 */
public class Clients extends JFrame {

    /** UI styling constants */
    private final Color background = new Color(18, 32, 35, 255);
    private final Color panelColor = new Color(30, 50, 55);
    private final Color darkColour = new Color(18, 32, 35);
    private final int fontSize = 18;

    /** UI components for data display */
    private JTable clientTable;
    private JTable bookingHistoryTable;
    private JTable paymentHistoryTable;
    private JTabbedPane tabbedPane;
    private JTextArea clientDetails;
    private JTextArea bookingDetails;
    private JTextArea paymentDetails;

    /**
     * Main method to launch the Clients application.
     * @param args Command line arguments (not used)
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

    /**
     * Constructs a new Clients frame and initializes the UI components.
     * Sets up the tabbed interface for client management.
     */
    public Clients() {
        setTitle("Lancaster's Music Hall Software: Clients");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(background);
        setContentPane(contentPane);
        contentPane.add(createHeaderPanel(), BorderLayout.NORTH);
        contentPane.add(createMainPanel(), BorderLayout.CENTER);

        loadClientData();

    }

    /**
     * Creates the main panel containing the tabbed interface.
     * @return JPanel containing the main content area
     */
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(background);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(background);
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setFont(new Font("TimesRoman", Font.BOLD, 16));

        createClientListTab();

        createBookingHistoryTab();
        createPaymentHistoryTab();

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        return mainPanel;
    }

    /**
     * Creates the client list tab with search functionality.
     */
    private void createClientListTab() {
        JPanel clientPanel = new JPanel(new BorderLayout());
        clientPanel.setBackground(panelColor);
        clientPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(panelColor);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

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

        JButton searchBtn = new JButton("Search");
        styleButton(searchBtn);
        searchBtn.addActionListener(e -> filterClients(searchField.getText()));
        searchField.addActionListener(e -> filterClients(searchField.getText()));

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);
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


        JButton addClientBtn = new JButton("Add New Client");
        styleButton(addClientBtn);
        addClientBtn.addActionListener(e -> showAddClientDialog());



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

    /**
     * Loads client data from the database into the client table.
     */
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

    /**
     * Loads booking history for a specific client.
     * @param clientId The ID of the client whose booking history to load
     */
    private void loadBookingHistory(String clientId) {
        try {
            String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
            String user = "in2033t26_a";
            String password = "jLxOPuQ69Mg";


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

    /**
     * Creates the booking history tab.
     */
    private void createBookingHistoryTab() {
        JPanel bookingPanel = new JPanel(new BorderLayout());
        bookingPanel.setBackground(panelColor);
        bookingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));



        bookingHistoryTable = new JTable();
        bookingHistoryTable.setAutoCreateRowSorter(true);
        JScrollPane bookingScrollPane = new JScrollPane(bookingHistoryTable);
        bookingScrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

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

    /**
     * Creates the payment history tab.
     */
    private void createPaymentHistoryTab() {
        JPanel paymentPanel = new JPanel(new BorderLayout());
        paymentPanel.setBackground(panelColor);
        paymentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));



        paymentHistoryTable = new JTable();
        paymentHistoryTable.setAutoCreateRowSorter(true);
        JScrollPane paymentScrollPane = new JScrollPane(paymentHistoryTable);
        paymentScrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));



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

    /**
     * Loads payment history for a specific client.
     * @param clientId The ID of the client whose payment history to load
     */
    private void loadPaymentHistory(String clientId) {
        try {
            String url = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26";
            String user = "in2033t26_a";
            String password = "jLxOPuQ69Mg";
            String query = "SELECT p.PaymentID, p.PaymentDate, p.Amount, p.PaymentMethod, p.Status, " +
                    "b.BookingName FROM payment p " +
                    "JOIN booking b ON p.BookingID = b.BookingID " +
                    "JOIN client c ON b.ClientID = c.ClientID " +
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

    /**
     * Shows the dialog for adding a new client.
     */
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

    /**
     * Filters clients based on search text.
     * @param searchText The text to search for in client names
     */
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



    /**
     * Adds a form field to a panel with a label.
     * @param panel The panel to add the field to
     * @param label The label text for the field
     * @param field The text field to add
     */
    private void addFormField(JPanel panel, String label, JTextField field) {
        JLabel jLabel = new JLabel(label);
        jLabel.setForeground(Color.WHITE);
        panel.add(jLabel);
        panel.add(field);

    }

    /**
     * Saves a new client to the database.
     * @param company The company name
     * @param contact The contact person's name
     * @param email The contact email
     * @param phone The contact phone number
     * @param address The street address
     * @param city The city
     * @param postcode The postal code
     * @param billingName The billing contact name
     * @param billingEmail The billing contact email
     */
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

    /**
     * Styles a button with consistent appearance.
     * @param button The button to style
     */
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

    /**
     * Creates the header panel with navigation and title.
     * @return JPanel containing the header components
     */
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
        JLabel titleLabel = new JLabel("Client Data");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 36));
        titlePanel.add(titleLabel);

        headerContainer.add(topBar);
        headerContainer.add(titlePanel);

        return headerContainer;
    }

    /**
     * Adds hover effects to a button.
     * @param button The button to add hover effects to
     */
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
