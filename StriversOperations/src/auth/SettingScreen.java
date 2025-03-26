package auth;

import javax.swing.*;
import java.awt.*;
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
        setPreferredSize(new Dimension(1200, 40));

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

    /**
     * The settings popup dialog shown when the settings button is clicked.
     */
    private static class SettingsDialog extends JDialog {

        public SettingsDialog(JFrame parent) {
            super(parent, "Settings", true);
            setSize(400, 250);
            setLocationRelativeTo(parent);
            getContentPane().setBackground(new Color(30, 40, 45));
            setLayout(new GridLayout(3, 1, 20, 20));
            ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

            //Remove the "X" close button
            setUndecorated(true);
            getRootPane().setWindowDecorationStyle(JRootPane.NONE);

            Font btnFont = new Font("TimesRoman", Font.PLAIN, 20);
            Color buttonColor = new Color(45, 55, 60);

            JButton logoutBtn = createDialogButton("Logout", btnFont, buttonColor);
            logoutBtn.addActionListener(e -> {
                dispose();
                parent.dispose();
                new UserLogin().setVisible(true);
            });

            JButton accessibilityBtn = createDialogButton("Accessibility Settings", btnFont, buttonColor);
            accessibilityBtn.setEnabled(false); //Placeholder

            JButton closeBtn = createDialogButton("Close", btnFont, buttonColor);
            closeBtn.addActionListener(e -> dispose());

            add(accessibilityBtn);
            add(logoutBtn);
            add(closeBtn);
        }

        private JButton createDialogButton(String text, Font font, Color bgColor) {
            JButton button = new JButton(text);
            button.setFont(font);
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                }
            });
            return button;
        }
    }
}
