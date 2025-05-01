package GUI;

import UserOperations.BidManagement;
import UserOperations.IBidManagement;

import javax.swing.*;
import java.awt.*;

public class UserOperations extends JFrame {
    public UserOperations() {
        setTitle("Select User Role");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        IBidManagement bidService = new BidManagement();

        JButton clientBtn = new JButton("Client");
        JButton landlordBtn = new JButton("Landlord");
        JButton adminBtn = new JButton("Admin");

        clientBtn.addActionListener(e -> {
            new BidManagementGUI("Client", bidService).setVisible(true);
            dispose();
        });

        landlordBtn.addActionListener(e -> {
            new BidManagementGUI("Landlord", bidService).setVisible(true);
            dispose();
        });

        adminBtn.addActionListener(e -> {
            new BidManagementGUI("Admin", bidService).setVisible(true);
            dispose();
        });

        setLayout(new GridLayout(3, 1, 10, 10));
        add(clientBtn);
        add(landlordBtn);
        add(adminBtn);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserOperations().setVisible(true));
    }
}
