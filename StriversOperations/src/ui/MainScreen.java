package ui;


import javax.swing.*;
import java.awt.*;

public class MainScreen extends JFrame {
    public MainScreen() {
        setTitle("Lancaster Music Hall");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // setJMenuBar(new MenuBar());
        add(new JLabel("Welcome to Lancaster Music Hall", SwingConstants.CENTER), BorderLayout.CENTER);

        setVisible(true);
    }
}
