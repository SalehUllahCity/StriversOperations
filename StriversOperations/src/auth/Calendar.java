package auth;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.Locale;

public class Calendar extends JFrame {

    private JPanel calendarPanel;
    private JLabel monthYearLabel;
    private LocalDate currentDate;

    private Color backgroundColour = new Color(18, 32, 35, 255);
    private int fontSize = 30;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Calendar frame = new Calendar();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public Calendar(){
        setTitle("Lancaster's Music Hall Software");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(backgroundColour);
        setLayout(new BorderLayout());

        currentDate = LocalDate.now().withDayOfMonth(1);

        //Header panel: contains settings, title, and month navigation
        createHeaderPanel();

        //Calendar Grid
        calendarPanel = new JPanel();
        calendarPanel.setBackground(backgroundColour);
        calendarPanel.setLayout(new GridLayout(0, 7, 10, 10));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));
        add(calendarPanel, BorderLayout.CENTER);

        //Populate calendar with days
        refreshCalendar();
    }

    /**
     * Creates the top section with navigation buttons, month label, and settings.
     */
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(backgroundColour);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // Navigation buttons
        JButton prevButton = createStyledButton("<");
        JButton nextButton = createStyledButton(">");
        prevButton.addActionListener(e -> {
            currentDate = currentDate.minusMonths(1);
            refreshCalendar();
        });
        nextButton.addActionListener(e -> {
            currentDate = currentDate.plusMonths(1);
            refreshCalendar();
        });

        //Current month label and year label
        monthYearLabel = new JLabel("", JLabel.CENTER);
        monthYearLabel.setForeground(Color.WHITE);
        monthYearLabel.setFont(new Font("TimesRoman", Font.BOLD, fontSize));

        headerPanel.add(prevButton, BorderLayout.WEST);
        headerPanel.add(monthYearLabel, BorderLayout.CENTER);
        headerPanel.add(nextButton, BorderLayout.EAST);

        //Wrapper to include settings panel and header
        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setBackground(backgroundColour);

        //Add Settings bar and header
        topWrapper.add(new SettingScreen(this));
        topWrapper.add(headerPanel);

        add(topWrapper, BorderLayout.NORTH);
    }

    private void refreshCalendar() {
        calendarPanel.removeAll();

        Month month = currentDate.getMonth();
        int year = currentDate.getYear();

        //Update the label at the top
        monthYearLabel.setText(month.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase() + " " + year);

        //Add weekday headers (Mon - Sun)
        for (DayOfWeek dow : DayOfWeek.values()) {
            JLabel dayLabel = new JLabel(dow.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase(), JLabel.CENTER);
            dayLabel.setForeground(Color.WHITE);
            dayLabel.setFont(new Font("TimesRoman", Font.BOLD, fontSize));
            calendarPanel.add(dayLabel);
        }

        //Calculate how many blank spaces before the 1st day
        LocalDate firstDayOfMonth = currentDate;
        int startDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue(); //Monday = 1
        int shift = startDayOfWeek % 7;
        int daysInMonth = month.length(Year.isLeap(year));

        for (int i = 0; i < shift; i++) {
            calendarPanel.add(new JLabel(""));
        }

        //Fill in the actual calendar days
        for (int day = 1; day <= daysInMonth; day++) {
            final int d = day;
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("TimesRoman", Font.BOLD, fontSize));
            dayButton.setForeground(Color.WHITE);
            dayButton.setBackground(new Color(36, 50, 55));
            dayButton.setFocusPainted(false);
            dayButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            dayButton.addActionListener(e -> showBookingDialog(d));
            addHoverEffect(dayButton);
            calendarPanel.add(dayButton);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("TimesRoman", Font.BOLD, fontSize));
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        addHoverEffect(button);
        return button;
    }

    private void showBookingDialog(int day) {
        JOptionPane.showMessageDialog(this, "Booking info for " + currentDate.withDayOfMonth(day), "Booking Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addHoverEffect(JButton button) {
        button.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
            }
        });
    }
}
