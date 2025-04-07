package auth;


import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

import javax.swing.*;

public class UserHome extends JFrame {

    private static final long serialVersionUID = 1;
    private JPanel contentPane;
    private int fontSize = 30;
    private JTextField usernameField;
    private JPasswordField passwordField;


    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                UserHome frame = new UserHome();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }



    /**
     * Create the frame.
     */
    public UserHome() { // pass String UserName

        setTitle("Lancaster's Music Hall Software: Home");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);



        getContentPane().setBackground(new Color(18,32,35,255));

        JLabel titleLabel = new JLabel("");
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 50));
        titleLabel.setForeground(new Color(255,255,255));

        JPanel titlePanel = new JPanel();
        // titlePanel.setBackground(Color.black);
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);

        JButton reportButton = createStyledButton("Reports");
        JButton reviewsButton = createStyledButton("Reviews");
        JButton calendarButton = createStyledButton("Calendar");
        JButton bookingButton = createStyledButton("Bookings");
        JButton logoutButton = createStyledButton("Logout");
        JButton diaryButton = createStyledButton("Diary");
        JButton clientButton = createStyledButton("Clients");
        JButton roomButton = createStyledButton("Room Information");
        JButton helpButton = createStyledButton("Help");

        // Add buttons to the frame directly at the bottom
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        // buttonPanel.setBackground(Color.black);
        buttonPanel.setOpaque(false);
        buttonPanel.add(reportButton);
        buttonPanel.add(calendarButton);
        buttonPanel.add(bookingButton);
        buttonPanel.add(reviewsButton);
        buttonPanel.add(diaryButton);
        buttonPanel.add(helpButton);
        buttonPanel.add(clientButton);
        buttonPanel.add(roomButton);
        buttonPanel.add(logoutButton);


        // Load logo
        ImageIcon characterIcon = new ImageIcon(getClass().getResource("/data/logo/logo.png"));

        JLabel characterLabel = new JLabel(characterIcon);

        // Add components to the frame using GridBagLayout
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Add title panel to the top
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0;
        getContentPane().add(titlePanel, gbc);

        // Add character label to the middle
        gbc.gridy = 0;
        gbc.weighty = 0;
        getContentPane().add(characterLabel, gbc);

        // Add button panel to the bottom
        gbc.gridy = 6;
        gbc.weighty = 0.2;
        getContentPane().add(buttonPanel, gbc);

        // Add action listeners

        reportButton.addActionListener(e -> {
            setVisible(false);
            try {
                new Report().setVisible(true);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }

        });

        diaryButton.addActionListener(e -> {
            setVisible(false);
            new Diary().setVisible(true);
        });

        calendarButton.addActionListener(e -> {
            setVisible(false);
            new Calendar().setVisible(true);

        });

        bookingButton.addActionListener(e -> {
            setVisible(false);
            new Booking().setVisible(true);

        });


        logoutButton.addActionListener(e -> {
            setVisible(false);
            new UserLogin().setVisible(true);

        });

        helpButton.addActionListener(e -> {
            setVisible(false);
            new Help().setVisible(true);
        });

        reviewsButton.addActionListener(e -> {
            setVisible(false);
            new Reviews().setVisible(true);
        });

        clientButton.addActionListener(e -> {
            setVisible(false);
            new Clients().setVisible(true);
        });

        roomButton.addActionListener(e -> {
            setVisible(false);
            new Rooms().setVisible(true);
        });

        // Add mouse listeners to the buttons for hover effect
        addHoverEffect(calendarButton);
        addHoverEffect(reportButton);
        addHoverEffect(bookingButton);
        addHoverEffect(diaryButton);
        addHoverEffect(logoutButton);
        addHoverEffect(reviewsButton);
        addHoverEffect(diaryButton);
        addHoverEffect(clientButton);
        addHoverEffect(roomButton);
    }

    // Method to create styled buttons
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("TimesRoman", Font.BOLD, fontSize));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(Color.black);
        button.setForeground(Color.white);
        return button;
    }

    private void addHoverEffect(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(new Color(255, 255, 255));
            }
        });
    }
    /*
    private void quitMenu() {
        System.exit(0);

    }

     */
}