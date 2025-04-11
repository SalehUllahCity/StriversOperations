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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * A GUI application for managing room bookings in a venue.
 * Provides functionality for selecting dates, spaces, and time slots for bookings.
 */
public class Booking extends JFrame {
    /** Color scheme for the application UI */
    private final Color darkColour = new Color(18, 32, 35);
    private final Color panelColor = new Color(30, 50, 55);
    private final Color availableColor = new Color(76, 175, 80);
    private final Color unavailableColor = new Color(244, 67, 54);
    private final Color selectedColor = new Color(255, 152, 0);
    private final Color buttonHoverColor = Color.LIGHT_GRAY;

    /** Formatters for date and time display */
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    /** Data structures for managing availability and time slots */
    private Map<String, String> availabilityMap;
    private List<String> timeSlots;
    private Map<String, Map<LocalDate, Map<LocalTime, Boolean>>> spaceAvailability;

    /** Available spaces for booking */
    private final String[] spaces = {
            "Select a space...",
            "The Green Room", "Brontë Boardroom", "Dickens Den", "Poe Parlor",
            "Globe Room", "Chekhov Chamber", "Main Hall", "Small Hall",
            "Rehearsal Space", "Entire Venue"
    };

    /** Color mapping for different spaces */
    private final Map<String, Color> spaceColors = new HashMap<>(); {
        spaceColors.put("The Green Room", new Color(76, 175, 80));    // Green
        spaceColors.put("Brontë Boardroom", new Color(33, 150, 243)); // Blue
        spaceColors.put("Dickens Den", new Color(156, 39, 176));      // Purple
        spaceColors.put("Poe Parlor", new Color(255, 152, 0));        // Orange
        spaceColors.put("Globe Room", new Color(0, 188, 212));        // Cyan
        spaceColors.put("Chekhov Chamber", new Color(233, 30, 99));   // Pink
        spaceColors.put("Main Hall", new Color(244, 67, 54));         // Red
        spaceColors.put("Small Hall", new Color(255, 235, 59));       // Yellow
        spaceColors.put("Rehearsal Space", new Color(63, 81, 181));   // Indigo
        spaceColors.put("Entire Venue", new Color(96, 125, 139));     // Blue Grey
    }

    /** UI components for booking information input */
    private JTextField bookingNameField;
    private JTextField clientNameField;
    private JTable timeSlotTable;
    private DefaultTableModel timeSlotModel;
    private JLabel totalLabel;
    private JLabel dateLabel;
    private JPanel calendarGrid;
    private JLabel monthYearLabel;
    private JComboBox<String> spaceSelector;
    private JList<String> bookingList;
    private DefaultListModel<String> bookingListModel;

    /** Booking type selection checkboxes */
    private JCheckBox allDayCheckBox;
    private JCheckBox eveningCheckBox;
    private JCheckBox morningCheckBox;
    private JCheckBox afternoonCheckBox;
    private JCheckBox weekCheckBox;
    private JCheckBox allDayShortCheckBox;
    private JCheckBox allDayLongCheckBox;
    private JCheckBox weekShortCheckBox;
    private JCheckBox weekLongCheckBox;

    /** Current booking state */
    private LocalDate selectedDate = LocalDate.now();
    private String selectedSpace = null;
    private List<BookingEvent> bookingEvents = new ArrayList<>();

    /**
     * Represents a single booking event with its associated details and cost calculation.
     */
    private static class BookingEvent {
        /** Date of the booking */
        LocalDate date;
        /** Start time of the booking */
        LocalTime startTime;
        /** End time of the booking */
        LocalTime endTime;
        /** Space being booked */
        String space;
        /** Type of event being booked */
        String eventType;
        /** Calculated cost of the booking */
        double cost;
        /** Whether this is an all-day booking */
        boolean isAllDay;
        /** Whether this is an evening booking */
        boolean isEvening;
        /** Whether this is a short all-day booking (10:00-17:00) */
        boolean isAllDayShort;
        /** Whether this is a long all-day booking (10:00-23:00) */
        boolean isAllDayLong;
        /** Whether this is a short week booking (10:00-18:00) */
        boolean isWeekShort;
        /** Whether this is a long week booking (10:00-23:00) */
        boolean isWeekLong;
        /** Whether this is a week booking */
        boolean isWeek;
        /** Whether this is a morning booking */
        boolean isMorning;
        /** Whether this is an afternoon booking */
        boolean isAfternoon;
        /** Whether this is a display-only booking (not for actual booking) */
        boolean isDisplayOnly;

        /**
         * Constructs a new booking event with the specified parameters.
         * @param date The date of the booking
         * @param startTime The start time of the booking
         * @param endTime The end time of the booking
         * @param space The space being booked
         * @param eventType The type of event being booked
         * @param isAllDay Whether this is an all-day booking
         * @param isEvening Whether this is an evening booking
         * @param isAllDayShort Whether this is a short all-day booking
         * @param isAllDayLong Whether this is a long all-day booking
         * @param isWeekShort Whether this is a short week booking
         * @param isWeekLong Whether this is a long week booking
         * @param isMorning Whether this is a morning booking
         * @param isAfternoon Whether this is an afternoon booking
         * @param isDisplayOnly Whether this is a display-only booking
         * @param isWeek Whether this is a week booking
         */
        public BookingEvent(LocalDate date, LocalTime startTime, LocalTime endTime,
                            String space, String eventType, boolean isAllDay, boolean isEvening,
                            boolean isAllDayShort, boolean isAllDayLong,
                            boolean isWeekShort, boolean isWeekLong,
                            boolean isMorning, boolean isAfternoon,
                            boolean isDisplayOnly, boolean isWeek) {
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.space = space;
            this.eventType = eventType;
            this.isAllDay = isAllDay;
            this.isEvening = isEvening;
            this.isAllDayShort = isAllDayShort;
            this.isAllDayLong = isAllDayLong;
            this.isWeekShort = isWeekShort;
            this.isWeekLong = isWeekLong;
            this.isMorning = isMorning;
            this.isAfternoon = isAfternoon;
            this.isDisplayOnly = isDisplayOnly;
            this.isWeek = isWeek;
            this.cost = calculateCost();
        }

