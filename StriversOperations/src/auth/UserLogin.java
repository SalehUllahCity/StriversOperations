package auth;



/*
Duplicate of Login screen, but I think login screen is used in other code
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class UserLogin extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTextField textField;
    private int fontSize = 30;
    private JPasswordField passwordField;
    private JButton btnNewButton;
    private JLabel label;
    private JPanel contentPane;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UserLogin frame = new UserLogin();
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
    public UserLogin() {
        setTitle("Lancaster's Music Hall Software: Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setResizable(false);


        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        getContentPane().setBackground(new Color(18,32,35,255));

        // Load logo
        ImageIcon characterIcon = new ImageIcon(getClass().getResource("/data/logo/logo.png"));
        JLabel characterLabel = new JLabel(characterIcon);
        characterLabel.setBounds(200, 0, characterIcon.getIconWidth()*2, characterIcon.getIconHeight()*2);
        contentPane.add(characterLabel);

        /*
        JLabel lblNewLabel = new JLabel("Login");
        lblNewLabel.setForeground(Color.WHITE);
            lblNewLabel.setFont(new Font("Times New Roman", Font.BOLD, 25));
        lblNewLabel.setBounds(550, 13, 273, 93);
        contentPane.add(lblNewLabel);

         */

        textField = new JTextField();
        textField.setFont(new Font("TimesRoman", Font.PLAIN, 25));
        textField.setBounds(550, 400, 250, 50);
        contentPane.add(textField);
        textField.setColumns(10);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("TimesRoman", Font.BOLD, 25));
        passwordField.setBounds(550, 500, 250, 50);
        contentPane.add(passwordField);

        JLabel lblUsername = new JLabel("Username");
        lblUsername.setBackground(Color.WHITE);
        lblUsername.setForeground(Color.WHITE);
        lblUsername.setFont(new Font("TimesRoman", Font.BOLD, 25));
        lblUsername.setBounds(400, 400, 193, 52);
        contentPane.add(lblUsername);

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setForeground(Color.WHITE);
        lblPassword.setBackground(Color.CYAN);
        lblPassword.setFont(new Font("TimesRoman", Font.BOLD, 25));
        lblPassword.setBounds(400, 500, 193, 52);
        contentPane.add(lblPassword);

        btnNewButton = new JButton("Login");
        btnNewButton.setFont(new Font("TimesRoman", Font.BOLD, 25));
        btnNewButton.setBounds(575, 600, 160, 70);
        btnNewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String userName = textField.getText();
                String password = passwordField.getText();
                try {
                    Connection connection = (Connection) DriverManager.getConnection("jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t26",
                            "in2033t26_a", "jLxOPuQ69Mg");

                    PreparedStatement st = (PreparedStatement) connection
                            .prepareStatement("Select Email, Password from user where Email=? and Password=?");

                    st.setString(1, userName);
                    st.setString(2, password);
                    ResultSet rs = st.executeQuery();
                    if (rs.next()) {
                        dispose();

                     setVisible(false);
                        UserHome ah = new UserHome(); // pass username
                        ah.setTitle("Welcome");
                        ah.setVisible(true);
                        // JOptionPane.showMessageDialog(btnNewButton, "You have successfully logged in"); - the message pop-up is annoying
                    } else {
                        JOptionPane.showMessageDialog(btnNewButton, "Wrong Username & Password");
                    }
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }


            }
        });

        contentPane.add(btnNewButton);





        label = new JLabel("");
        label.setBounds(0, 0, 1008, 562);
        contentPane.add(label);

        addHoverEffect(btnNewButton);

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
                button.setForeground(new Color(0, 0, 0));
            }
        });
    }





    private void quitGame() {
        System.exit(0);

    }
}