package GUI;

import javax.swing.*;
import java.awt.*;

public class AdminOperationsGUI extends JFrame {

    public AdminOperationsGUI() {
        setTitle("Admin Operations");
        setSize(400, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Admin panel is under development.", SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
    }
}
