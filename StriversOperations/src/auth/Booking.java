package auth;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class Booking extends JFrame {
    private final Color darkColour = new Color(18, 32, 35);
    private final Color panelColor = new Color(30, 50, 55);
    private final Color availableColor = new Color(76, 175, 80);
    private final Color unavailableColor = new Color(244, 67, 54);
    private final Color selectedColor = new Color(255, 152, 0);
    private final Color buttonHoverColor = Color.LIGHT_GRAY;
    private final int fontSize = 18;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy");
    
    private final String[] spaces = {
            "Select a space...",
            "The Green Room", "Bronte Boardroom", "Dickens Den", "Poe Parlor",
            "Globe Room", "Chekhov Chamber", "Main Hall", "Small Hall",
            "Rehearsal Space", "Entire Venue"
    };

    private final Map<String, Color> spaceColors = new HashMap<>();
    
    {
        // Initialize space colors
        spaceColors.put("The Green Room", new Color(76, 175, 80));    // Green
        spaceColors.put("Bronte Boardroom", new Color(33, 150, 243)); // Blue
        spaceColors.put("Dickens Den", new Color(156, 39, 176));      // Purple
        spaceColors.put("Poe Parlor", new Color(255, 152, 0));        // Orange
        spaceColors.put("Globe Room", new Color(0, 188, 212));        // Cyan
        spaceColors.put("Chekhov Chamber", new Color(233, 30, 99));   // Pink
        spaceColors.put("Main Hall", new Color(244, 67, 54));         // Red
        spaceColors.put("Small Hall", new Color(255, 235, 59));       // Yellow
        spaceColors.put("Rehearsal Space", new Color(63, 81, 181));   // Indigo
        spaceColors.put("Entire Venue", new Color(96, 125, 139));     // Blue Grey
    }

    private JTextField bookingNameField;
    private JTable timeSlotTable;
    private DefaultTableModel timeSlotModel;
    private JLabel totalLabel;
    private JLabel dateLabel;
    private JPanel calendarGrid;
    private JLabel monthYearLabel;
    private JComboBox<String> spaceSelector;
    private JList<String> bookingList;
    private DefaultListModel<String> bookingListModel;
    private JCheckBox allDayCheckBox;
    private JCheckBox eveningCheckBox;

    private LocalDate selectedDate = LocalDate.now();
    private String selectedSpace = null;
    private List<BookingEvent> bookingEvents = new ArrayList<>();
    private Map<String, Map<LocalDate, Map<LocalTime, Boolean>>> spaceAvailability = new HashMap<>();

    private static class BookingEvent {
        LocalDate date;
        LocalTime startTime;
        LocalTime endTime;
        String space;
        String eventType;
        double cost;
        boolean isAllDay;
        boolean isEvening;

        public BookingEvent(LocalDate date, LocalTime startTime, LocalTime endTime,
                            String space, String eventType, boolean isAllDay, boolean isEvening) {
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.space = space;
            this.eventType = eventType;
            this.isAllDay = isAllDay;
            this.isEvening = isEvening;
            this.cost = calculateCost();
        }

        private double calculateCost() {
            int dayOfWeek = date.getDayOfWeek().getValue(); // 1 = Monday, 6 = Saturday, 7 = Sunday
            boolean isWeekend = dayOfWeek == 6;  // Only Saturday for weekend rates
            boolean isWeekdayOrSunday = dayOfWeek >= 1 && dayOfWeek <= 4 || dayOfWeek == 7; // Sun-Thu
            int hours = (int) java.time.Duration.between(startTime, endTime).toHours();

            switch (space) {
                case "Entire Venue":
                    if (isAllDay) {
                        return isWeekend ? 9500.0 : 8500.0;
                    } else if (isEvening) {
                        return isWeekend ? 6750.0 : 6250.0;
                    }
                    return 0.0;

                case "Main Hall":
                    if (isAllDay) {
                        return isWeekend ? 4200.0 : 3800.0;
                    } else if (isEvening) {
                        return isWeekend ? 2200.0 : 1850.0;
                    } else if (hours >= 3) {
                        // Minimum 3 hours at £325 per hour
                        return hours * 325.0;
                    }
                    return 0.0;

                case "Small Hall":
                    if (isAllDay) {
                        return isWeekend ? 2500.0 : 2200.0;
                    } else if (isEvening) {
                        return isWeekend ? 1300.0 : 950.0;
                    } else if (hours >= 3) {
                        // Minimum 3 hours at £225 per hour
                        return hours * 225.0;
                    }
                    return 0.0;

                case "Rehearsal Space":
                    if (isAllDay) {
                        return isWeekend ? 500.0 : 450.0;
                    } else {
                        // Minimum 3 hours at £60 per hour
                        return Math.max(3, hours) * 60.0;
                    }

                case "The Green Room":
                    return hours >= 8 ? 130.0 : hours * 25.0;

                case "Bronte Boardroom":
                    return hours >= 8 ? 200.0 : hours * 40.0;

                case "Dickens Den":
                    return hours >= 8 ? 150.0 : hours * 30.0;

                case "Poe Parlor":
                    return hours >= 8 ? 170.0 : hours * 35.0;

                case "Globe Room":
                    return hours >= 8 ? 250.0 : hours * 50.0;

                case "Chekhov Chamber":
                    return hours >= 8 ? 180.0 : hours * 38.0;

                default:
                    return 0.0;
            }
        }
    }

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
        // Load the database driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading database driver: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        setupFrame();
        initializeData();
        createUI();
        updateTimeSlotGrid();
    }

    private void setupFrame() {
        setTitle("Lancaster's Music Hall Software - New Booking");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(darkColour);
        getContentPane().setLayout(new BorderLayout());
    }

    private void initializeData() {
        initializeSpaceAvailability();
        initializeTimeSlotModel();
        loadExistingBookings();
    }

    private void createUI() {
        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setBackground(darkColour);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainContent.add(createTopBar(this), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        centerPanel.setBackground(darkColour);

        centerPanel.add(createLeftPanel());
        centerPanel.add(createMiddlePanel());
        centerPanel.add(createRightPanel());

        mainContent.add(centerPanel, BorderLayout.CENTER);
        add(mainContent);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("TimesRoman", Font.PLAIN, 14));
        button.setBackground(darkColour);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(buttonHoverColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(Color.WHITE);
            }
        });
        
        return button;
    }

    private Border createHoverBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)
        );
    }

    private Border createNormalBorder() {
        return BorderFactory.createEmptyBorder(5, 5, 5, 5);
    }

    private void initializeSpaceAvailability() {
        for (String space : spaces) {
            if (!space.equals("Select a space...")) {
                Map<LocalDate, Map<LocalTime, Boolean>> dateMap = new HashMap<>();
                spaceAvailability.put(space, dateMap);
            }
        }
    }

    private Map<LocalTime, Boolean> getOrCreateTimeSlots(String space, LocalDate date) {
        Map<LocalDate, Map<LocalTime, Boolean>> dateMap = spaceAvailability.get(space);
        if (!dateMap.containsKey(date)) {
            Map<LocalTime, Boolean> timeSlots = new HashMap<>();
            for (int hour = 10; hour <= 20; hour++) {
                timeSlots.put(LocalTime.of(hour, 0), true);
                if (hour < 20) {
                    timeSlots.put(LocalTime.of(hour, 30), true);
                }
            }
            dateMap.put(date, timeSlots);
        }
        return dateMap.get(date);
    }

    private void initializeTimeSlotModel() {
        String[] columnNames = {"Time", "Status"};
        timeSlotModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Add rows for each time slot
        for (int hour = 10; hour <= 20; hour++) {
            timeSlotModel.addRow(new Object[]{String.format("%02d:00", hour), ""});
            if (hour < 20) {
                timeSlotModel.addRow(new Object[]{String.format("%02d:30", hour), ""});
            }
        }
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(panelColor);
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Month view panel without border
        JPanel monthViewPanel = new JPanel(new BorderLayout());
        monthViewPanel.setBackground(panelColor);
        
        // Add title
        JLabel titleLabel = new JLabel("Select Date");
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        monthViewPanel.add(titleLabel, BorderLayout.NORTH);

        // Add the rest of the month view components
        monthViewPanel.add(createMonthViewContent(), BorderLayout.CENTER);
        leftPanel.add(monthViewPanel, BorderLayout.NORTH);
        leftPanel.add(createBookingControlsPanel(), BorderLayout.CENTER);

        return leftPanel;
    }

    private void updateCalendarGrid() {
        calendarGrid.removeAll();
        calendarGrid.setBackground(panelColor);

        // Add day labels
        String[] days = {"M", "T", "W", "T", "F", "S", "S"};
        for (String day : days) {
            JLabel label = new JLabel(day, JLabel.CENTER);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("SansSerif", Font.PLAIN, 12));
            label.setOpaque(true);
            label.setBackground(panelColor);
            calendarGrid.add(label);
        }

        YearMonth yearMonth = YearMonth.from(selectedDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        // Add empty labels for days before the first of the month
        for (int i = 1; i < dayOfWeek; i++) {
            JLabel empty = new JLabel("");
            empty.setOpaque(true);
            empty.setBackground(panelColor);
            calendarGrid.add(empty);
        }

        // Add day buttons
        for (int i = 1; i <= yearMonth.lengthOfMonth(); i++) {
            LocalDate date = yearMonth.atDay(i);
            calendarGrid.add(createDayButton(date, i));
        }

        // Add empty labels for remaining grid cells
        int totalCells = 42; // 6 rows × 7 columns
        int remainingCells = totalCells - (dayOfWeek - 1 + yearMonth.lengthOfMonth());
        for (int i = 0; i < remainingCells; i++) {
            JLabel empty = new JLabel("");
            empty.setOpaque(true);
            empty.setBackground(panelColor);
            calendarGrid.add(empty);
        }

        monthYearLabel.setText(yearMonth.getMonth().toString() + " " + yearMonth.getYear());
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private void updateTimeSlotVisibility() {
        if (timeSlotTable != null) {
            Container parent = timeSlotTable.getParent();
            while (parent != null && !(parent instanceof JPanel)) {
                parent = parent.getParent();
            }
            if (parent != null) {
                parent.setVisible(selectedSpace != null);
            }
            if (selectedSpace != null) {
                // Check if it's Sunday for performance spaces
                if (isPerformanceSpace(selectedSpace) && selectedDate.getDayOfWeek().getValue() == 7) {
                    JOptionPane.showMessageDialog(this,
                        "Performance spaces are not available on Sundays.",
                        "Space Unavailable",
                        JOptionPane.WARNING_MESSAGE);
                    spaceSelector.setSelectedIndex(0);
                    selectedSpace = null;
                    return;
                }

                loadExistingBookings();
                updateTimeSlotGrid();
                
                boolean isRoomType = Arrays.asList(
                    "The Green Room", "Bronte Boardroom", "Dickens Den",
                    "Poe Parlor", "Globe Room", "Chekhov Chamber"
                ).contains(selectedSpace);
                
                boolean isRehearsalSpace = "Rehearsal Space".equals(selectedSpace);
                boolean isPerformanceSpace = isPerformanceSpace(selectedSpace);
                boolean isEntireVenue = "Entire Venue".equals(selectedSpace);
                
                allDayCheckBox.setVisible(isRoomType || isRehearsalSpace || isPerformanceSpace || isEntireVenue);
                eveningCheckBox.setVisible(isPerformanceSpace || isEntireVenue);
                
                allDayCheckBox.setSelected(false);
                eveningCheckBox.setSelected(false);
                
                // Clear all selections when changing spaces
                for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                    String status = (String) timeSlotModel.getValueAt(row, 1);
                    if (status.equals("Selected")) {
                        timeSlotModel.setValueAt("Available", row, 1);
                    }
                }
            }
        }
    }

    private boolean isPerformanceSpace(String space) {
        return "Main Hall".equals(space) || "Small Hall".equals(space);
    }

    private void handleDateSelection(LocalDate date) {
        selectedDate = date;
        dateLabel.setText(selectedDate.format(dateFormatter));
        loadExistingBookings();  // Reload bookings when date changes
        updateTimeSlotGrid();
        updateCalendarGrid();  // Add this line to refresh the calendar highlighting
    }

    private void handleAllDaySelection() {
        if (allDayCheckBox.isSelected()) {
            if (!isAllDayBookingAvailable()) {
                allDayCheckBox.setSelected(false);
                JOptionPane.showMessageDialog(this,
                        "All-day booking is not available. Some time slots are already booked.",
                        "Booking Not Available",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Select all available slots
            for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                String currentStatus = (String) timeSlotModel.getValueAt(row, 1);
                if (currentStatus.equals("Available")) {
                    timeSlotModel.setValueAt("Selected", row, 1);
                }
            }
        } else {
            // Revert all selected slots back to available
            for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                String currentStatus = (String) timeSlotModel.getValueAt(row, 1);
                if (currentStatus.equals("Selected")) {
                    timeSlotModel.setValueAt("Available", row, 1);
                }
            }
        }
        
        // Update cost display for Rehearsal Space all-day booking
        if (allDayCheckBox.isSelected() && "Rehearsal Space".equals(selectedSpace)) {
            int dayOfWeek = selectedDate.getDayOfWeek().getValue();
            boolean isWeekend = dayOfWeek == 6 || dayOfWeek == 7;
            double cost = isWeekend ? 500.0 : 450.0;
            totalLabel.setText(String.format("Total Cost: £%.2f", cost));
        }
    }

    private void handleEveningSelection() {
        if (eveningCheckBox.isSelected()) {
            allDayCheckBox.setSelected(false);
            
            // Check if evening slots are available
            boolean eveningSlotsAvailable = true;
            for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                String timeStr = (String) timeSlotModel.getValueAt(row, 0);
                LocalTime time = LocalTime.parse(timeStr);
                if (time.getHour() >= 17) {
                    String status = (String) timeSlotModel.getValueAt(row, 1);
                    if (!status.equals("Available")) {
                        eveningSlotsAvailable = false;
                        break;
                    }
                }
            }
            
            if (!eveningSlotsAvailable) {
                eveningCheckBox.setSelected(false);
                JOptionPane.showMessageDialog(this,
                    "Evening booking is not available. Some evening slots are already booked.",
                    "Booking Not Available",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Select all evening slots
            for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                String timeStr = (String) timeSlotModel.getValueAt(row, 0);
                LocalTime time = LocalTime.parse(timeStr);
                String currentStatus = (String) timeSlotModel.getValueAt(row, 1);
                
                if (time.getHour() >= 17 && currentStatus.equals("Available")) {
                    timeSlotModel.setValueAt("Selected", row, 1);
                } else if (time.getHour() < 17 && currentStatus.equals("Selected")) {
                    timeSlotModel.setValueAt("Available", row, 1);
                }
            }
        } else {
            // Revert all evening slots back to available
            for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                String timeStr = (String) timeSlotModel.getValueAt(row, 0);
                LocalTime time = LocalTime.parse(timeStr);
                String currentStatus = (String) timeSlotModel.getValueAt(row, 1);
                if (time.getHour() >= 17 && currentStatus.equals("Selected")) {
                    timeSlotModel.setValueAt("Available", row, 1);
                }
            }
        }
    }

    private void updateTimeSlotGrid() {
        if (timeSlotTable == null || timeSlotModel == null || selectedSpace == null) return;

        Map<LocalTime, Boolean> timeSlots = getOrCreateTimeSlots(selectedSpace, selectedDate);
        for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
            String timeStr = (String) timeSlotModel.getValueAt(row, 0);
            LocalTime time = LocalTime.parse(timeStr);

            boolean isBooked = isTimeSlotBooked(time);
            timeSlotModel.setValueAt(
                    isBooked ? "Your Booking" :
                            timeSlots.get(time) ? "Available" : "Unavailable",
                    row, 1
            );
        }

        timeSlotTable.repaint();
    }

    private boolean isTimeSlotBooked(LocalTime time) {
        for (BookingEvent event : bookingEvents) {
            if (event.space.equals(selectedSpace) &&
                    event.date.equals(selectedDate) &&
                    !time.isBefore(event.startTime) &&
                    time.isBefore(event.endTime)) {
                return true;
            }
        }
        return false;
    }

    private JPanel createMiddlePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(panelColor);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Add title
        JLabel titleLabel = new JLabel("Available Time Slots");
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Create content panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBackground(panelColor);
        
        // Create checkbox panel
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        checkboxPanel.setBackground(panelColor);
        
        // All day checkbox
        allDayCheckBox = new JCheckBox("All Day");
        allDayCheckBox.setFont(new Font("TimesRoman", Font.PLAIN, 14));
        allDayCheckBox.setForeground(Color.WHITE);
        allDayCheckBox.setBackground(panelColor);
        allDayCheckBox.setVisible(false);
        allDayCheckBox.addActionListener(e -> handleAllDaySelection());
        
        // Evening checkbox
        eveningCheckBox = new JCheckBox("Evening");
        eveningCheckBox.setFont(new Font("TimesRoman", Font.PLAIN, 14));
        eveningCheckBox.setForeground(Color.WHITE);
        eveningCheckBox.setBackground(panelColor);
        eveningCheckBox.setVisible(false);
        eveningCheckBox.addActionListener(e -> handleEveningSelection());
        
        checkboxPanel.add(allDayCheckBox);
        checkboxPanel.add(eveningCheckBox);
        
        // Add time slot table
        JScrollPane tableScrollPane = createTimeSlotTable();
        
        // Add components to content panel
        contentPanel.add(checkboxPanel, BorderLayout.NORTH);
        contentPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Legend panel
        JPanel legendPanel = createLegendPanel();
        contentPanel.add(legendPanel, BorderLayout.SOUTH);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JScrollPane createTimeSlotTable() {
        timeSlotTable = new JTable(timeSlotModel);
        TimeSlotCellRenderer renderer = new TimeSlotCellRenderer();
        setupTimeSlotTable(renderer);

        JScrollPane scrollPane = new JScrollPane(timeSlotTable);
        return scrollPane;
    }

    private void setupTimeSlotTable(TimeSlotCellRenderer renderer) {
        timeSlotTable.setDefaultRenderer(Object.class, renderer);
        timeSlotTable.setRowHeight(30);
        timeSlotTable.getTableHeader().setReorderingAllowed(false);
        timeSlotTable.setCellSelectionEnabled(true);
        timeSlotTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set column widths
        timeSlotTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        timeSlotTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        timeSlotTable.getColumnModel().getColumn(0).setMaxWidth(50);
        timeSlotTable.getColumnModel().getColumn(0).setMinWidth(50);

        setupTimeSlotTableListeners(renderer);
    }

    private void setupTimeSlotTableListeners(TimeSlotCellRenderer renderer) {
        timeSlotTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = timeSlotTable.rowAtPoint(e.getPoint());
                renderer.setHoveredRow(row);
                timeSlotTable.repaint();
            }
        });

        timeSlotTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                renderer.setHoveredRow(-1);
                timeSlotTable.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleTimeSlotClick();
            }
        });
    }

    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 2));
        legendPanel.setBackground(panelColor);
        legendPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        addLegendItem(legendPanel, availableColor, "Available");
        addLegendItem(legendPanel, selectedColor, "Selected");
        addLegendItem(legendPanel, unavailableColor, "Unavailable");
        
        return legendPanel;
    }

    private void addLegendItem(JPanel panel, Color color, String text) {
        JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        itemPanel.setBackground(panelColor);
        
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(15, 15));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        itemPanel.add(colorBox);
        itemPanel.add(label);
        panel.add(itemPanel);
    }

    class TimeSlotCellRenderer extends DefaultTableCellRenderer {
        private int hoveredRow = -1;

        public void setHoveredRow(int row) {
            hoveredRow = row;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (column == 1) { // Status column
                String status = value != null ? value.toString() : "";
                setHorizontalAlignment(JLabel.CENTER);
                
                if (selectedSpace != null && status.equals("Available")) {
                    setBackground(spaceColors.getOrDefault(selectedSpace, availableColor));
                } else {
                    applyStatusStyle(status);
                }

                // Add hover effect
                if (row == hoveredRow && (status.equals("Available") || status.equals("Selected"))) {
                    setBorder(createHoverBorder());
                } else {
                    setBorder(createNormalBorder());
                }
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
                setHorizontalAlignment(JLabel.LEFT);
                setBorder(createNormalBorder());
            }

            return c;
        }

        private void applyStatusStyle(String status) {
            switch (status) {
                case "Selected":
                case "Your Booking":
                    setBackground(selectedColor);
                    setForeground(Color.BLACK);
                    break;
                case "Unavailable":
                    setBackground(unavailableColor);
                    setForeground(Color.WHITE);
                    break;
                default:
                    setBackground(timeSlotTable.getBackground());
                    setForeground(timeSlotTable.getForeground());
            }
        }
    }

    private void handleTimeSlotClick() {
        if (selectedSpace == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a space first",
                    "No Space Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = timeSlotTable.getSelectedRow();
        if (row >= 0) {
            String timeStr = (String) timeSlotTable.getValueAt(row, 0);
            LocalTime time = LocalTime.parse(timeStr);
            String currentStatus = (String) timeSlotTable.getValueAt(row, 1);

            if (currentStatus.equals("Selected")) {
                timeSlotModel.setValueAt("Available", row, 1);
            } else if (currentStatus.equals("Available")) {
                timeSlotModel.setValueAt("Selected", row, 1);
            }
        }
    }

    private void updateTotalCost() {
        double total = bookingEvents.stream().mapToDouble(event -> event.cost).sum();
        totalLabel.setText(String.format("Total Cost: £%.2f", total));
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setBackground(panelColor);

        // Total cost label
        totalLabel = new JLabel("Total Cost: £0.00");
        totalLabel.setForeground(Color.WHITE);
        totalLabel.setFont(new Font("TimesRoman", Font.PLAIN, 14));
        panel.add(totalLabel, BorderLayout.WEST);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBackground(panelColor);

        JButton saveBtn = createStyledButton("Save Booking");
        JButton cancelBtn = createStyledButton("Cancel");

        // Remove saveBooking action listener, keeping button for now
        cancelBtn.addActionListener(e -> {
            dispose();
            new UserHome().setVisible(true);
        });

        buttonsPanel.add(saveBtn);
        buttonsPanel.add(cancelBtn);
        panel.add(buttonsPanel, BorderLayout.EAST);

        return panel;
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

    private JPanel createTopBar(JFrame parentFrame) {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(darkColour);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton homeBtn = createStyledButton("← Home");
        homeBtn.addActionListener(e -> {
            parentFrame.dispose();
            new UserHome().setVisible(true);
        });

        JButton settingsBtn = createStyledButton("⚙ Settings");
        settingsBtn.addActionListener(e -> new SettingScreen.SettingsDialog(parentFrame).setVisible(true));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        leftPanel.add(homeBtn);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(settingsBtn);

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);

        return topBar;
    }

    private void confirmBooking() {
        String bookingName = bookingNameField.getText().trim();
        if (bookingName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a name for this booking event",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<LocalTime> selectedTimes = collectSelectedTimes();
        if (selectedTimes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one time slot",
                    "No Time Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate for Entire Venue
        if ("Entire Venue".equals(selectedSpace)) {
            boolean hasEveningSlots = selectedTimes.stream()
                .anyMatch(time -> time.getHour() >= 17);
            boolean hasDaySlots = selectedTimes.stream()
                .anyMatch(time -> time.getHour() < 17);
            
            if (!eveningCheckBox.isSelected() && !allDayCheckBox.isSelected()) {
                JOptionPane.showMessageDialog(this,
                    "Entire Venue bookings must be either evening or all-day bookings",
                    "Invalid Booking Type",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Validate mixed day and evening bookings
            if (hasEveningSlots && hasDaySlots && !allDayCheckBox.isSelected()) {
                JOptionPane.showMessageDialog(this,
                    "Cannot mix day and evening slots.\nPlease use all-day booking for full day reservations.",
                    "Invalid Booking Time",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Validate minimum booking duration and time restrictions
        if (isPerformanceSpace(selectedSpace)) {
            int selectedHours = (selectedTimes.size() * 30) / 60;
            boolean hasEveningSlots = selectedTimes.stream()
                .anyMatch(time -> time.getHour() >= 17);
            boolean hasDaySlots = selectedTimes.stream()
                .anyMatch(time -> time.getHour() < 17);
            
            if (!eveningCheckBox.isSelected() && !allDayCheckBox.isSelected()) {
                // Regular booking validation
                if (selectedHours < 3) {
                    JOptionPane.showMessageDialog(this,
                        "Performance spaces require a minimum booking of 3 hours",
                        "Invalid Booking Duration",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Check if booking is within allowed hours (10:00-17:00)
                if (hasEveningSlots) {
                    JOptionPane.showMessageDialog(this,
                        "Regular bookings must be between 10:00 and 17:00.\nPlease use evening booking for slots after 17:00",
                        "Invalid Booking Time",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

                // Check if it's Saturday without evening/all-day booking
                if (selectedDate.getDayOfWeek().getValue() == 6) {
                    JOptionPane.showMessageDialog(this,
                        "Regular bookings are not available on Saturdays.\nPlease use evening or all-day booking.",
                        "Invalid Booking Day",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            
            // Validate mixed day and evening bookings
            if (hasEveningSlots && hasDaySlots && !allDayCheckBox.isSelected()) {
                JOptionPane.showMessageDialog(this,
                    "Cannot mix day and evening slots.\nPlease use all-day booking for full day reservations.",
                    "Invalid Booking Time",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Check minimum booking duration and weekend restrictions for Rehearsal Space
        if ("Rehearsal Space".equals(selectedSpace)) {
            int selectedHours = (selectedTimes.size() * 30) / 60;
            int dayOfWeek = selectedDate.getDayOfWeek().getValue();
            boolean isWeekend = dayOfWeek == 6 || dayOfWeek == 7;

            if (!allDayCheckBox.isSelected()) {
                // Check minimum hours
                if (selectedHours < 3) {
                    JOptionPane.showMessageDialog(this,
                        "Rehearsal Space requires a minimum booking of 3 hours",
                        "Invalid Booking Duration",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Check weekend booking restriction
                if (isWeekend) {
                    JOptionPane.showMessageDialog(this,
                        "Regular bookings are not available on weekends.\nPlease use all-day booking for weekend reservations.",
                        "Invalid Booking Day",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }

        createBookingEvents(selectedTimes, bookingName);
        updateUIAfterBooking();
    }

    private List<LocalTime> collectSelectedTimes() {
        List<LocalTime> selectedTimes = new ArrayList<>();
        for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
            if ("Selected".equals(timeSlotModel.getValueAt(row, 1))) {
                selectedTimes.add(LocalTime.parse((String)timeSlotModel.getValueAt(row, 0)));
            }
        }
        return selectedTimes;
    }

    private void createBookingEvents(List<LocalTime> selectedTimes, String bookingName) {
        List<LocalTime> startTimes = new ArrayList<>();
        List<LocalTime> endTimes = new ArrayList<>();
        LocalTime currentStart = null;
        LocalTime previousTime = null;

        for (LocalTime time : selectedTimes) {
            if (currentStart == null) {
                currentStart = time;
            } else if (previousTime != null && !time.equals(previousTime.plusMinutes(30))) {
                endTimes.add(previousTime.plusMinutes(30));
                startTimes.add(currentStart);
                currentStart = time;
            }
            previousTime = time;
        }
        if (currentStart != null && previousTime != null) {
            startTimes.add(currentStart);
            endTimes.add(previousTime.plusMinutes(30));
        }

        for (int i = 0; i < startTimes.size(); i++) {
            createSingleBookingEvent(startTimes.get(i), endTimes.get(i), bookingName);
        }
    }

    private void createSingleBookingEvent(LocalTime startTime, LocalTime endTime, String bookingName) {
        BookingEvent event = new BookingEvent(
                selectedDate, startTime, endTime,
                selectedSpace, bookingName,
                allDayCheckBox.isSelected() && allDayCheckBox.isVisible(),
                eveningCheckBox.isSelected() && eveningCheckBox.isVisible()
        );
        bookingEvents.add(event);

        Map<LocalTime, Boolean> timeSlots = getOrCreateTimeSlots(selectedSpace, selectedDate);
        LocalTime current = startTime;
        while (!current.equals(endTime)) {
            timeSlots.put(current, false);
            current = current.plusMinutes(30);
        }
        
            updateTotalCost();
        }

    private void updateUIAfterBooking() {
        for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
            if ("Selected".equals(timeSlotModel.getValueAt(row, 1))) {
                timeSlotModel.setValueAt("Your Booking", row, 1);
            }
        }
        bookingNameField.setText("");
        updateBookingList();
        updateTotalCost();
    }

    private void updateBookingList() {
        bookingListModel.clear();
        for (BookingEvent event : bookingEvents) {
            bookingListModel.addElement(String.format("%s - %s \n%s, %s to %s",
                    event.eventType,
                    event.space,
                    event.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    event.startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    event.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            ));
        }
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(panelColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Add title
        JLabel titleLabel = new JLabel("Your Booking Events");
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Booking list
        bookingListModel = new DefaultListModel<>();
        bookingList = new JList<>(bookingListModel);
        setupBookingList();
        
        JScrollPane scrollPane = new JScrollPane(bookingList);
        scrollPane.setBorder(null);
        scrollPane.setBackground(panelColor);
        scrollPane.getViewport().setBackground(panelColor);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Actions panel
        JPanel actionsPanel = createActionsPanel();
        panel.add(actionsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void setupBookingList() {
        bookingList.setBackground(panelColor);
        bookingList.setForeground(Color.WHITE);
        bookingList.setFont(new Font("TimesRoman", Font.PLAIN, 14));
        bookingList.setBorder(null);  // Remove list border
        
        // Track hover state for delete button only
        int[] hoveredButton = {-1, 0};  // Only tracking delete button now
        
        bookingList.setCellRenderer(new BookingListCellRenderer(hoveredButton));
        setupBookingListListeners(hoveredButton);
        bookingList.setFixedCellHeight(-1);
    }

    private class BookingListCellRenderer extends DefaultListCellRenderer {
        private final int[] hoveredButton;

        public BookingListCellRenderer(int[] hoveredButton) {
            this.hoveredButton = hoveredButton;
        }

            @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JPanel cellPanel = new JPanel(new BorderLayout(5, 0));
            cellPanel.setBackground(isSelected ? list.getSelectionBackground() : panelColor);
            
            // Text area setup
            JTextArea textArea = createBookingTextArea(value.toString(), list, cellPanel, isSelected);
            
            // Delete button setup
            JPanel buttonsPanel = createBookingButtonsPanel(index, isSelected, list);
            
            cellPanel.add(textArea, BorderLayout.CENTER);
            cellPanel.add(buttonsPanel, BorderLayout.EAST);
            
            return cellPanel;
        }

        private JTextArea createBookingTextArea(String text, JList<?> list, JPanel parent, boolean isSelected) {
            JTextArea textArea = new JTextArea(text);
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            textArea.setBackground(parent.getBackground());
            textArea.setForeground(isSelected ? list.getSelectionForeground() : Color.WHITE);
            textArea.setFont(list.getFont());
            textArea.setBorder(createNormalBorder());
            
            // Set preferred width
            int width = list.getWidth();
            if (width > 0) {
                textArea.setSize(width - 40, Short.MAX_VALUE);  // Adjusted width since we only have delete button now
                textArea.setPreferredSize(new Dimension(width - 40, textArea.getPreferredSize().height));
            }
            
            return textArea;
        }

        private JPanel createBookingButtonsPanel(int index, boolean isSelected, JList<?> list) {
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            buttonsPanel.setBackground(isSelected ? list.getSelectionBackground() : panelColor);
            
            // Delete button
            JLabel deleteButton = new JLabel("✕");
            deleteButton.setFont(new Font("TimesRoman", Font.PLAIN, 16));
            deleteButton.setForeground(isSelected ? list.getSelectionForeground() : Color.WHITE);
            
            // Add hover effect border
            if (hoveredButton[0] == index && hoveredButton[1] == 0) {  // Changed to 0 since it's the only button now
                deleteButton.setBorder(createHoverBorder());
                } else {
                deleteButton.setBorder(createNormalBorder());
            }
            
            buttonsPanel.add(deleteButton);
            
            return buttonsPanel;
        }
    }

    private void setupBookingListListeners(int[] hoveredButton) {
        // Mouse motion listener for hover effects
        bookingList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateButtonHoverState(e.getPoint(), hoveredButton);
            }
        });
        
        // Mouse listener for click and exit events
        bookingList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                resetHoverState(hoveredButton);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                handleBookingListClick(e.getPoint());
            }
        });
    }

    private void updateButtonHoverState(Point point, int[] hoveredButton) {
        int index = bookingList.locationToIndex(point);
        if (index >= 0) {
            Rectangle bounds = bookingList.getCellBounds(index, index);
            if (bounds != null) {
                int x = point.x - bounds.x;
                int rightEdge = bounds.width;
                
                if (x >= rightEdge - 40) {  // Adjusted since we only have delete button
                    hoveredButton[0] = index;
                    hoveredButton[1] = 0;  // Only delete button now
                    bookingList.repaint();
                } else if (hoveredButton[0] != -1) {
                    resetHoverState(hoveredButton);
                }
            }
        }
    }

    private void resetHoverState(int[] hoveredButton) {
        hoveredButton[0] = -1;
        hoveredButton[1] = -1;
        bookingList.repaint();
    }

    private void handleBookingListClick(Point point) {
        int index = bookingList.locationToIndex(point);
        if (index >= 0) {
            Rectangle bounds = bookingList.getCellBounds(index, index);
            if (bounds != null) {
                int x = point.x - bounds.x;
                int rightEdge = bounds.width;
                
                if (x >= rightEdge - 40) {  // Adjusted since we only have delete button
                    deleteBooking(index);
                }
            }
        }
    }

    private void deleteBooking(int index) {
        if (index >= 0 && index < bookingEvents.size()) {
            BookingEvent event = bookingEvents.get(index);

            Map<LocalTime, Boolean> timeSlots = getOrCreateTimeSlots(event.space, event.date);
            LocalTime current = event.startTime;
            while (!current.equals(event.endTime)) {
                timeSlots.put(current, true);
                current = current.plusMinutes(30);
            }

            bookingEvents.remove(index);
            updateBookingList();
            updateTimeSlotGrid();
            updateTotalCost(); // Update the total cost after deleting a booking
        }
    }

    private JPanel createMonthViewContent() {
        JPanel monthContent = new JPanel(new BorderLayout());
        monthContent.setBackground(panelColor);

        // Create month header
        JPanel monthHeader = new JPanel(new BorderLayout());
        monthHeader.setBackground(panelColor);
        monthHeader.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton prev = createStyledButton("<");
        JButton next = createStyledButton(">");
        monthYearLabel = new JLabel("", JLabel.CENTER);
        monthYearLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        monthYearLabel.setForeground(Color.WHITE);

        prev.addActionListener(e -> {
            selectedDate = selectedDate.minusMonths(1);
            updateCalendarGrid();
        });

        next.addActionListener(e -> {
            selectedDate = selectedDate.plusMonths(1);
            updateCalendarGrid();
        });

        monthHeader.add(prev, BorderLayout.WEST);
        monthHeader.add(monthYearLabel, BorderLayout.CENTER);
        monthHeader.add(next, BorderLayout.EAST);

        // Create calendar grid
        calendarGrid = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarGrid.setBackground(panelColor);

        // Create date label
        dateLabel = new JLabel("", JLabel.CENTER);
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setFont(new Font("TimesRoman", Font.PLAIN, 14));
        dateLabel.setText(selectedDate.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")));
        dateLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        monthContent.add(monthHeader, BorderLayout.NORTH);
        monthContent.add(calendarGrid, BorderLayout.CENTER);
        monthContent.add(dateLabel, BorderLayout.SOUTH);

        // Initialize calendar grid
        updateCalendarGrid();

        return monthContent;
    }

    private JPanel createBookingControlsPanel() {
        JPanel bookingControlsPanel = new JPanel(new GridBagLayout());
        bookingControlsPanel.setBackground(panelColor);
        bookingControlsPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Create booking name panel
        JPanel bookingNamePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        bookingNamePanel.setBackground(panelColor);

        JLabel bookingNameLabel = new JLabel("Event Booking Name:");
        bookingNameLabel.setForeground(Color.WHITE);
        bookingNameLabel.setFont(new Font("TimesRoman", Font.PLAIN, 14));

        bookingNameField = new JTextField();
        bookingNameField.setFont(new Font("TimesRoman", Font.PLAIN, 14));

        bookingNamePanel.add(bookingNameLabel);
        bookingNamePanel.add(bookingNameField);

        // Create space selection panel
        JPanel spacePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        spacePanel.setBackground(panelColor);
        spacePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        JLabel spaceLabel = new JLabel("Select Space:");
        spaceLabel.setForeground(Color.WHITE);
        spaceLabel.setFont(new Font("TimesRoman", Font.PLAIN, 14));

        spaceSelector = new JComboBox<>(spaces);
        spaceSelector.setFont(new Font("TimesRoman", Font.PLAIN, 14));
        spaceSelector.addActionListener(e -> {
            if (spaceSelector.getSelectedIndex() == 0) {
                if (selectedSpace != null) {
                    spaceSelector.setSelectedItem(selectedSpace);
                }
                return;
            }
            selectedSpace = (String)spaceSelector.getSelectedItem();
            updateTimeSlotVisibility();
        });

        spacePanel.add(spaceLabel);
        spacePanel.add(spaceSelector);

        // Create confirm button
        JButton confirmButton = createStyledButton("Confirm Event Booking");
        confirmButton.setPreferredSize(new Dimension(250, 35));
        confirmButton.addActionListener(e -> confirmBooking());

        // Add components to booking controls panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        bookingControlsPanel.add(bookingNamePanel, gbc);

        gbc.gridy = 1;
        bookingControlsPanel.add(spacePanel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        bookingControlsPanel.add(confirmButton, gbc);

        return bookingControlsPanel;
    }

    private void loadExistingBookings() {
        String query = "SELECT BookingName, Client, Room, StartTime, EndTime, BookingDate FROM booking WHERE BookingDate = ?";
        
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26", "in2033t26_a", "jLxOPuQ69Mg");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(selectedDate));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String room = rs.getString("Room");
                LocalTime startTime = rs.getTime("StartTime").toLocalTime();
                LocalTime endTime = rs.getTime("EndTime").toLocalTime();
                
                // Mark these slots as unavailable in our availability map
                Map<LocalTime, Boolean> timeSlots = getOrCreateTimeSlots(room, selectedDate);
                LocalTime current = startTime;
                while (!current.equals(endTime)) {
                    timeSlots.put(current, false);  // false means unavailable
                    current = current.plusMinutes(30);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading existing bookings: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isAllDayBookingAvailable() {
        for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
            String status = (String) timeSlotModel.getValueAt(row, 1);
            if (status.equals("Unavailable") || status.equals("Your Booking")) {
                return false;
            }
        }
        return true;
    }

    private JButton createDayButton(LocalDate date, int day) {
        JButton dayButton = new JButton(String.valueOf(day));
        dayButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
        dayButton.setForeground(Color.WHITE);
        dayButton.setBackground(date.equals(selectedDate) ? new Color(0, 120, 215) : darkColour);
        dayButton.setOpaque(true);
        dayButton.setBorderPainted(false);
        dayButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        dayButton.setFocusPainted(false);
        dayButton.setPreferredSize(new Dimension(30, 30));

        dayButton.addMouseListener(new MouseAdapter() {
            private Border originalBorder;

            @Override
            public void mouseEntered(MouseEvent e) {
                originalBorder = dayButton.getBorder();
                dayButton.setBorderPainted(true);
                dayButton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 2),
                        BorderFactory.createEmptyBorder(2, 2, 2, 2)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                dayButton.setBorder(originalBorder);
                dayButton.setBorderPainted(false);
            }
        });

        dayButton.addActionListener(e -> handleDateSelection(date));

        return dayButton;
    }
}
