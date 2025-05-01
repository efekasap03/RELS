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

        clientBtn.addActionListener(e -> {
            new BidManagementGUI("Client", bidService).setVisible(true);
            dispose();
        });

        landlordBtn.addActionListener(e -> {
            new BidManagementGUI("Landlord", bidService).setVisible(true);
            dispose();
        });

        setLayout(new GridLayout(2, 1));
        add(clientBtn);
        add(landlordBtn);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserOperations().setVisible(true));
    }
}