        /**
         * Calculates the cost for this booking event based on various factors including:
         * - Room type and size
         * - Booking duration
         * - Day of week (weekend vs weekday)
         * - Time of day (morning, afternoon, evening)
         * - Special booking types (all-day, weekly, etc.)
         * 
         * @return The calculated cost for the booking event
         */
        private double calculateCost() {
            if (isDisplayOnly) {
                return 0.0;
            }

            long totalMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
            int fullHours = (int) (totalMinutes / 60);
            int remainingMinutes = (int) (totalMinutes % 60);
            boolean hasHalfHour = remainingMinutes == 30;

            int dayOfWeek = date.getDayOfWeek().getValue();
            boolean isWeekend = dayOfWeek == 6 || dayOfWeek == 7;
            boolean isFridayOrSaturday = dayOfWeek == 5 || dayOfWeek == 6;

            switch (space) {
                case "The Green Room":
                    if (isWeek) {
                        return 600.0;
                    } else if (isAllDay) {
                        return 130.0;
                    } else if (isMorning) {
                        return 75.0;
                    } else if (isAfternoon) {
                        return 75.0;
                    } else {
                        double hourlyRate = 25.0;
                        return fullHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    }
                case "Brontë Boardroom":
                    if (isWeek) {
                        return 900.0;
                    } else if (isAllDay) {
                        return 200.0;
                    } else if (isMorning) {
                        return 120.0;
                    } else if (isAfternoon) {
                        return 120.0;
                    } else {
                        double hourlyRate = 40.0;
                        return fullHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    }
                case "Dickens Den":
                    if (isWeek) {
                        return 700.0;
                    } else if (isAllDay) {
                        return 150.0;
                    } else if (isMorning) {
                        return 90.0;
                    } else if (isAfternoon) {
                        return 90.0;
                    } else {
                        double hourlyRate = 30.0;
                        return fullHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    }
                case "Poe Parlor":
                    if (isWeek) {
                        return 800.0;
                    } else if (isAllDay) {
                        return 170.0;
                    } else if (isMorning) {
                        return 100.0;
                    } else if (isAfternoon) {
                        return 100.0;
                    } else {
                        double hourlyRate = 35.0;
                        return fullHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    }
                case "Globe Room":
                    if (isWeek) {
                        return 1100.0;
                    } else if (isAllDay) {
                        return 250.0;
                    } else if (isMorning) {
                        return 150.0;
                    } else if (isAfternoon) {
                        return 150.0;
                    } else {
                        double hourlyRate = 50.0;
                        return fullHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    }
                case "Chekhov Chamber":
                    if (isWeek) {
                        return 850.0;
                    } else if (isAllDay) {
                        return 180.0;
                    } else if (isMorning) {
                        return 110.0;
                    } else if (isAfternoon) {
                        return 110.0;
                    } else {
                        double hourlyRate = 38.0;
                        return fullHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    }
                case "Main Hall":
                    if (isWeek) {
                        return 2500.0;
                    } else if (isAllDay) {
                        return isFridayOrSaturday ? 4200.0 : 3800.0;
                    } else if (isEvening) {
                        return isFridayOrSaturday ? 2200.0 : 1850.0;
                    } else if (fullHours >= 3) {
                        double hourlyRate = 325.0;
                        return fullHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    }
                    return 0.0;
                case "Small Hall":
                    if (isWeek) {
                        return 1500.0;
                    } else if (isAllDay) {
                        return isFridayOrSaturday ? 2500.0 : 2200.0;
                    } else if (isEvening) {
                        return isFridayOrSaturday ? 1300.0 : 950.0;
                    } else if (fullHours >= 3) {
                        double hourlyRate = 225.0;
                        return fullHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    }
                    return 0.0;
                case "Rehearsal Space":
                    if (isWeekShort) {
                        return 1000.0;
                    } else if (isWeekLong) {
                        return 1500.0;
                    } else if (isAllDayShort) {
                        return isWeekend ? 340.0 : 240.0;
                    } else if (isAllDayLong) {
                        return isWeekend ? 500.0 : 450.0;
                    } else {
                        double hourlyRate = 60.0;
                        int effectiveHours = Math.max(3, fullHours);
                        return effectiveHours * hourlyRate + (hasHalfHour ? hourlyRate / 2 : 0);
                    }
                default:
                    return 0.0;
            }
        }
    }

