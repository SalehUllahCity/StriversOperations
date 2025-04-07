package auth;

import BoxOfficeInterface.JDBC;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Reviews extends JFrame {
    private final Color background = new Color(18, 32, 35, 255);
    private final Color panelColor = new Color(30, 50, 55);
    private final Color buttonColor = new Color(40, 70, 75);
    private final int fontSize = 22;
    private JTable reviewsTable;
    private DefaultTableModel tableModel;
    Map<Integer, Boolean> sentReviews = new HashMap<>();

    public Reviews() {
        setTitle("Lancaster's Music Hall Software: Reviews");
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

        loadReviews();
    }



    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(background);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create a panel for the filter and legend
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(background);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(panelColor);
        filterPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80)),
                "Filter Reviews",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("TimesRoman", Font.BOLD, 18),  // Larger font for title
                Color.WHITE));  // White text color

        // "All Reviews" label - now properly styled
        JLabel filterLabel = new JLabel("Filter By:");
        filterLabel.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        filterLabel.setForeground(Color.WHITE);

        // Add legend panel to the right of filter panel
        topPanel.add(createLegendPanel(), BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        String[] filterOptions = {"All Reviews", "Venue Reviews", "Show Reviews", "High Ratings (4-5)", "Low Ratings (1-3)"};
        JComboBox<String> filterCombo = new JComboBox<>(filterOptions);
        styleComboBox(filterCombo);

        JButton filterButton = new JButton("Apply Filter");
        styleButton(filterButton);
        filterButton.addActionListener(e -> applyFilter(filterCombo.getSelectedIndex()));

        filterPanel.add(filterLabel);  // Now using styled label instead of raw text
        filterPanel.add(filterCombo);
        filterPanel.add(filterButton);

        // Reviews table
        String[] columns = {"ReviewID", "User Email", "Client Name", "Booking Name", "Rating", "Comment", "Date", "Response"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only Response column (index 6) is editable
            }
        };

        reviewsTable = new JTable(tableModel);
        styleTable(reviewsTable);
        JScrollPane scrollPane = new JScrollPane(reviewsTable);
        scrollPane.setBackground(background);

        ReviewRenderer renderer = new ReviewRenderer();
        for (int i = 0; i < reviewsTable.getColumnCount(); i++) {
            reviewsTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // 2. Configure comment column rendering & row height
        reviewsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {

                // For comment column only (index 4)
                if (column == 5) {
                    JTextArea textArea = new JTextArea(value != null ? value.toString() : "");
                    textArea.setLineWrap(true);
                    textArea.setWrapStyleWord(true);
                    textArea.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                    textArea.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                    textArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

                    // Calculate preferred height
                    FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
                    int textWidth = textArea.getPreferredSize().width;
                    int availableWidth = table.getColumnModel().getColumn(column).getWidth() - 8;
                    int lineCount = (textWidth > 0) ? (int) Math.ceil((double) textWidth / availableWidth) : 1;
                    int preferredHeight = fm.getHeight() * lineCount + 8;

                    // Set row height
                    if (table.getRowHeight(row) < preferredHeight) {
                        table.setRowHeight(row, preferredHeight);
                    }

                    return textArea;
                }

                // Default renderer for other columns
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                label.setToolTipText(value != null ? value.toString() : null);
                return label;
            }

        });



        reviewsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = reviewsTable.rowAtPoint(e.getPoint());
                int col = reviewsTable.columnAtPoint(e.getPoint());

                if (row >= 0) {
                    if (col == 5) {  // Comment column (now index 5)
                        String comment = (String) tableModel.getValueAt(row, col);
                        showTextPopup("Full Review Comment", comment, false);
                    }
                    else if (col == 7) {  // Response column (now index 7)
                        String response = (String) tableModel.getValueAt(row, col);
                        showTextPopup("Edit Response", response, true);
                    }
                }
            }
        });





        reviewsTable.putClientProperty("terminateEditOnFocusLost", true); // Saves when clicking away

// Custom editor for the Response column
        DefaultCellEditor responseEditor = new DefaultCellEditor(new JTextField()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                                                         boolean isSelected, int row, int column) {
                Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
                c.setBackground(new Color(60, 90, 100)); // Editor background color
                c.setForeground(Color.WHITE); // Editor text color
                return c;
            }
        };

        reviewsTable.getColumnModel().getColumn(7).setCellEditor(responseEditor);

