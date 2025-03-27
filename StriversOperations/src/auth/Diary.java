package auth;

import javax.swing.*;
import java.awt.*;

// Bookings that are unconfirmed/not fully paid for within the 28 days time slot we provide
public class Diary extends JFrame {


    private final Color background = new Color(18, 32, 35, 255);
    private final int fontSize = 22;

    public Diary() {
        setTitle("Lancaster's Music Hall Software");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setResizable(false);
        setLocationRelativeTo(null);

        //Main content pane
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(background);
        setContentPane(contentPane);


    }
}
