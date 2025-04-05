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
import java.util.Vector;

public class Reviews extends JFrame {
    private final Color background = new Color(18, 32, 35, 255);
    private final Color panelColor = new Color(30, 50, 55);
    private final Color buttonColor = new Color(40, 70, 75);
    private final int fontSize = 22;
    private JTable reviewsTable;
    private DefaultTableModel tableModel;

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
        String[] columns = {"ID", "User", "Booking ID", "Rating", "Comment", "Date", "Response"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Response column (index 6) is editable
            }
        };



        reviewsTable = new JTable(tableModel);
        styleTable(reviewsTable);
        JScrollPane scrollPane = new JScrollPane(reviewsTable);
        scrollPane.setBackground(background);

        // 2. Configure comment column rendering & row height
        reviewsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {

                // For comment column only (index 4)
                if (column == 4) {
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
                if (e.getClickCount() == 2) {
                    int row = reviewsTable.rowAtPoint(e.getPoint());
                    int col = reviewsTable.columnAtPoint(e.getPoint());

                    if (col == 4) {  // Comment column
                        showTextPopup("Full Comment", (String) tableModel.getValueAt(row, col), false);
                    }
                    else if (col == 6) {  // Response column
                        showTextPopup("Edit Response", (String) tableModel.getValueAt(row, col), true);
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

        reviewsTable.getColumnModel().getColumn(6).setCellEditor(responseEditor);

// Add listener to save changes to database
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 6) { // Only handle Response column changes
                int row = e.getFirstRow();
                int reviewId = (Integer) tableModel.getValueAt(row, 0); // Get ID from first column
                String response = (String) tableModel.getValueAt(row, 6);

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
        reviewsTable.getColumnModel().getColumn(4).setPreferredWidth(300);

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

        mainPanel.add(filterPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        return mainPanel;
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
    }

    private void saveResponseToDatabase() {
        int row = reviewsTable.getSelectedRow();
        int reviewId = (Integer) tableModel.getValueAt(row, 0);
        String response = (String) tableModel.getValueAt(row, 6);

        try {
            JDBC jdbc = new JDBC();
            jdbc.executeUpdate(
                    "UPDATE review SET response = ? WHERE review_id = ?",
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

    private void loadReviews() {
        try {
            tableModel.setRowCount(0); // Clear existing data

            // Connect to database and fetch reviews
            JDBC jdbc = new JDBC();
            ResultSet rs = jdbc.executeQuery("SELECT * FROM review ORDER BY ReviewDate DESC");

            while (rs.next()) {
                /*
                String type = rs.getString("BookingID"); //.startsWith("SHOW") ? "Show" : "Venue";
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("ReviewID"));
                row.add(type);
                row.add(rs.getString("user_name"));
                row.add(rs.getString("booking_id"));
                row.add(rs.getInt("rating"));
                row.add(rs.getString("comment"));
                row.add(rs.getDate("review_date"));
                row.add(rs.getString("response"));

                 */
                Object[] row = {
                        rs.getInt("ReviewID"),
                        rs.getInt("UserID"),
                        rs.getInt("BookingID"),
                        rs.getInt("Rating"),
                        rs.getString("Comment"),
                        rs.getDate("ReviewDate"),
                        rs.getString("Response")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading reviews: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void applyFilter(int filterType) {
        try {
            tableModel.setRowCount(0);
            JDBC jdbc = new JDBC();
            String query = "SELECT * FROM review ";

            switch (filterType) {
                case 1: query += "WHERE booking_id NOT LIKE 'SHOW%'"; break; // Venue only
                case 2: query += "WHERE booking_id LIKE 'SHOW%'"; break; // Show only
                case 3: query += "WHERE rating >= 4"; break; // High ratings
                case 4: query += "WHERE rating <= 3"; break; // Low ratings
                default: break; // All reviews
            }

            query += " ORDER BY ReviewDate DESC";
            ResultSet rs = jdbc.executeQuery(query);

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("ReviewID"),
                        rs.getInt("UserID"),
                        rs.getInt("BookingID"),
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
            // Get values using correct column indices
            int reviewId = (Integer) tableModel.getValueAt(selectedRow, 0); // ID is column 0
            String response = (String) tableModel.getValueAt(selectedRow, 6); // Response is column 6

            if (response == null || response.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a response",
                        "Empty Response", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Update database
            try {
                JDBC jdbc = new JDBC();
                jdbc.executeUpdate("UPDATE review SET Response = ? WHERE ReviewID = ?",
                        response, reviewId);

                // Refresh the table to show changes
                loadReviews();

                JOptionPane.showMessageDialog(this, "Response submitted successfully",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (HeadlessException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error submitting response: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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
            String idStr = tableModel.getValueAt(selectedRow, 0).toString();
            int id = Integer.parseInt(idStr);

            String type = tableModel.getValueAt(selectedRow, 1).toString();
            String user = tableModel.getValueAt(selectedRow, 2).toString();
            String bookingId = tableModel.getValueAt(selectedRow, 3).toString();

            String ratingStr = tableModel.getValueAt(selectedRow, 4).toString();
            int rating = Integer.parseInt(ratingStr);

            String comment = tableModel.getValueAt(selectedRow, 5).toString();
            String date = tableModel.getValueAt(selectedRow, 6).toString();

            Object responseObj = tableModel.getValueAt(selectedRow, 7);
            String response = (responseObj != null) ? responseObj.toString() : "No response";

            // Format for export
            String exportText = String.format(
                    "Review Details:\n\nID: %s\nType: %s\nUser: %s\nBooking ID: %s\n" +
                            "Rating: %d/5\nDate: %s\n\nComment:\n%s\n\nResponse:\n%s",
                    idStr, type, user, bookingId, rating, date, comment, response);

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
