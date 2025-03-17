package GUI;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class TitleScreen extends JFrame {

    private int fontSize = 30;

    public TitleScreen() {
        setTitle("Lancaster's Music Hall Software");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 1000);
        setLocationRelativeTo(null);

        getContentPane().setBackground(new Color(90, 85, 116));

        JLabel titleLabel = new JLabel("Lancaster's Music Hall");
        titleLabel.setFont(new Font("TimesRoman", Font.BOLD, 50));
        titleLabel.setForeground(new Color(255,255,255));

        JPanel titlePanel = new JPanel();
        // titlePanel.setBackground(Color.black);
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);

        JButton loginButton = createStyledButton("Login");
        JButton helpButton = createStyledButton("Help");
        JButton quitButton = createStyledButton("Quit");

        // Add buttons to the frame directly at the bottom
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        // buttonPanel.setBackground(Color.black);
        buttonPanel.setOpaque(false);
        buttonPanel.add(loginButton);
        buttonPanel.add(helpButton);
        buttonPanel.add(quitButton);

        // Load logo
        ImageIcon characterIcon = new ImageIcon("data/logo/logo.png");
        JLabel characterLabel = new JLabel(characterIcon);

        // Add components to the frame using GridBagLayout
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Add title panel to the top
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.2;
        getContentPane().add(titlePanel, gbc);

        // Add character label to the middle
        gbc.gridy = 1;
        gbc.weighty = 0.6;
        getContentPane().add(characterLabel, gbc);

        // Add button panel to the bottom
        gbc.gridy = 2;
        gbc.weighty = 0.2;
        getContentPane().add(buttonPanel, gbc);

        // Add action listeners
        /*
        loginButton.addActionListener(e -> {
            try {

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

         */

        /*

        helpButton.addActionListener(e -> {
            try {

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

         */
        quitButton.addActionListener(e -> quitGame());

        // Add mouse listeners to the buttons for hover effect
        addHoverEffect(loginButton);
        addHoverEffect(helpButton);
        addHoverEffect(quitButton);
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


    private void quitGame() {
        System.exit(0);

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TitleScreen titleScreen = new TitleScreen();
            titleScreen.setVisible(true);

        });
    }
}
