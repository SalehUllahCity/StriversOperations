package auth;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class Booking extends JFrame {

    private JTextField bookingNameField, clientNameField;
    private JTable bookingTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JSpinner startDateSpinner, endDateSpinner;

    private final Color backgroundColour = new Color(18, 32, 35, 255);
    private final int fontSize = 25;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Booking frame = new Booking();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create the booking frame UI
     */
    public Booking() {
        setTitle("Lancaster's Music Hall Software");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(backgroundColour);
        getContentPane().setLayout(null);

        JPanel topBar = createTopBar(this);
        topBar.setBounds(0, 0, 1200, 50);
        getContentPane().add(topBar);

        JLabel titleLabel = new JLabel("New Booking");
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(450, 20, 400, 50);
        getContentPane().add(titleLabel);
        getContentPane().setComponentZOrder(titleLabel, 0);

        buildInputFields();
        buildBookingTable();
        buildActions();
    }

    /**
     * Create input fields for booking details.
     */
    private void buildInputFields() {
        int labelHeight = 40;
        int fieldHeight = 40;
        int labelWidth = 200;
        int fieldWidth = 250;

        // Booking Name
        JLabel bookingNameLabel = createLabel("Booking Name:");
        bookingNameLabel.setBounds(100, 100, labelWidth, labelHeight);
        getContentPane().add(bookingNameLabel);

        bookingNameField = createTextField();
        bookingNameField.setBounds(300, 100, fieldWidth, fieldHeight);
        getContentPane().add(bookingNameField);

        // Client Name
        JLabel clientNameLabel = createLabel("Client Name:");
        clientNameLabel.setBounds(600, 100, labelWidth, labelHeight);
        getContentPane().add(clientNameLabel);

        clientNameField = createTextField();
        clientNameField.setBounds(800, 100, fieldWidth, fieldHeight);
        getContentPane().add(clientNameField);

        // Start Date
        JLabel startDateLabel = createLabel("Start Date:");
        startDateLabel.setBounds(100, 160, labelWidth, labelHeight);
        getContentPane().add(startDateLabel);

        startDateSpinner = createDateSpinner();
        startDateSpinner.setBounds(300, 160, fieldWidth, fieldHeight);
        getContentPane().add(startDateSpinner);

        // End Date
        JLabel endDateLabel = createLabel("End Date:");
        endDateLabel.setBounds(600, 160, labelWidth, labelHeight);
        getContentPane().add(endDateLabel);

        endDateSpinner = createDateSpinner();
        endDateSpinner.setBounds(800, 160, fieldWidth, fieldHeight);
        getContentPane().add(endDateSpinner);
    }

    /**
     * Top bar with Home and Settings buttons
     */
    private JPanel createTopBar(JFrame parentFrame) {
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setBackground(backgroundColour);
        topWrapper.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Home button: returns to UserHome
        JButton homeBtn = new JButton("← Home");
        styleTopButton(homeBtn);
        homeBtn.addActionListener(e -> {
            parentFrame.dispose();
            new UserHome().setVisible(true);
        });
        topWrapper.add(homeBtn, BorderLayout.WEST);

        // Settings button: opens settings dialog
        JButton settingsBtn = new JButton("⚙ Settings");
        styleTopButton(settingsBtn);
        settingsBtn.addActionListener(e -> new SettingScreen.SettingsDialog(parentFrame).setVisible(true));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(settingsBtn);
        topWrapper.add(rightPanel, BorderLayout.EAST);

        return topWrapper;
    }

    private void styleTopButton(JButton button) {
        button.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        button.setBackground(backgroundColour);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        addHoverEffect(button);
    }

    /**
     * Booking slots table
     */
    private void buildBookingTable() {
        String[] columns = {"Date", "Time", "Booking Space", "Rate Type", "Booking Type", "Cost"};
        tableModel = new DefaultTableModel(columns, 0);

        bookingTable = new JTable(tableModel);
        bookingTable.setFont(new Font("TimesRoman", Font.PLAIN, 20));
        bookingTable.setRowHeight(30);
        bookingTable.getTableHeader().setReorderingAllowed(false);
        bookingTable.getTableHeader().setResizingAllowed(false);

        JScrollPane tableScroll = new JScrollPane(bookingTable);
        tableScroll.setBounds(100, 220, 1000, 280);
        getContentPane().add(tableScroll);
    }

    /**
     * Buttons for slot management and save/cancel actions
     */
    private void buildActions() {
        createActionButton("Add Slot", 100, e -> addBookingSlot());
        createActionButton("Save Booking", 320, e -> JOptionPane.showMessageDialog(this, "Booking saved."));
        createActionButton("Cancel", 590, e -> dispose());

        totalLabel = new JLabel("Total: £0");
        totalLabel.setFont(new Font("TimesRoman", Font.PLAIN, fontSize));
        totalLabel.setForeground(Color.WHITE);
        totalLabel.setBounds(100, 590, 300, 40);
        getContentPane().add(totalLabel);
    }

    private void createActionButton(String text, int x, java.awt.event.ActionListener action) {
        JButton button = createStyledButton(text);
        button.setBounds(x, 520, 200, 50);
        button.addActionListener(action);
        getContentPane().add(button);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("TimesRoman", Font.PLAIN, fontSize));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("TimesRoman", Font.PLAIN, fontSize));
        return field;
    }

    private JSpinner createDateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd-MM-yyyy"));
        return spinner;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("TimesRoman", Font.PLAIN, fontSize));
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        addHoverEffect(button);
        return button;
    }

    private void addHoverEffect(JButton button) {
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

    //Dialog to add a booking slot
    private void addBookingSlot() {
        String[] rooms = {
                "The Green Room", "Bronte Boardroom", "Dickens Den", "Poe Parlor",
                "Globe Room", "Chekhov Chamber", "Main Hall", "Small Hall",
                "Rehearsal Space", "Entire Venue"
        };

        //Rate types by category
        String[] roomRates = {"1 Hour", "Morning/Afternoon", "All Day", "Week"};
        String[] venueRates = {"Evening Rate", "Full Day"};
        String[] performanceRates = {"Hourly Rate", "Evening Rate", "Daily Rate"};
        String[] rehearsalRates = {"Hourly Rate", "Daily Rate", "Weekly Rate"};

        JComboBox<String> roomBox = new JComboBox<>(rooms);
        JComboBox<String> rateBox = new JComboBox<>(roomRates); // default rates
        JComboBox<String> timeBox = new JComboBox<>(new String[]{
                "10:00", "11:00", "12:00", "13:00", "14:00",
                "15:00", "16:00", "17:00", "18:00", "19:00"
        });
        JTextField bookingTypeField = new JTextField();
        JSpinner slotDateSpinner = createDateSpinner();

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Date:")); panel.add(slotDateSpinner);
        panel.add(new JLabel("Time:")); panel.add(timeBox);
        panel.add(new JLabel("Room:")); panel.add(roomBox);
        panel.add(new JLabel("Rate Type:")); panel.add(rateBox);
        panel.add(new JLabel("Booking Type:")); panel.add(bookingTypeField);

        // Dynamically update rate options based on room selection
        roomBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedRoom = (String) roomBox.getSelectedItem();
                rateBox.removeAllItems();

                if (selectedRoom.equals("Main Hall") || selectedRoom.equals("Small Hall")) {
                    for (String rate : performanceRates) rateBox.addItem(rate);
                } else if (selectedRoom.equals("Rehearsal Space")) {
                    for (String rate : rehearsalRates) rateBox.addItem(rate);
                } else if (selectedRoom.equals("Entire Venue")) {
                    for (String rate : venueRates) rateBox.addItem(rate);
                } else {
                    for (String rate : roomRates) rateBox.addItem(rate);
                }
            }
        });

        //Show input dialog
        int result = JOptionPane.showConfirmDialog(this, panel, "Add Booking Slot", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            Date slotDate = (Date) slotDateSpinner.getValue();
            Date startDate = (Date) startDateSpinner.getValue();
            Date endDate = (Date) endDateSpinner.getValue();

            if (slotDate.before(startDate) || slotDate.after(endDate)) {
                JOptionPane.showMessageDialog(this, "Date must be within booking range.");
                return;
            }

            String room = (String) roomBox.getSelectedItem();
            String time = (String) timeBox.getSelectedItem();
            String rate = (String) rateBox.getSelectedItem();
            String booking = bookingTypeField.getText().trim();

            if (booking.isEmpty()) {
                JOptionPane.showMessageDialog(this, "BookingType cannot be empty.");
                return;
            }

            String[] bookingType = {
                    "Meeting", "Theatre", "Conference", "Seminar",
                    "Screenings", "Charity", "Fashion Shows",
                    "Other"
            };

            String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(slotDate);

            Vector<String> row = new Vector<>();
            row.add(formattedDate);
            row.add(time);
            row.add(room);
            row.add(rate);
            row.add(booking);

            //Get price from the map
            int cost = 0;
            if (ratePrices.containsKey(room) && ratePrices.get(room).containsKey(rate)) {
                cost = ratePrices.get(room).get(rate);
            }
            row.add("£" + cost);
            tableModel.addRow(row);
            updateTotalCost();
        }
    }

    //Booking rates per room and rate type
    private final java.util.Map<String, java.util.Map<String, Integer>> ratePrices = new java.util.HashMap<>() {{
        put("The Green Room", new java.util.HashMap<>() {{
            put("1 Hour", 25);
            put("Morning/Afternoon", 75);
            put("All Day", 130);
            put("Week", 600);
        }});
        put("Bronte Boardroom", new java.util.HashMap<>() {{
            put("1 Hour", 40);
            put("Morning/Afternoon", 120);
            put("All Day", 200);
            put("Week", 900);
        }});
        put("Dickens Den", new java.util.HashMap<>() {{
            put("1 Hour", 30);
            put("Morning/Afternoon", 90);
            put("All Day", 150);
            put("Week", 700);
        }});
        put("Poe Parlor", new java.util.HashMap<>() {{
            put("1 Hour", 35);
            put("Morning/Afternoon", 100);
            put("All Day", 170);
            put("Week", 800);
        }});
        put("Globe Room", new java.util.HashMap<>() {{
            put("1 Hour", 50);
            put("Morning/Afternoon", 150);
            put("All Day", 250);
            put("Week", 1100);
        }});
        put("Chekhov Chamber", new java.util.HashMap<>() {{
            put("1 Hour", 38);
            put("Morning/Afternoon", 110);
            put("All Day", 180);
            put("Week", 850);
        }});
        put("Main Hall", new java.util.HashMap<>() {{
            put("Hourly Rate", 75);
            put("Evening Rate", 250);
            put("Daily Rate", 450);
        }});
        put("Small Hall", new java.util.HashMap<>() {{
            put("Hourly Rate", 50);
            put("Evening Rate", 180);
            put("Daily Rate", 300);
        }});
        put("Rehearsal Space", new java.util.HashMap<>() {{
            put("Hourly Rate", 30);
            put("Daily Rate", 100);
            put("Weekly Rate", 400);
        }});
        put("Entire Venue", new java.util.HashMap<>() {{
            put("Evening Rate", 600);
            put("Full Day", 1000);
        }});
    }};

    /**
     * Calculates the total cost of all booking slots in the table
     * and updates the totalLabel with the sum.
     */
    private void updateTotalCost() {
        int total = 0;
        //Go through each row in the table
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            //Get the cost from column 5 (index starts at 0)
            Object value = tableModel.getValueAt(row, 5);
            if (value != null) {
                String costText = value.toString().replace("£", "").trim();
                //Try to turn the text into a number
                try {
                    int cost = Integer.parseInt(costText);
                    total += cost;
                } catch (NumberFormatException e) {
                    //If it's not a number, skip it
                }
            }
        }
        totalLabel.setText("Total: £" + total);
    }
}