// Add listener to save changes to database
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 7) { // Now checking column 7 for Response
                int row = e.getFirstRow();
                int reviewId = (Integer) tableModel.getValueAt(row, 0);
                String response = (String) tableModel.getValueAt(row, 7);

                // Save to database in background
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        try {
                            JDBC jdbc = new JDBC();
                            jdbc.executeUpdate(
                                    "UPDATE review SET Response = ? WHERE ReviewID = ?",
                                    response,
                                    reviewId
                            );
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        // Optional: Show success message
                        JOptionPane.showMessageDialog(Reviews.this,
                                "Response saved successfully",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }.execute();
            }
        });

        // 3. Set comment column width
        reviewsTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // ReviewID
        reviewsTable.getColumnModel().getColumn(1).setPreferredWidth(130); // User Email
        reviewsTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Client Name
        reviewsTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Booking Name
        reviewsTable.getColumnModel().getColumn(4).setPreferredWidth(60);  // Rating
        reviewsTable.getColumnModel().getColumn(5).setPreferredWidth(200); // Comment
        reviewsTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Date
        reviewsTable.getColumnModel().getColumn(7).setPreferredWidth(150); // Response

        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setBackground(panelColor);

        JButton respondButton = new JButton("Submit Response");
        styleButton(respondButton);
        respondButton.addActionListener(e -> submitResponse());

        JButton refreshButton = new JButton("Refresh");
        styleButton(refreshButton);
        refreshButton.addActionListener(e -> loadReviews());

        JButton exportButton = new JButton("Export Selected");
        styleButton(exportButton);
        exportButton.addActionListener(e -> exportReview());

        actionPanel.add(respondButton);
        actionPanel.add(refreshButton);
        actionPanel.add(exportButton);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(background);

        // Add legend just above the table
        centerPanel.add(createSimpleLegend(), BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(reviewsTable), BorderLayout.CENTER);


        mainPanel.add(filterPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JButton sendToClientButton = new JButton("Sent to Client?");
        styleButton(sendToClientButton);
        sendToClientButton.addActionListener(e -> sendToClient());
        actionPanel.add(sendToClientButton);

        JButton addReviewButton = new JButton("Add Review");
        styleButton(addReviewButton);
        addReviewButton.addActionListener(e -> showAddReviewDialog());
        actionPanel.add(addReviewButton);

        return mainPanel;
    }

    private void sendToClient() {
        int selectedRow = reviewsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a review to send to client",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reviewId = (Integer) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to send this review to the client?",
                "Confirm Send to Client",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Update database
            updateSentStatusInDatabase(reviewId);

            // Update our map
            sentReviews.put(reviewId, true);

            // Refresh the display
            reviewsTable.repaint();
        }
    }

    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        legendPanel.setBackground(panelColor);
        legendPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80)),
                "Legend",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("TimesRoman", Font.BOLD, 18),
                Color.WHITE));

        // Create legend item
        JPanel legendItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        legendItem.setBackground(panelColor);

        // Color indicator
        JLabel colorLabel = new JLabel();
        colorLabel.setOpaque(true);
        colorLabel.setBackground(new Color(50, 120, 70)); // Same green as your rows
        colorLabel.setPreferredSize(new Dimension(20, 20));
        colorLabel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        // Text label
        JLabel textLabel = new JLabel("Sent to Client");
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(new Font("TimesRoman", Font.PLAIN, 16));

        legendItem.add(colorLabel);
        legendItem.add(textLabel);

        legendPanel.add(legendItem);

        return legendPanel;
    }


    private void updateSentStatusInDatabase(int reviewId) {
        try {
            JDBC jdbc = new JDBC();
            jdbc.executeUpdate(
                    "UPDATE review SET SentToClient = 1 WHERE ReviewID = ?",
                    reviewId
            );
        } catch (SQLException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating sent status: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    private void saveResponseToDatabase() {
        int row = reviewsTable.getSelectedRow();
        if (row == -1) return;

        int reviewId = (Integer) tableModel.getValueAt(row, 0); // ReviewID is column 0
        String response = (String) tableModel.getValueAt(row, 7); // Response is column 7

        try {
            JDBC jdbc = new JDBC();
            jdbc.executeUpdate(
                    "UPDATE review SET Response = 1 WHERE ReviewID = ?",
                    response,
                    reviewId
            );
        } catch (SQLException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving response: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateAndSubmitReview(String email, String client, String booking, int rating, String comment) {
        // Basic validation
        if (email.isEmpty() || client == null || booking == null || comment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (rating < 1 || rating > 5) {
            JOptionPane.showMessageDialog(this, "Rating must be between 1 and 5",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            JDBC jdbc = new JDBC();

            // First get the UserID from email
            ResultSet rs = jdbc.executeQuery(
                    "SELECT UserID FROM user WHERE Email = ?",
                    email
            );

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "User with this email not found",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            int userId = rs.getInt("UserID");

            // Get BookingID from booking name (more reliable now since it's from dropdown)
            rs = jdbc.executeQuery(
                    "SELECT BookingID FROM booking WHERE BookingName = ?",
                    booking
            );

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Booking not found",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            int bookingId = rs.getInt("BookingID");

            // Insert the review
            jdbc.executeUpdate(
                    "INSERT INTO review (UserID, BookingID, Rating, Comment, ReviewDate, SentToClient) " +
                            "VALUES (?, ?, ?, ?, CURRENT_DATE, FALSE)",
                    userId, bookingId, rating, comment
            );

            // Refresh the table
            loadReviews();

            JOptionPane.showMessageDialog(this, "Review added successfully",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;

        } catch (SQLException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Error adding review: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        field.setBackground(new Color(60, 90, 100));
        field.setForeground(Color.WHITE);
        field.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        spinner.setBackground(new Color(60, 90, 100));
        spinner.setForeground(Color.WHITE);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor)editor).getTextField();
            tf.setBackground(new Color(60, 90, 100));
            tf.setForeground(Color.WHITE);
            tf.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        }
    }

    private void loadClientNames(JComboBox<String> combo) {
        try {
            JDBC jdbc = new JDBC();
            ResultSet rs = jdbc.executeQuery("SELECT DISTINCT Client FROM booking ORDER BY Client");
            while (rs.next()) {
                combo.addItem(rs.getString("Client"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error loading client names: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBookingNames(JComboBox<String> combo) {
        try {
            JDBC jdbc = new JDBC();
            ResultSet rs = jdbc.executeQuery("SELECT BookingName FROM booking ORDER BY BookingName");
            while (rs.next()) {
                combo.addItem(rs.getString("BookingName"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error loading booking names: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddReviewDialog() {
        JDialog dialog = new JDialog(this, "Add New Review", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 500);  // Slightly taller to accommodate dropdowns
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(panelColor);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form fields
        JLabel emailLabel = new JLabel("User Email:");
        emailLabel.setForeground(Color.WHITE);
        JTextField emailField = new JTextField(20);
        styleTextField(emailField);

        // Client Name Dropdown
        JLabel clientLabel = new JLabel("Client Name:");
        clientLabel.setForeground(Color.WHITE);
        JComboBox<String> clientCombo = new JComboBox<>();
        styleComboBox(clientCombo);
        loadClientNames(clientCombo);

        // Booking Name Dropdown
        JLabel bookingLabel = new JLabel("Booking Name:");
        bookingLabel.setForeground(Color.WHITE);
        JComboBox<String> bookingCombo = new JComboBox<>();
        styleComboBox(bookingCombo);
        loadBookingNames(bookingCombo);

        // Rating
        JLabel ratingLabel = new JLabel("Rating (1-5):");
        ratingLabel.setForeground(Color.WHITE);
        JSpinner ratingSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        styleSpinner(ratingSpinner);

        // Comment
        JLabel commentLabel = new JLabel("Comment:");
        commentLabel.setForeground(Color.WHITE);
        JTextArea commentArea = new JTextArea(3, 20);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JScrollPane commentScroll = new JScrollPane(commentArea);

        // Add components to panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(emailLabel, gbc);
        gbc.gridy++;
        panel.add(clientLabel, gbc);
        gbc.gridy++;
        panel.add(bookingLabel, gbc);
        gbc.gridy++;
        panel.add(ratingLabel, gbc);
        gbc.gridy++;
        panel.add(commentLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(emailField, gbc);
        gbc.gridy++;
        panel.add(clientCombo, gbc);
        gbc.gridy++;
        panel.add(bookingCombo, gbc);
        gbc.gridy++;
        panel.add(ratingSpinner, gbc);
        gbc.gridy++;
        panel.add(commentScroll, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(panelColor);

        JButton submitButton = new JButton("Submit");
        styleButton(submitButton);
        submitButton.addActionListener(e -> {
            // Show confirmation dialog
            int confirm = JOptionPane.showConfirmDialog(
                    dialog,
                    "Are you sure you want to submit this review?",
                    "Confirm Submission",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (validateAndSubmitReview(
                        emailField.getText(),
                        (String) clientCombo.getSelectedItem(),
                        (String) bookingCombo.getSelectedItem(),
                        (Integer) ratingSpinner.getValue(),
                        commentArea.getText()
                )) {
                    dialog.dispose();
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton);
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showTextPopup(String title, String text, boolean editable) {
        // Create text area with proper settings
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(editable);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 10, 10, 10));

        // Scroll pane with constrained size
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        // Create dialog buttons
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        // Only show buttons for editable (response) popups
        JPanel buttonPanel = new JPanel();
        if (editable) {
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
        }

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        if (editable) {
            contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        }

        // Create the dialog
        JDialog dialog = new JDialog(this, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.getContentPane().add(contentPanel);

        // Button actions
        saveButton.addActionListener(e -> {
            tableModel.setValueAt(textArea.getText(),
                    reviewsTable.getSelectedRow(), 7); // Update table
            saveResponseToDatabase(); // Save to DB
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        // Configure dialog
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(true);
        dialog.setVisible(true);

        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private class ReviewRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Get the ReviewID from column 0
            int reviewId = (Integer) tableModel.getValueAt(row, 0);

            if (sentReviews.containsKey(reviewId) && sentReviews.get(reviewId)) {
                c.setBackground(new Color(50, 120, 70)); // Dark green
                c.setForeground(Color.WHITE);
            } else {
                // Default colors
                c.setBackground(isSelected ? table.getSelectionBackground() : panelColor);
                c.setForeground(isSelected ? table.getSelectionForeground() : Color.WHITE);
            }

            return c;
        }
    }

    private void loadReviews() {
        try {
            tableModel.setRowCount(0);
            sentReviews.clear(); // Clear previous data

            JDBC jdbc = new JDBC();
            String query = "SELECT r.ReviewID, u.Email, b.Client, b.BookingName, " +
                    "r.Rating, r.Comment, r.ReviewDate, r.Response, r.SentToClient " +
                    "FROM review r " +
                    "LEFT JOIN user u ON r.UserID = u.UserID " +
                    "LEFT JOIN booking b ON r.BookingID = b.BookingID " +
                    "ORDER BY r.ReviewDate DESC";

            ResultSet rs = jdbc.executeQuery(query);

            while (rs.next()) {
                int reviewId = rs.getInt("ReviewID");
                boolean isSent = rs.getBoolean("SentToClient");

                // Store in our map
                sentReviews.put(reviewId, isSent);

                // Add to table model (without SentToClient column)
                Object[] row = {
                        reviewId,
                        rs.getString("Email"),
                        rs.getString("Client"),
                        rs.getString("BookingName"),
                        rs.getInt("Rating"),
                        rs.getString("Comment"),
                        rs.getDate("ReviewDate"),
                        rs.getString("Response")
                };
                tableModel.addRow(row);
            }

            reviewsTable.repaint();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading reviews: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private JPanel createSimpleLegend() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        legendPanel.setBackground(panelColor);

        // Create the colored box
        JLabel colorBox = new JLabel("  ");
        colorBox.setOpaque(true);
        colorBox.setBackground(new Color(50, 120, 70)); // Same green as your rows
        colorBox.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        colorBox.setPreferredSize(new Dimension(20, 20));

        // Create the text label
        JLabel textLabel = new JLabel("Sent to Client");
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(new Font("TimesRoman", Font.PLAIN, 16));

        legendPanel.add(colorBox);
        legendPanel.add(textLabel);

        return legendPanel;
    }

    private void applyFilter(int filterType) {
        try {
            tableModel.setRowCount(0);
            JDBC jdbc = new JDBC();
            String query = "SELECT r.ReviewID, u.Email, b.Client, b.BookingName, " +
                    "r.Rating, r.Comment, r.ReviewDate, r.Response " +
                    "FROM review r " +
                    "LEFT JOIN user u ON r.UserID = u.UserID " +
                    "LEFT JOIN booking b ON r.BookingID = b.BookingID ";

            switch (filterType) {
                case 1: query += "WHERE b.BookingName NOT LIKE 'SHOW%'"; break; // Venue only
                case 2: query += "WHERE b.BookingName LIKE 'SHOW%'"; break; // Show only
                case 3: query += "WHERE r.Rating >= 4"; break; // High ratings
                case 4: query += "WHERE r.Rating <= 3"; break; // Low ratings
                default: break; // All reviews
            }

            query += " ORDER BY r.ReviewDate DESC";
            ResultSet rs = jdbc.executeQuery(query);

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("ReviewID"),
                        rs.getString("Email"),
                        rs.getString("Client"),
                        rs.getString("BookingName"),
                        rs.getInt("Rating"),
                        rs.getString("Comment"),
                        rs.getDate("ReviewDate"),
                        rs.getString("Response")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error filtering reviews: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void submitResponse() {
        int selectedRow = reviewsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a review to respond to",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int reviewId = (Integer) tableModel.getValueAt(selectedRow, 0);
            String response = (String) tableModel.getValueAt(selectedRow, 7); // Column 7

            if (response == null || response.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a response",
                        "Empty Response", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JDBC jdbc = new JDBC();
            jdbc.executeUpdate("UPDATE review SET Response = ? WHERE ReviewID = ?",
                    response, reviewId);

            loadReviews(); // Refresh to show changes
            JOptionPane.showMessageDialog(this, "Response submitted successfully",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error submitting response: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportReview() {
        int selectedRow = reviewsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a review to export",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Safely get all values with proper type conversion
            String reviewId = tableModel.getValueAt(selectedRow, 0).toString();
            String userEmail = tableModel.getValueAt(selectedRow, 1).toString();
            String clientName = tableModel.getValueAt(selectedRow, 2).toString();
            String bookingName = tableModel.getValueAt(selectedRow, 3).toString();

            String ratingStr = tableModel.getValueAt(selectedRow, 4).toString();
            int rating = Integer.parseInt(ratingStr);

            String comment = tableModel.getValueAt(selectedRow, 5).toString();
            String date = tableModel.getValueAt(selectedRow, 6).toString();

            Object responseObj = tableModel.getValueAt(selectedRow, 7);
            String response = (responseObj != null) ? responseObj.toString() : "No response";

            // Format for export
            String exportText = String.format(
                    "Review Details:\n\nReview ID: %s\nUser Email: %s\nClient: %s\nBooking: %s\n" +
                            "Rating: %d/5\nDate: %s\n\nComment:\n%s\n\nResponse:\n%s",
                    reviewId, userEmail, clientName, bookingName, rating, date, comment, response);

            // Copy to clipboard
            StringSelection selection = new StringSelection(exportText);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

            JOptionPane.showMessageDialog(this,
                    "Review copied to clipboard!\nYou can now paste it anywhere.",
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error exporting review: " + e.getMessage() +
                            "\nPlease ensure all fields have valid values.",
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Styling methods
    private void styleTable(JTable table) {
        table.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        table.setBackground(panelColor);
        table.setForeground(Color.WHITE);
        table.setGridColor(Color.DARK_GRAY);
        table.setSelectionBackground(new Color(60, 90, 100));
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("TimesRoman", Font.BOLD, 18));
        table.getTableHeader().setBackground(buttonColor);
        table.getTableHeader().setForeground(Color.WHITE);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        button.setBackground(buttonColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(50, 90, 100));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(buttonColor);
            }
        });
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        combo.setBackground(panelColor);
        combo.setForeground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
    }






    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Reviews frame = new Reviews();
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
        JLabel titleLabel = new JLabel("Reviews");
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
