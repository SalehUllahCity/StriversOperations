package auth;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SettingScreen extends JPanel  {

    private final JFrame parentFrame;
    private final Color backgroundColour = new Color(30, 40, 45);
    private final int fontSize = 18;

    public SettingScreen(JFrame parentFrame){
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(new Color(30, 40, 45));
        setPreferredSize(new Dimension(1200, 60));

        //Settings button
        JButton settingsBtn = createStyledButton("⚙ Settings");
        settingsBtn.addActionListener(e -> new SettingsDialog(parentFrame).setVisible(true));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(settingsBtn);

        add(rightPanel, BorderLayout.EAST);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("TimesRoman", Font.PLAIN, fontSize));
        button.setBackground(backgroundColour);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        addHoverEffect(button);
        return button;
    }

    private void addHoverEffect(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setForeground(Color.LIGHT_GRAY);
            }

            public void mouseExited(MouseEvent e) {
                button.setForeground(Color.WHITE);
            }
        });
    }

    /**
     * The settings popup dialog shown when the settings button is clicked.
     */
    private static class SettingsDialog extends JDialog {

        public SettingsDialog(JFrame parent) {
            super(parent, "Settings", true);
            setSize(400, 300);
            setLocationRelativeTo(parent);
            getContentPane().setBackground(new Color(25, 35, 40));
            setLayout(new GridLayout(4, 1, 10, 10));

            Font btnFont = new Font("TimesRoman", Font.PLAIN, 20);

            JButton logoutBtn = new JButton("Logout");
            logoutBtn.setFont(btnFont);
            logoutBtn.setBackground(Color.BLACK);
            logoutBtn.setForeground(Color.WHITE);
            logoutBtn.setFocusPainted(false);
            logoutBtn.addActionListener(e -> {
                dispose();
                parent.dispose();
                new LoginScreen().setVisible(true);
            });

            JButton accessibilityBtn = new JButton("Accessibility Settings");
            accessibilityBtn.setFont(btnFont);
            accessibilityBtn.setEnabled(false); // Placeholder

            add(accessibilityBtn);
            add(logoutBtn);
        }
    }
}
