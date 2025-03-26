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

    public Booking() {
        setTitle("Lancaster's Music Hall Software - New Booking");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(backgroundColour);
        getContentPane().setLayout(null);

        //Title
        JLabel titleLabel = new JLabel("New Booking");
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(450, 20, 400, 50);
        getContentPane().add(titleLabel);

        //Booking and Client name inputs
        JLabel bookingNameLabel = createLabel("Booking Name:");
        bookingNameLabel.setBounds(100, 100, 200, 40);
        getContentPane().add(bookingNameLabel);

        bookingNameField = createTextField();
        bookingNameField.setBounds(300, 100, 250, 40);
        getContentPane().add(bookingNameField);

        JLabel clientNameLabel = createLabel("Client Name:");
        clientNameLabel.setBounds(600, 100, 200, 40);
        getContentPane().add(clientNameLabel);

        clientNameField = createTextField();
        clientNameField.setBounds(800, 100, 250, 40);
        getContentPane().add(clientNameField);

        //Start and End date pickers
        JLabel startDateLabel = createLabel("Start Date:");
        startDateLabel.setBounds(100, 150, 200, 40);
        getContentPane().add(startDateLabel);

        startDateSpinner = createDateSpinner();
        startDateSpinner.setBounds(300, 150, 250, 40);
        getContentPane().add(startDateSpinner);

        JLabel endDateLabel = createLabel("End Date:");
        endDateLabel.setBounds(600, 150, 200, 40);
        getContentPane().add(endDateLabel);

        endDateSpinner = createDateSpinner();
        endDateSpinner.setBounds(800, 150, 250, 40);
        getContentPane().add(endDateSpinner);

        //Table to show added booking slots
        String[] columns = {"Date", "Time", "Booking Space", "Rate Type", "Purpose", "Cost"};
        tableModel = new DefaultTableModel(columns, 0);
        bookingTable = new JTable(tableModel);
        bookingTable.setFont(new Font("TimesRoman", Font.PLAIN, 20));
        bookingTable.setRowHeight(30);
        bookingTable.getTableHeader().setReorderingAllowed(false);
        bookingTable.getTableHeader().setResizingAllowed(false);
        JScrollPane tableScroll = new JScrollPane(bookingTable);
        tableScroll.setBounds(100, 220, 1000, 280);
        getContentPane().add(tableScroll);

        //Buttons
        JButton addSlotBtn = createStyledButton("Add Slot");
        addSlotBtn.setBounds(100, 520, 200, 50);
        getContentPane().add(addSlotBtn);

        JButton calculateBtn = createStyledButton("Calculate Cost");
        calculateBtn.setBounds(320, 520, 250, 50);
        getContentPane().add(calculateBtn);

        JButton saveBtn = createStyledButton("Save Booking");
        saveBtn.setBounds(590, 520, 200, 50);
        getContentPane().add(saveBtn);

        JButton cancelBtn = createStyledButton("Cancel");
        cancelBtn.setBounds(810, 520, 150, 50);
        getContentPane().add(cancelBtn);

        //Total cost display
        totalLabel = new JLabel("Total: £0");
        totalLabel.setFont(new Font("TimesRoman", Font.PLAIN, fontSize));
        totalLabel.setForeground(Color.WHITE);
        totalLabel.setBounds(100, 590, 300, 40);
        getContentPane().add(totalLabel);

        //Actions
        addSlotBtn.addActionListener(e -> addBookingSlot());
        calculateBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Cost calculation not implemented."));
        saveBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Booking saved."));
        cancelBtn.addActionListener(e -> dispose());
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
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
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
        JTextField purposeField = new JTextField();
        JSpinner slotDateSpinner = createDateSpinner();

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Date:")); panel.add(slotDateSpinner);
        panel.add(new JLabel("Time:")); panel.add(timeBox);
        panel.add(new JLabel("Room:")); panel.add(roomBox);
        panel.add(new JLabel("Rate Type:")); panel.add(rateBox);
        panel.add(new JLabel("Purpose:")); panel.add(purposeField);

        //Dynamically update rate options based on room selection
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

        // Show input dialog
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
            String purpose = purposeField.getText().trim();

            if (purpose.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Purpose cannot be empty.");
                return;
            }

            String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(slotDate);

            Vector<String> row = new Vector<>();
            row.add(formattedDate);
            row.add(time);
            row.add(room);
            row.add(rate);
            row.add(purpose);
            row.add("£0"); //Placeholder cost for now
            tableModel.addRow(row);
        }
    }

}
