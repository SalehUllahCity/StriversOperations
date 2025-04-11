package auth;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A panel component that provides access to application settings.
 * Contains a settings button that opens a dialog with various options
 * including logout and accessibility settings.
 */
public class SettingScreen extends JPanel  {

    /** The parent frame that contains this settings panel */
    private final JFrame parentFrame;
    /** The font size used for buttons in this panel */
    private final int fontSize = 18;

    /**
     * Constructs a new SettingScreen panel.
     * @param parentFrame The parent frame that contains this settings panel
     */
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

    /**
     * Creates a styled button with consistent appearance.
     * @param text The text to display on the button
     * @return A JButton with the specified text and styling
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("TimesRoman", Font.PLAIN, fontSize));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        addHoverEffect(button);
        return button;
    }

    /**
     * Adds hover effects to a button.
     * Changes the button's foreground color when the mouse enters/exits.
     * @param button The button to add hover effects to
     */
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
     * A dialog window that displays application settings options.
     * Provides access to logout, accessibility settings, and dialog closing.
     */
    protected static class SettingsDialog extends JDialog {

        /**
         * Constructs a new SettingsDialog.
         * @param parent The parent frame for this dialog
         */
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

        /**
         * Creates a styled button for the settings dialog.
         * @param text The text to display on the button
         * @param font The font to use for the button text
         * @param bgColor The background color for the button
         * @return A JButton with the specified styling
         */
        private JButton createDialogButton(String text, Font font, Color bgColor) {
            JButton button = new JButton(text);
            button.setFont(font);
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            addHoverEffect(button);
            return button;
        }

        /**
         * Adds hover effects to a button in the settings dialog.
         * Changes the button's border color when the mouse enters/exits.
         * @param button The button to add hover effects to
         */
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
}