    /**
     * Main method to launch the Booking application.
     * @param args Command line arguments (not used)
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
     * Constructs a new Booking frame and initializes the UI components.
     * Sets up the database connection and loads initial data.
     */
    public Booking() {
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

    /**
     * Sets up the main frame properties including title, size, and layout.
     */
    private void setupFrame() {
        setTitle("Lancaster's Music Hall Software: New Booking");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(darkColour);
        getContentPane().setLayout(new BorderLayout());
    }

    /**
     * Initializes the data structures for managing availability and time slots.
     * Sets up the initial state for the booking system.
     */
    private void initializeData() {
        availabilityMap = new HashMap<>();
        timeSlots = new ArrayList<>();
        initializeSpaceAvailability();

        for (int hour = 10; hour <= 24; hour++) {
            for (int min = 0; min < 60; min += 30) {
                if (hour == 24 && min > 0) continue;

                LocalTime time = LocalTime.of(hour == 24 ? 0 : hour, min);
                String timeSlot = time.format(timeFormatter);
                timeSlots.add(timeSlot);
                availabilityMap.put(timeSlot, "Available");
            }
        }
        loadExistingBookings();
    }

    /**
     * Initializes the space availability map for tracking booking status.
     */
    private void initializeSpaceAvailability() {
        spaceAvailability = new HashMap<>();
        for (String space : spaces) {
            if (!space.equals("Select a space...")) {
                Map<LocalDate, Map<LocalTime, Boolean>> dateMap = new HashMap<>();
                spaceAvailability.put(space, dateMap);
            }
        }
    }

    /**
     * Gets or creates the time slots map for a specific space and date.
     * @param space The space to get time slots for
     * @param date The date to get time slots for
     * @return Map of time slots with their availability status
     */
    private Map<LocalTime, Boolean> getOrCreateTimeSlots(String space, LocalDate date) {
        Map<LocalDate, Map<LocalTime, Boolean>> dateMap = spaceAvailability.get(space);
        if (!dateMap.containsKey(date)) {
            Map<LocalTime, Boolean> timeSlots = new HashMap<>();
            for (int hour = 10; hour <= 24; hour++) {
                for (int min = 0; min < 60; min += 30) {
                    if (hour == 24 && min > 0) continue;

                    LocalTime time = LocalTime.of(hour == 24 ? 0 : hour, min);
                    timeSlots.put(time, true);
                }
            }
            dateMap.put(date, timeSlots);
        }
        return dateMap.get(date);
    }

    /**
     * Creates the main UI components and layout.
     */
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

    /**
     * Creates a styled button with hover effects.
     * @param text The text to display on the button
     * @return A styled JButton instance
     */
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

    /**
     * Creates a border for hover effects.
     * @return A compound border for hover state
     */
    private Border createHoverBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)
        );
    }

    /**
     * Creates a normal border for non-hover state.
     * @return An empty border
     */
    private Border createNormalBorder() {
        return BorderFactory.createEmptyBorder(5, 5, 5, 5);
    }

    /**
     * Initializes the time slot table model with default values.
     */
    private void initializeTimeSlotModel() {
        String[] columnNames = {"Time", "Status"};
        timeSlotModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (int hour = 10; hour <= 24; hour++) {
            if (hour == 24) {
                timeSlotModel.addRow(new Object[]{"00:00", "Available"});
                continue;
            }
            timeSlotModel.addRow(new Object[]{String.format("%02d:00", hour), "Available"});
            timeSlotModel.addRow(new Object[]{String.format("%02d:30", hour), "Available"});
        }
    }

    /**
     * Creates the time slot table with appropriate styling and behavior.
     * @return A JScrollPane containing the time slot table
     */
    private JScrollPane createTimeSlotTable() {
        initializeTimeSlotModel();

        timeSlotTable = new JTable(timeSlotModel);
        TimeSlotCellRenderer renderer = new TimeSlotCellRenderer();
        setupTimeSlotTable(renderer);

        return new JScrollPane(timeSlotTable);
    }

    /**
     * Sets up the time slot table with appropriate renderers and listeners.
     * @param renderer The cell renderer to use for the table
     */
    private void setupTimeSlotTable(TimeSlotCellRenderer renderer) {
        timeSlotTable.setDefaultRenderer(Object.class, renderer);
        timeSlotTable.setRowHeight(25);
        timeSlotTable.getTableHeader().setReorderingAllowed(false);
        timeSlotTable.setCellSelectionEnabled(true);
        timeSlotTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        timeSlotTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        timeSlotTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        timeSlotTable.getColumnModel().getColumn(0).setMaxWidth(50);
        timeSlotTable.getColumnModel().getColumn(0).setMinWidth(50);

        setupTimeSlotTableListeners(renderer);
    }

    /**
     * Sets up mouse listeners for the time slot table.
     * @param renderer The cell renderer to use for hover effects
     */
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

    /**
     * Creates the left panel containing the calendar and booking controls.
     * @return A JPanel containing the calendar and controls
     */
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(panelColor);
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel monthViewPanel = new JPanel(new BorderLayout());
        monthViewPanel.setBackground(panelColor);

        JLabel titleLabel = new JLabel("Select Date");
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        monthViewPanel.add(titleLabel, BorderLayout.NORTH);

        monthViewPanel.add(createMonthViewContent(), BorderLayout.CENTER);
        leftPanel.add(monthViewPanel, BorderLayout.NORTH);
        leftPanel.add(createBookingControlsPanel(), BorderLayout.CENTER);

        return leftPanel;
    }

    /**
     * Updates the calendar grid to reflect the current month and selected date.
     */
    private void updateCalendarGrid() {
        calendarGrid.removeAll();
        calendarGrid.setBackground(panelColor);
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
        for (int i = 1; i < dayOfWeek; i++) {
            JLabel empty = new JLabel("");
            empty.setOpaque(true);
            empty.setBackground(panelColor);
            calendarGrid.add(empty);
        }
        for (int i = 1; i <= yearMonth.lengthOfMonth(); i++) {
            LocalDate date = yearMonth.atDay(i);
            calendarGrid.add(createDayButton(date, i));
        }
        int totalCells = 42;
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

    /**
     * Updates the visibility of time slots based on the selected space.
     */
    private void updateTimeSlotVisibility() {
        if (timeSlotTable != null) {
            Container parent = timeSlotTable.getParent();
            while (parent != null && !(parent instanceof JPanel)) {
                parent = parent.getParent();
            }
            if (parent != null) {
                parent.setVisible(selectedSpace != null && !selectedSpace.equals("Select a space..."));
            }
            if (selectedSpace != null && !selectedSpace.equals("Select a space...")) {
                if (isPerformanceSpace(selectedSpace) && selectedDate.getDayOfWeek().getValue() == 7) {
                    JOptionPane.showMessageDialog(this,
                        "Performance spaces are not available on Sundays.",
                        "Space Unavailable",
                        JOptionPane.WARNING_MESSAGE);
                    spaceSelector.setSelectedIndex(0);
                    selectedSpace = null;
                    parent.setVisible(false);
                    return;
                }

                loadExistingBookings();
                updateTimeSlotGrid();

                boolean isRoomType = Arrays.asList(
                    "The Green Room", "Brontë Boardroom", "Dickens Den",
                    "Poe Parlor", "Globe Room", "Chekhov Chamber"
                ).contains(selectedSpace);

                boolean isRehearsalSpace = "Rehearsal Space".equals(selectedSpace);
                boolean isPerformanceSpace = isPerformanceSpace(selectedSpace);
                boolean isEntireVenue = "Entire Venue".equals(selectedSpace);
                hideAllCheckboxes();

                if (isRoomType) {
                    morningCheckBox.setVisible(true);
                    afternoonCheckBox.setVisible(true);
                    allDayCheckBox.setVisible(true);
                    weekCheckBox.setVisible(true);
                } else if (isRehearsalSpace) {
                    allDayShortCheckBox.setVisible(true);
                    allDayLongCheckBox.setVisible(true);
                    weekShortCheckBox.setVisible(true);
                    weekLongCheckBox.setVisible(true);
                } else if (isPerformanceSpace) {
                    allDayCheckBox.setVisible(true);
                    eveningCheckBox.setVisible(true);
                } else if (isEntireVenue) {
                    allDayCheckBox.setVisible(true);
                    eveningCheckBox.setVisible(true);
                }
            } else {
                hideAllCheckboxes();
            }
        }
    }

    /**
     * Hides all booking type checkboxes.
     */
    private void hideAllCheckboxes() {
        morningCheckBox.setVisible(false);
        afternoonCheckBox.setVisible(false);
        allDayCheckBox.setVisible(false);
        allDayShortCheckBox.setVisible(false);
        allDayLongCheckBox.setVisible(false);
        eveningCheckBox.setVisible(false);
        weekCheckBox.setVisible(false);
        weekShortCheckBox.setVisible(false);
        weekLongCheckBox.setVisible(false);
    }

    /**
     * Checks if a space is a performance space.
     * @param space The space to check
     * @return true if the space is a performance space, false otherwise
     */
    private boolean isPerformanceSpace(String space) {
        return "Main Hall".equals(space) || "Small Hall".equals(space);
    }

    /**
     * Handles date selection and updates the UI accordingly.
     * @param date The selected date
     */
    private void handleDateSelection(LocalDate date) {
        selectedDate = date;
        dateLabel.setText(selectedDate.format(dateFormatter));
        if (selectedSpace != null && !selectedSpace.equals("Select a space...")) {
            loadExistingBookings();
            updateTimeSlotGrid();
        }
        updateCalendarGrid();
    }

    /**
     * Handles all-day booking selection and updates the time slots.
     */
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
            for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                String currentStatus = (String) timeSlotModel.getValueAt(row, 1);
                if (currentStatus.equals("Available")) {
                    timeSlotModel.setValueAt("Selected", row, 1);
                }
            }
        } else {
            for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                String currentStatus = (String) timeSlotModel.getValueAt(row, 1);
                if (currentStatus.equals("Selected")) {
                    timeSlotModel.setValueAt("Available", row, 1);
                }
            }
        }
        if (allDayCheckBox.isSelected() && "Rehearsal Space".equals(selectedSpace)) {
            int dayOfWeek = selectedDate.getDayOfWeek().getValue();
            boolean isWeekend = dayOfWeek == 6 || dayOfWeek == 7;
            double cost = isWeekend ? 500.0 : 450.0;
            totalLabel.setText(String.format("Total Cost: £%.2f + VAT", cost));
        }
    }

    /**
     * Handles evening booking selection and updates the time slots.
     */
    private void handleEveningSelection() {
        if (eveningCheckBox.isSelected()) {
            allDayCheckBox.setSelected(false);

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

    /**
     * Updates the time slot grid to reflect current availability.
     */
    private void updateTimeSlotGrid() {
        if (timeSlotTable == null || timeSlotModel == null || selectedSpace == null) return;

        Map<LocalTime, Boolean> timeSlots = getOrCreateTimeSlots(selectedSpace, selectedDate);
        for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
            timeSlotModel.setValueAt("Available", row, 1);
        }

        for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
            String timeStr = (String) timeSlotModel.getValueAt(row, 0);
            LocalTime time = LocalTime.parse(timeStr);
            if (!timeSlots.getOrDefault(time, true)) {
                timeSlotModel.setValueAt("Unavailable", row, 1);
            }
        }
        for (BookingEvent event : bookingEvents) {
            if (event.space.equals(selectedSpace) && event.date.equals(selectedDate)) {
                LocalTime current = event.startTime;
                while (!current.equals(event.endTime)) {
                    for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                        String timeStr = (String) timeSlotModel.getValueAt(row, 0);
                        if (timeStr.equals(current.format(timeFormatter))) {
                            timeSlotModel.setValueAt("Your Booking", row, 1);
                            break;
                        }
                    }
                    current = current.plusMinutes(30);
                }
            }
        }
        timeSlotTable.repaint();
    }

    /**
     * Creates the middle panel containing time slot selection.
     * @return A JPanel containing the time slot selection interface
     */
    private JPanel createMiddlePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(panelColor);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel("Available Time Slots");
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(panelColor);

        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        checkboxPanel.setBackground(panelColor);
        checkboxPanel.setPreferredSize(new Dimension(0, 80));
        checkboxPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        JPanel currentRow = new JPanel(new GridLayout(1, 2, 10, 0));
        currentRow.setBackground(panelColor);
        morningCheckBox = new JCheckBox("Morning (10:00-12:00)");
        styleCheckBox(morningCheckBox);
        morningCheckBox.addActionListener(e -> handleMorningSelection());

        afternoonCheckBox = new JCheckBox("Afternoon (12:00-17:00)");
        styleCheckBox(afternoonCheckBox);
        afternoonCheckBox.addActionListener(e -> handleAfternoonSelection());

        eveningCheckBox = new JCheckBox("Evening (17:00-00:00)");
        styleCheckBox(eveningCheckBox);
        eveningCheckBox.addActionListener(e -> handleEveningSelection());
        allDayCheckBox = new JCheckBox("All Day");
        styleCheckBox(allDayCheckBox);
        allDayCheckBox.addActionListener(e -> handleAllDaySelection());

        allDayShortCheckBox = new JCheckBox("All Day (10:00-17:00)");
        styleCheckBox(allDayShortCheckBox);
        allDayShortCheckBox.addActionListener(e -> handleAllDayShortSelection());

        allDayLongCheckBox = new JCheckBox("All Day (10:00-23:00)");
        styleCheckBox(allDayLongCheckBox);
        allDayLongCheckBox.addActionListener(e -> handleAllDayLongSelection());
        weekCheckBox = new JCheckBox("All Week");
        styleCheckBox(weekCheckBox);
        weekCheckBox.addActionListener(e -> handleWeekSelection());
        weekShortCheckBox = new JCheckBox("All Week (10:00-18:00)");
        styleCheckBox(weekShortCheckBox);
        weekShortCheckBox.addActionListener(e -> handleWeekShortSelection());

        weekLongCheckBox = new JCheckBox("All Week (10:00-23:00)");
        styleCheckBox(weekLongCheckBox);
        weekLongCheckBox.addActionListener(e -> handleWeekLongSelection());
        hideAllCheckboxes();

        checkboxPanel.add(morningCheckBox);
        checkboxPanel.add(afternoonCheckBox);
        checkboxPanel.add(eveningCheckBox);
        checkboxPanel.add(allDayCheckBox);
        checkboxPanel.add(allDayShortCheckBox);
        checkboxPanel.add(allDayLongCheckBox);
        checkboxPanel.add(weekCheckBox);
        checkboxPanel.add(weekShortCheckBox);
        checkboxPanel.add(weekLongCheckBox);


        JScrollPane tableScrollPane = createTimeSlotTable();
        tableScrollPane.setPreferredSize(new Dimension(0, 650));

        contentPanel.add(checkboxPanel, BorderLayout.NORTH);
        contentPanel.add(tableScrollPane, BorderLayout.CENTER);


        JPanel legendPanel = createLegendPanel();
        contentPanel.add(legendPanel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        return mainPanel;
    }

    /**
     * Creates the legend panel showing availability status colors.
     * @return A JPanel containing the legend
     */
    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 2));
        legendPanel.setBackground(panelColor);
        legendPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        addLegendItem(legendPanel, availableColor, "Available");
        addLegendItem(legendPanel, selectedColor, "Selected");
        addLegendItem(legendPanel, unavailableColor, "Unavailable");

        return legendPanel;
    }

    /**
     * Adds a legend item to the legend panel.
     * @param panel The panel to add the item to
     * @param color The color to display
     * @param text The text to display
     */
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

    /**
     * Handles time slot selection when clicked.
     */
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

            String currentStatus = (String) timeSlotTable.getValueAt(row, 1);

            if (currentStatus.equals("Selected")) {
                timeSlotModel.setValueAt("Available", row, 1);
            } else if (currentStatus.equals("Available")) {
                timeSlotModel.setValueAt("Selected", row, 1);
            }
        }
    }


    /**
     * Updates the total cost display based on current bookings.
     */
    private void updateTotalCost() {
        double total = bookingEvents.stream().mapToDouble(event -> event.cost).sum();
        totalLabel.setText(String.format("Total Cost: £%.2f + VAT", total));
    }


    /**
     * Creates the actions panel with save and cancel buttons.
     * @return A JPanel containing the action buttons
     */
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 10));
        panel.setBackground(panelColor);

        totalLabel = new JLabel("Total Cost: £0.00 + VAT", JLabel.CENTER);
        totalLabel.setForeground(Color.WHITE);
        totalLabel.setFont(new Font("TimesRoman", Font.PLAIN, 14));
        panel.add(totalLabel, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.setBackground(panelColor);

        JButton saveBtn = createStyledButton("Save Booking");
        saveBtn.addActionListener(e -> saveBookingsToDatabase());

        JButton cancelBtn = createStyledButton("Cancel");
        cancelBtn.addActionListener(e -> {
            dispose();
            new UserHome().setVisible(true);
        });

        buttonsPanel.add(saveBtn);
        buttonsPanel.add(cancelBtn);
        panel.add(buttonsPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the top bar with navigation buttons.
     * @param parentFrame The parent frame
     * @return A JPanel containing the top bar
     */
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

    /**
     * Confirms the current booking and performs validation.
     */
    private void confirmBooking() {
        String bookingName = bookingNameField.getText().trim();
        if (bookingName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a name for this booking event",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String clientName = clientNameField.getText().trim();
        if (clientName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a client name for this booking event",
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
        if (selectedTimes.size() < 2) {
            JOptionPane.showMessageDialog(this,
                "Minimum booking duration is 1 hour for all spaces",
                "Invalid Booking Duration",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
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
            if (hasEveningSlots && hasDaySlots && !allDayCheckBox.isSelected()) {
                JOptionPane.showMessageDialog(this,
                    "Cannot mix day and evening slots.\nPlease use all-day booking for full day reservations.",
                    "Invalid Booking Time",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        if (isPerformanceSpace(selectedSpace)) {
            int selectedHours = (selectedTimes.size() * 30) / 60;
            boolean hasEveningSlots = selectedTimes.stream()
                .anyMatch(time -> time.getHour() >= 17);
            boolean hasDaySlots = selectedTimes.stream()
                .anyMatch(time -> time.getHour() < 17);

            if (!eveningCheckBox.isSelected() && !allDayCheckBox.isSelected()) {
                if (selectedHours < 3) {
                    JOptionPane.showMessageDialog(this,
                        "Performance spaces require a minimum booking of 3 hours",
                        "Invalid Booking Duration",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (hasEveningSlots) {
                    JOptionPane.showMessageDialog(this,
                        "Regular bookings must be between 10:00 and 17:00.\nPlease use evening booking for slots after 17:00",
                        "Invalid Booking Time",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (selectedDate.getDayOfWeek().getValue() == 6) {
                    JOptionPane.showMessageDialog(this,
                        "Regular bookings are not available on Saturdays.\nPlease use evening or all-day booking.",
                        "Invalid Booking Day",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            if (hasEveningSlots && hasDaySlots && !allDayCheckBox.isSelected()) {
                JOptionPane.showMessageDialog(this,
                    "Cannot mix day and evening slots.\nPlease use all-day booking for full day reservations.",
                    "Invalid Booking Time",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        if ("Rehearsal Space".equals(selectedSpace)) {
            int selectedHours = (selectedTimes.size() * 30) / 60;
            if (!allDayShortCheckBox.isSelected() && !allDayLongCheckBox.isSelected() &&
                !weekShortCheckBox.isSelected() && !weekLongCheckBox.isSelected()) {
                if (selectedHours < 3) {
                    JOptionPane.showMessageDialog(this,
                        "Rehearsal Space requires a minimum booking of 3 hours",
                        "Invalid Booking Duration",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }
        Map<LocalTime, Boolean> timeSlots = getOrCreateTimeSlots(selectedSpace, selectedDate);
        for (LocalTime time : selectedTimes) {
            if (!timeSlots.getOrDefault(time, true)) {
                JOptionPane.showMessageDialog(this,
                        "Cannot book - some selected slots are already booked",
                        "Booking Conflict",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        createBookingEvents(selectedTimes, bookingName);


        updateUIAfterBooking();
    }

    /**
     * Collects all selected time slots.
     * @return List of selected LocalTime objects
     */
    private List<LocalTime> collectSelectedTimes() {
        List<LocalTime> selectedTimes = new ArrayList<>();
        for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
            if ("Selected".equals(timeSlotModel.getValueAt(row, 1))) {
                selectedTimes.add(LocalTime.parse((String)timeSlotModel.getValueAt(row, 0)));
            }
        }
        return selectedTimes;
    }

    /**
     * Creates booking events from selected time slots.
     * @param selectedTimes The list of selected time slots
     * @param bookingName The name of the booking
     */
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

    /**
     * Saves the current bookings to the database.
     */
    private void saveBookingsToDatabase() {
        if (bookingEvents.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No bookings to save",
                    "No Bookings",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String insertQuery = "INSERT INTO booking (UserID, BookingDate, StartTime, EndTime, Room, Client, BookingName, " +
                "BookingEndDate, ClientID, HoldingStatus, TotalCost) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26",
                "in2033t26_a", "jLxOPuQ69Mg");
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            int userId = 1;
            for (BookingEvent event : bookingEvents) {
                if (event.isDisplayOnly) continue;

                stmt.setInt(1, userId);
                stmt.setDate(2, java.sql.Date.valueOf(event.date));
                stmt.setTime(3, java.sql.Time.valueOf(event.startTime));
                stmt.setTime(4, java.sql.Time.valueOf(event.endTime));
                stmt.setString(5, event.space);
                stmt.setString(6, clientNameField.getText().trim());
                stmt.setString(7, event.eventType);

                if (event.isWeek || event.isWeekShort || event.isWeekLong) {
                    stmt.setDate(8, java.sql.Date.valueOf(event.date.plusDays(6)));
                } else {
                    stmt.setDate(8, java.sql.Date.valueOf(event.date));
                }

                stmt.setInt(9, 1);
                stmt.setString(10, "Tentative");
                stmt.setBigDecimal(11, new java.math.BigDecimal(event.cost));

                stmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Bookings saved successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            bookingEvents.clear();
            updateBookingList();
            updateTotalCost();
            updateTimeSlotGrid();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error saving bookings: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * Creates a single booking event.
     * @param startTime The start time of the booking
     * @param endTime The end time of the booking
     * @param bookingName The name of the booking
     */
    private void createSingleBookingEvent(LocalTime startTime, LocalTime endTime, String bookingName) {
        boolean isAllDayShort = allDayShortCheckBox != null && allDayShortCheckBox.isSelected() && allDayShortCheckBox.isVisible();
        boolean isAllDayLong = allDayLongCheckBox != null && allDayLongCheckBox.isSelected() && allDayLongCheckBox.isVisible();
        boolean isAllDay = allDayCheckBox.isSelected() && allDayCheckBox.isVisible();

        boolean isWeekShort = weekShortCheckBox != null && weekShortCheckBox.isSelected() && weekShortCheckBox.isVisible();
        boolean isWeekLong = weekLongCheckBox != null && weekLongCheckBox.isSelected() && weekLongCheckBox.isVisible();
        boolean isWeek = weekCheckBox != null && weekCheckBox.isSelected() && weekCheckBox.isVisible();
        boolean isMorning = morningCheckBox != null && morningCheckBox.isSelected() && morningCheckBox.isVisible();
        boolean isAfternoon = afternoonCheckBox != null && afternoonCheckBox.isSelected() && afternoonCheckBox.isVisible();

        if (isWeek || isWeekShort || isWeekLong) {
            LocalTime bookingStart = LocalTime.of(10, 0);
            LocalTime bookingEnd;

            if (isWeek) {
                bookingEnd = LocalTime.of(0, 0);
            } else if (isWeekShort) {
                bookingEnd = LocalTime.of(18, 0);
            } else {
                bookingEnd = LocalTime.of(23, 0);
            }
            LocalDate endDate = selectedDate.plusDays(6);


            BookingEvent mainEvent = new BookingEvent(
                selectedDate, bookingStart, bookingEnd,
                selectedSpace, String.format("%s (Weekly: %s - %s)",
                    bookingName,
                    selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
                isAllDay || isAllDayShort || isAllDayLong,
                eveningCheckBox.isSelected() && eveningCheckBox.isVisible(),
                isAllDayShort,
                isAllDayLong,
                isWeekShort,
                isWeekLong,
                isMorning,
                isAfternoon,
                false,
                isWeek
            );
            mainEvent.isWeek = isWeek;
            bookingEvents.add(mainEvent);




            LocalDate currentDate = selectedDate;
            for (int day = 1; day < 7; day++) {
                currentDate = currentDate.plusDays(1);
                BookingEvent hiddenEvent = new BookingEvent(
                    currentDate, bookingStart, bookingEnd,
                    selectedSpace, bookingName,
                    true,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    true,
                    isWeek
                ) {

                    @Override
                    public String toString() {
                        return null;
                    }
                };
                bookingEvents.add(hiddenEvent);



                Map<LocalTime, Boolean> timeSlots = getOrCreateTimeSlots(selectedSpace, currentDate);
                LocalTime current = bookingStart;
                while (!current.equals(bookingEnd)) {
                    timeSlots.put(current, false);
                    current = current.plusMinutes(30);
                }
            }
        } else {

            BookingEvent event = new BookingEvent(
                selectedDate, startTime, endTime,
                selectedSpace, bookingName,
                isAllDay || isAllDayShort || isAllDayLong,
                eveningCheckBox.isSelected() && eveningCheckBox.isVisible(),
                isAllDayShort,
                isAllDayLong,
                isWeekShort,
                isWeekLong,
                isMorning,
                isAfternoon,
                false,
                isWeek
            );
            event.isWeek = isWeek;
            bookingEvents.add(event);

            Map<LocalTime, Boolean> timeSlots = getOrCreateTimeSlots(selectedSpace, selectedDate);
            LocalTime current = startTime;
            while (!current.equals(endTime)) {
                timeSlots.put(current, false);
                current = current.plusMinutes(30);
            }
        }


        updateTotalCost();
        updateTimeSlotGrid();

    }

    /**
     * Updates the UI after a booking is confirmed.
     */
    private void updateUIAfterBooking() {
        for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
            if ("Selected".equals(timeSlotModel.getValueAt(row, 1))) {
                timeSlotModel.setValueAt("Your Booking", row, 1);
            }
        }
        morningCheckBox.setSelected(false);
        afternoonCheckBox.setSelected(false);
        allDayCheckBox.setSelected(false);
        allDayShortCheckBox.setSelected(false);
        allDayLongCheckBox.setSelected(false);
        eveningCheckBox.setSelected(false);
        weekCheckBox.setSelected(false);
        weekShortCheckBox.setSelected(false);
        weekLongCheckBox.setSelected(false);

        bookingNameField.setText("");
        clientNameField.setText("");
        updateBookingList();
        updateTotalCost();
    }

    /**
     * Updates the booking list display.
     */
    private void updateBookingList() {
        bookingListModel.clear();
        for (BookingEvent event : bookingEvents) {
            if (event.toString() != null) {
                String timeRange = event.startTime.format(DateTimeFormatter.ofPattern("HH:mm")) +
                                 " to " +
                                 (event.endTime.equals(LocalTime.of(0, 0)) ? "00:00" : event.endTime.format(DateTimeFormatter.ofPattern("HH:mm")));

                if (event.isWeek || event.isWeekShort || event.isWeekLong) {
                    bookingListModel.addElement(String.format("%s\n%s",
                        event.eventType,
                        timeRange
                    ));
                } else {

                    bookingListModel.addElement(String.format("%s\n%s, %s",
                        event.eventType,
                        event.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        timeRange
                    ));
                }
            }
        }
    }

    /**
     * Creates the right panel containing the booking list.
     * @return A JPanel containing the booking list
     */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(panelColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel("Your Booking Events");
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        bookingListModel = new DefaultListModel<>();
        bookingList = new JList<>(bookingListModel);
        setupBookingList();

        JScrollPane scrollPane = new JScrollPane(bookingList);
        scrollPane.setBorder(null);
        scrollPane.setBackground(panelColor);
        scrollPane.getViewport().setBackground(panelColor);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel actionsPanel = createActionsPanel();
        panel.add(actionsPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Sets up the booking list with appropriate styling and behavior.
     */
    private void setupBookingList() {
        bookingList.setBackground(panelColor);
        bookingList.setForeground(Color.WHITE);
        bookingList.setFont(new Font("TimesRoman", Font.PLAIN, 14));
        bookingList.setBorder(null);

        int[] hoveredButton = {-1, 0};

        bookingList.setCellRenderer(new BookingListCellRenderer(hoveredButton));
        setupBookingListListeners(hoveredButton);
        bookingList.setFixedCellHeight(-1);
    }

    /**
     * Sets up mouse listeners for the booking list.
     * @param hoveredButton Array to track hover state
     */
    private void setupBookingListListeners(int[] hoveredButton) {
        bookingList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateButtonHoverState(e.getPoint(), hoveredButton);
            }
        });

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

    /**
     * Updates the hover state of buttons in the booking list.
     * @param point The current mouse point
     * @param hoveredButton Array to track hover state
     */
    private void updateButtonHoverState(Point point, int[] hoveredButton) {
        int index = bookingList.locationToIndex(point);
        if (index >= 0) {
            Rectangle bounds = bookingList.getCellBounds(index, index);
            if (bounds != null) {
                int x = point.x - bounds.x;
                int rightEdge = bounds.width;

                if (x >= rightEdge - 40) {
                    hoveredButton[0] = index;
                    hoveredButton[1] = 0;
                    bookingList.repaint();
                } else if (hoveredButton[0] != -1) {
                    resetHoverState(hoveredButton);
                }
            }
        }
    }

    /**
     * Resets the hover state of buttons in the booking list.
     * @param hoveredButton Array to track hover state
     */
    private void resetHoverState(int[] hoveredButton) {
        hoveredButton[0] = -1;
        hoveredButton[1] = -1;
        bookingList.repaint();
    }

    /**
     * Handles clicks on the booking list.
     * @param point The click location
     */
    private void handleBookingListClick(Point point) {

        int index = bookingList.locationToIndex(point);
        if (index >= 0) {
            Rectangle bounds = bookingList.getCellBounds(index, index);
            if (bounds != null) {
                int x = point.x - bounds.x;
                int rightEdge = bounds.width;

                if (x >= rightEdge - 40) {
                    deleteBooking(index);
                }
            }
        }
    }

    /**
     * Deletes a booking from the list.
     * @param index The index of the booking to delete
     */
    private void deleteBooking(int index) {

        if (index >= 0 && index < bookingEvents.size()) {
            BookingEvent event = bookingEvents.get(index);

            boolean isWeeklyBooking = event.eventType != null &&
                (event.eventType.contains("Weekly:") || event.isWeek || event.isWeekShort || event.isWeekLong);

            if (isWeeklyBooking) {

                LocalDate startDate = event.date;
                String baseEventName = event.eventType.split(" \\(Weekly:")[0];

                bookingEvents.removeIf(e ->
                    (e.date.isEqual(startDate) || (e.date.isAfter(startDate) && e.date.isBefore(startDate.plusDays(7)))) &&
                    e.space.equals(event.space) &&
                    (e.eventType.equals(baseEventName) || (e.eventType != null && e.eventType.contains("Weekly:")))
                );

                for (int day = 0; day < 7; day++) {
                    LocalDate currentDate = startDate.plusDays(day);
                    Map<LocalTime, Boolean> timeSlots = getOrCreateTimeSlots(event.space, currentDate);

                    LocalTime current = event.startTime;
                    while (!current.equals(event.endTime)) {
                        timeSlots.put(current, true);
                        current = current.plusMinutes(30);
                    }
                }
            } else {

                Map<LocalTime, Boolean> timeSlots = getOrCreateTimeSlots(event.space, event.date);
                LocalTime current = event.startTime;
                while (!current.equals(event.endTime)) {
                    timeSlots.put(current, true);
                    current = current.plusMinutes(30);
                }
                bookingEvents.remove(index);
            }

            updateBookingList();
            updateTimeSlotGrid();
            updateTotalCost();
        }
    }

    /**
     * Creates the month view content panel.
     * @return A JPanel containing the month view
     */
    private JPanel createMonthViewContent() {
        JPanel monthContent = new JPanel(new BorderLayout());
        monthContent.setBackground(panelColor);
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

        calendarGrid = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarGrid.setBackground(panelColor);

        dateLabel = new JLabel("", JLabel.CENTER);
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setFont(new Font("TimesRoman", Font.PLAIN, 14));
        dateLabel.setText(selectedDate.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")));
        dateLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        monthContent.add(monthHeader, BorderLayout.NORTH);
        monthContent.add(calendarGrid, BorderLayout.CENTER);
        monthContent.add(dateLabel, BorderLayout.SOUTH);

        updateCalendarGrid();

        return monthContent;
    }

    /**
     * Creates the booking controls panel.
     * @return A JPanel containing booking controls
     */
    private JPanel createBookingControlsPanel() {
        JPanel bookingControlsPanel = new JPanel(new GridBagLayout());
        bookingControlsPanel.setBackground(panelColor);
        bookingControlsPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JPanel bookingNamePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        bookingNamePanel.setBackground(panelColor);

        JLabel bookingNameLabel = new JLabel("Event Booking Name:");
        bookingNameLabel.setForeground(Color.WHITE);
        bookingNameLabel.setFont(new Font("TimesRoman", Font.PLAIN, 14));

        bookingNameField = new JTextField();
        bookingNameField.setFont(new Font("TimesRoman", Font.PLAIN, 14));

        bookingNamePanel.add(bookingNameLabel);
        bookingNamePanel.add(bookingNameField);
        JLabel clientNameLabel = new JLabel("Client Name:");
        clientNameLabel.setForeground(Color.WHITE);
        clientNameLabel.setFont(new Font("TimesRoman", Font.PLAIN, 14));

        clientNameField = new JTextField();
        clientNameField.setFont(new Font("TimesRoman", Font.PLAIN, 14));

        JPanel clientNamePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        clientNamePanel.setBackground(panelColor);
        clientNamePanel.add(clientNameLabel);
        clientNamePanel.add(clientNameField);
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
        JButton confirmButton = createStyledButton("Confirm Event Booking");
        confirmButton.setPreferredSize(new Dimension(250, 35));
        confirmButton.addActionListener(e -> confirmBooking());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        bookingControlsPanel.add(bookingNamePanel, gbc);

        gbc.gridy = 1;
        bookingControlsPanel.add(clientNamePanel, gbc);

        gbc.gridy = 2;
        bookingControlsPanel.add(spacePanel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(15, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        bookingControlsPanel.add(confirmButton, gbc);

        return bookingControlsPanel;
    }

    /**
     * Loads existing bookings from the database.
     */
    private void loadExistingBookings() {
        if (selectedSpace == null) return;
        Map<LocalTime, Boolean> timeSlots = getOrCreateTimeSlots(selectedSpace, selectedDate);
        timeSlots.replaceAll((k, v) -> true);

        String query = "SELECT StartTime, EndTime FROM booking WHERE Room = ? AND BookingDate = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26",
                "in2033t26_a", "jLxOPuQ69Mg");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, selectedSpace);
            stmt.setDate(2, java.sql.Date.valueOf(selectedDate));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LocalTime startTime = rs.getTime("StartTime").toLocalTime();
                LocalTime endTime = rs.getTime("EndTime").toLocalTime();


                LocalTime current = startTime;
                while (!current.equals(endTime)) {
                    timeSlots.put(current, false);
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
        updateTimeSlotGrid();
    }

    /**
     * Checks if an all-day booking is available.
     * @return true if all-day booking is available, false otherwise
     */
    private boolean isAllDayBookingAvailable() {
        for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
            String status = (String) timeSlotModel.getValueAt(row, 1);
            if (status.equals("Unavailable") || status.equals("Your Booking")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a day button for the calendar.
     * @param date The date for the button
     * @param day The day number
     * @return A JButton for the specified day
     */
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

    /**
     * Handles morning booking selection.
     */
    private void handleMorningSelection() {
        if (morningCheckBox.isSelected()) {
            afternoonCheckBox.setSelected(false);
            allDayCheckBox.setSelected(false);
            allDayShortCheckBox.setSelected(false);
            allDayLongCheckBox.setSelected(false);
            eveningCheckBox.setSelected(false);
            weekCheckBox.setSelected(false);
            for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                String timeStr = (String) timeSlotModel.getValueAt(row, 0);
                LocalTime time = LocalTime.parse(timeStr);
                if (time.getHour() >= 10 && time.getHour() < 12) {
                    if (timeSlotModel.getValueAt(row, 1).equals("Available")) {
                        timeSlotModel.setValueAt("Selected", row, 1);
                    }
                } else if (timeSlotModel.getValueAt(row, 1).equals("Selected")) {
                    timeSlotModel.setValueAt("Available", row, 1);
                }
            }
        } else {
            for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                if (timeSlotModel.getValueAt(row, 1).equals("Selected")) {
                    timeSlotModel.setValueAt("Available", row, 1);
                }
            }
        }
    }

    /**
     * Handles afternoon booking selection.
     */
    private void handleAfternoonSelection() {
        if (afternoonCheckBox.isSelected()) {
            morningCheckBox.setSelected(false);
            allDayCheckBox.setSelected(false);
            allDayShortCheckBox.setSelected(false);
            allDayLongCheckBox.setSelected(false);
            eveningCheckBox.setSelected(false);
            weekCheckBox.setSelected(false);
            for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                String timeStr = (String) timeSlotModel.getValueAt(row, 0);
                LocalTime time = LocalTime.parse(timeStr);
                if (time.getHour() >= 12 && time.getHour() < 17) {
                    if (timeSlotModel.getValueAt(row, 1).equals("Available")) {
                        timeSlotModel.setValueAt("Selected", row, 1);
                    }
                } else if (timeSlotModel.getValueAt(row, 1).equals("Selected")) {
                    timeSlotModel.setValueAt("Available", row, 1);
                }
            }
        } else {
            for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                if (timeSlotModel.getValueAt(row, 1).equals("Selected")) {
                    timeSlotModel.setValueAt("Available", row, 1);
                }
            }
        }
    }

    /**
     * Checks if a weekly booking is available.
     * @return true if weekly booking is available, false otherwise
     */
    private boolean isWeeklyBookingAvailable() {
        LocalDate currentDate = selectedDate;
        LocalTime startOfDay = LocalTime.of(10, 0);
        LocalTime endOfDay = LocalTime.of(23, 0);

        for (int day = 0; day < 7; day++) {
            Map<LocalTime, Boolean> timeSlots = getOrCreateTimeSlots(selectedSpace, currentDate);
            LocalTime currentTime = startOfDay;
            while (!currentTime.equals(endOfDay)) {
                if (!timeSlots.getOrDefault(currentTime, false)) {
                    return false;
                }
                currentTime = currentTime.plusMinutes(30);
            }

            currentDate = currentDate.plusDays(1);
        }

        return true;
    }

    /**
     * Handles week booking selection.
     */
    private void handleWeekSelection() {
        if (weekCheckBox.isSelected()) {
            if (!isWeeklyBookingAvailable()) {
                weekCheckBox.setSelected(false);
                JOptionPane.showMessageDialog(this,
                    "Weekly booking is not available. Some time slots are already booked in the next 7 days.",
                    "Booking Not Available",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            morningCheckBox.setSelected(false);
            afternoonCheckBox.setSelected(false);
            allDayCheckBox.setSelected(false);
            allDayShortCheckBox.setSelected(false);
            allDayLongCheckBox.setSelected(false);
            eveningCheckBox.setSelected(false);

            selectAllDaySlots();
        } else {
            clearAllSelections();
        }
    }

    /**
     * Handles week short booking selection.
     */
    private void handleWeekShortSelection() {
        if (weekShortCheckBox.isSelected()) {
            if (!isWeeklyBookingAvailableForHours(LocalTime.of(10, 0), LocalTime.of(18, 0))) {
                weekShortCheckBox.setSelected(false);
                JOptionPane.showMessageDialog(this,
                    "Weekly booking (10:00-18:00) is not available. Some time slots are already booked in the next 7 days.",
                    "Booking Not Available",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            weekLongCheckBox.setSelected(false);
            allDayShortCheckBox.setSelected(false);
            allDayLongCheckBox.setSelected(false);

            selectTimeRangeSlots(LocalTime.of(10, 0), LocalTime.of(18, 0));
        } else {
            clearAllSelections();
        }
    }

    /**
     * Handles week long booking selection.
     */
    private void handleWeekLongSelection() {
        if (weekLongCheckBox.isSelected()) {
            if (!isWeeklyBookingAvailableForHours(LocalTime.of(10, 0), LocalTime.of(23, 0))) {
                weekLongCheckBox.setSelected(false);
                JOptionPane.showMessageDialog(this,
                    "Weekly booking (10:00-23:00) is not available. Some time slots are already booked in the next 7 days.",
                    "Booking Not Available",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            weekShortCheckBox.setSelected(false);
            allDayShortCheckBox.setSelected(false);
            allDayLongCheckBox.setSelected(false);

            selectTimeRangeSlots(LocalTime.of(10, 0), LocalTime.of(23, 0));
        } else {
            clearAllSelections();
        }
    }

    /**
     * Checks if a weekly booking is available for specific hours.
     * @param startHour The start hour
     * @param endHour The end hour
     * @return true if booking is available, false otherwise
     */
    private boolean isWeeklyBookingAvailableForHours(LocalTime startHour, LocalTime endHour) {
        LocalDate currentDate = selectedDate;

        for (int day = 0; day < 7; day++) {
            Map<LocalTime, Boolean> timeSlots = getOrCreateTimeSlots(selectedSpace, currentDate);

            LocalTime currentTime = startHour;
            while (!currentTime.equals(endHour)) {
                if (!timeSlots.getOrDefault(currentTime, false)) {
                    return false;
                }
                currentTime = currentTime.plusMinutes(30);
            }

            currentDate = currentDate.plusDays(1);
        }

        return true;
    }

    /**
     * Selects time slots within a specific range.
     * @param startTime The start time
     * @param endTime The end time
     */
    private void selectTimeRangeSlots(LocalTime startTime, LocalTime endTime) {
        for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
            String timeStr = (String) timeSlotModel.getValueAt(row, 0);
            LocalTime time = LocalTime.parse(timeStr);
            if (time.compareTo(startTime) >= 0 && time.compareTo(endTime) < 0) {
                if (timeSlotModel.getValueAt(row, 1).equals("Available")) {
                    timeSlotModel.setValueAt("Selected", row, 1);
                }
            } else if (timeSlotModel.getValueAt(row, 1).equals("Selected")) {
                timeSlotModel.setValueAt("Available", row, 1);
            }
        }
    }

    /**
     * Selects all day time slots.
     */
    private void selectAllDaySlots() {
        for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
            if (timeSlotModel.getValueAt(row, 1).equals("Available")) {
                timeSlotModel.setValueAt("Selected", row, 1);
            }
        }
    }

    /**
     * Clears all selected time slots.
     */
    private void clearAllSelections() {
        for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
            if (timeSlotModel.getValueAt(row, 1).equals("Selected")) {
                timeSlotModel.setValueAt("Available", row, 1);
            }
        }
    }

    /**
     * Handles all-day short booking selection.
     */
    private void handleAllDayShortSelection() {
        if (allDayShortCheckBox.isSelected()) {
            allDayLongCheckBox.setSelected(false);
            weekShortCheckBox.setSelected(false);
            weekLongCheckBox.setSelected(false);
            for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                String timeStr = (String) timeSlotModel.getValueAt(row, 0);
                LocalTime time = LocalTime.parse(timeStr);
                if (time.getHour() >= 10 && time.getHour() < 17) {
                    if (timeSlotModel.getValueAt(row, 1).equals("Available")) {
                        timeSlotModel.setValueAt("Selected", row, 1);
                    }
                } else if (timeSlotModel.getValueAt(row, 1).equals("Selected")) {
                    timeSlotModel.setValueAt("Available", row, 1);
                }
            }
        } else {

            for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                if (timeSlotModel.getValueAt(row, 1).equals("Selected")) {
                    timeSlotModel.setValueAt("Available", row, 1);
                }
            }
        }
    }

    /**
     * Handles all-day long booking selection.
     */
    private void handleAllDayLongSelection() {
        if (allDayLongCheckBox.isSelected()) {
            allDayShortCheckBox.setSelected(false);
            weekShortCheckBox.setSelected(false);
            weekLongCheckBox.setSelected(false);

            for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                String timeStr = (String) timeSlotModel.getValueAt(row, 0);
                LocalTime time = LocalTime.parse(timeStr);
                if (time.getHour() >= 10 && time.getHour() < 23) {
                    if (timeSlotModel.getValueAt(row, 1).equals("Available")) {
                        timeSlotModel.setValueAt("Selected", row, 1);
                    }
                } else if (timeSlotModel.getValueAt(row, 1).equals("Selected")) {
                    timeSlotModel.setValueAt("Available", row, 1);
                }
            }
        } else {
            for (int row = 0; row < timeSlotModel.getRowCount(); row++) {
                if (timeSlotModel.getValueAt(row, 1).equals("Selected")) {
                    timeSlotModel.setValueAt("Available", row, 1);
                }
            }
        }
    }

    /**
     * Styles a checkbox with consistent appearance.
     * @param checkbox The checkbox to style
     */
    private void styleCheckBox(JCheckBox checkbox) {
        checkbox.setFont(new Font("TimesRoman", Font.PLAIN, 12));
        checkbox.setForeground(Color.WHITE);
        checkbox.setBackground(panelColor);
    }

    /**
     * Custom cell renderer for the time slot table.
     * Handles the display of different booking statuses with appropriate colors and hover effects.
     */
    class TimeSlotCellRenderer extends DefaultTableCellRenderer {
        private int hoveredRow = -1;

        /**
         * Sets the currently hovered row for hover effect display.
         * @param row The row index being hovered over
         */
        public void setHoveredRow(int row) {
            hoveredRow = row;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 1) {
                String status = value != null ? value.toString() : "";
                setHorizontalAlignment(JLabel.CENTER);

                switch (status) {
                    case "Available":
                        setBackground(availableColor);
                        setForeground(Color.BLACK);
                        break;
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
                        setBackground(table.getBackground());
                        setForeground(table.getForeground());
                }

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
    }

    /**
     * Custom cell renderer for the booking list.
     * Handles the display of booking items with appropriate styling and hover effects.
     */
    private class BookingListCellRenderer extends DefaultListCellRenderer {
        private final int[] hoveredButton;

        /**
         * Creates a new booking list cell renderer.
         * @param hoveredButton Array to track hover state
         */
        public BookingListCellRenderer(int[] hoveredButton) {
            this.hoveredButton = hoveredButton;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JPanel cellPanel = new JPanel(new BorderLayout(5, 0));
            cellPanel.setBackground(isSelected ? list.getSelectionBackground() : panelColor);

            JTextArea textArea = createBookingTextArea(value.toString(), list, cellPanel, isSelected);

            JPanel buttonsPanel = createBookingButtonsPanel(index, isSelected, list);

            cellPanel.add(textArea, BorderLayout.CENTER);
            cellPanel.add(buttonsPanel, BorderLayout.EAST);

            return cellPanel;
        }

        /**
         * Creates a text area for displaying booking information.
         * @param text The text to display
         * @param list The parent list component
         * @param parent The parent panel
         * @param isSelected Whether the item is selected
         * @return A configured JTextArea
         */
        private JTextArea createBookingTextArea(String text, JList<?> list, JPanel parent, boolean isSelected) {
            JTextArea textArea = new JTextArea(text);
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            textArea.setBackground(parent.getBackground());
            textArea.setForeground(isSelected ? list.getSelectionForeground() : Color.WHITE);
            textArea.setFont(list.getFont());
            textArea.setBorder(createNormalBorder());

            int width = list.getWidth();
            if (width > 0) {
                textArea.setSize(width - 40, Short.MAX_VALUE);
                textArea.setPreferredSize(new Dimension(width - 40, textArea.getPreferredSize().height));
            }

            return textArea;
        }

        /**
         * Creates a panel containing action buttons for a booking item.
         * @param index The index of the booking item
         * @param isSelected Whether the item is selected
         * @param list The parent list component
         * @return A panel containing the action buttons
         */
        private JPanel createBookingButtonsPanel(int index, boolean isSelected, JList<?> list) {
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            buttonsPanel.setBackground(isSelected ? list.getSelectionBackground() : panelColor);

            JLabel deleteButton = new JLabel("✕");
            deleteButton.setFont(new Font("TimesRoman", Font.PLAIN, 16));
            deleteButton.setForeground(isSelected ? list.getSelectionForeground() : Color.WHITE);

            if (hoveredButton[0] == index && hoveredButton[1] == 0) {
                deleteButton.setBorder(createHoverBorder());
            } else {
                deleteButton.setBorder(createNormalBorder());
            }

            buttonsPanel.add(deleteButton);

            return buttonsPanel;
        }
    }
}
