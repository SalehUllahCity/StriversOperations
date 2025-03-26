package auth;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private JLabel monthLabel;
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

        //Top Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(backgroundColour);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JButton prevButton = createStyledButton("<");
        JButton nextButton = createStyledButton(">");

        monthLabel = new JLabel("", JLabel.CENTER);
        monthLabel.setForeground(Color.WHITE);
        monthLabel.setFont(new Font("TimesRoman", Font.BOLD, fontSize));

        headerPanel.add(prevButton, BorderLayout.WEST);
        headerPanel.add(monthLabel, BorderLayout.CENTER);
        headerPanel.add(nextButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        //Calendar Grid
        calendarPanel = new JPanel();
        calendarPanel.setBackground(backgroundColour);
        calendarPanel.setLayout(new GridLayout(0, 7, 10, 10));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));
        add(calendarPanel, BorderLayout.CENTER);

        //Button Actions
        prevButton.addActionListener(e -> {
            currentDate = currentDate.minusMonths(1);
            refreshCalendar();
        });

        nextButton.addActionListener(e -> {
            currentDate = currentDate.plusMonths(1);
            refreshCalendar();
        });

        refreshCalendar();
    }

    private void refreshCalendar() {
        calendarPanel.removeAll();

        Month month = currentDate.getMonth();
        int year = currentDate.getYear();
        monthLabel.setText(month.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase() + " " + year);

        //Day headers
        for (DayOfWeek dow : DayOfWeek.values()) {
            JLabel dayLabel = new JLabel(dow.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase(), JLabel.CENTER);
            dayLabel.setForeground(Color.WHITE);
            dayLabel.setFont(new Font("TimesRoman", Font.BOLD, fontSize));
            calendarPanel.add(dayLabel);
        }

        LocalDate firstDayOfMonth = currentDate;
        int startDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue(); //Monday = 1
        int shift = startDayOfWeek % 7;
        int daysInMonth = month.length(Year.isLeap(year));

        for (int i = 0; i < shift; i++) {
            calendarPanel.add(new JLabel(""));
        }

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
}
