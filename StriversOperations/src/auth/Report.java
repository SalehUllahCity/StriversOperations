package auth;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Report extends JFrame {

    private final Color background = new Color(18, 32, 35, 255);
    private final int fontSize = 22;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Report frame = new Report();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create the frame.
     */
    public Report(){
        setTitle("Lancaster's Music Hall Software");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel();
        contentPane.setBackground(background);
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        JLabel title = new JLabel("Reports", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("TimesRoman", Font.BOLD, 36));
        title.setBorder(new EmptyBorder(20, 0, 20, 0));
        contentPane.add(title, BorderLayout.NORTH);

        //Tabs for each report type
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("TimesRoman", Font.BOLD, fontSize));

        tabbedPane.addTab("Venue Usage", createVenueUsageTab());
        tabbedPane.addTab("Daily Sheets", createDailySheetsTab());
        tabbedPane.addTab("Financial Summary", createFinanceTab());

        contentPane.add(tabbedPane, BorderLayout.CENTER);
    }

    //Tab 1: Venue usage report across dates/spaces/bookings
    private JPanel createVenueUsageTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(background);

        String[] columns = {
                "Booking Name", "Client", "Space", "Start Date", "End Date", "Configuration", "Held?", "Contracted?"
        };

        JTable table = new JTable(new DefaultTableModel(columns, 0));
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    //Tab 2: Daily run sheet for operational setup
    private JPanel createDailySheetsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(background);

        String[] columns = {
                "Date", "Room", "Used By", "Setup Required", "Seating Config", "Accessibility Notes"
        };

        JTable table = new JTable(new DefaultTableModel(columns, 0));
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    //Tab 3: Financial summary per booking
    private JPanel createFinanceTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(background);

        String[] columns = {
                "Booking Name", "Client", "Hire Fee (£)", "Ticket Revenue (£)", "Payable to Client (£)", "Net Income (£)"
        };

        JTable table = new JTable(new DefaultTableModel(columns, 0));
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Shared table styling
    private void styleTable(JTable table) {
        table.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("TimesRoman", Font.BOLD, 20));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
    }
}
