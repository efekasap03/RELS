package GUI;

import UserOperations.IBidManagement;
import UserOperations.BidManagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class BidManagementGUI extends JFrame {
    private final IBidManagement bidManagement;

    public BidManagementGUI(IBidManagement bidManagement) {
        this.bidManagement = bidManagement;
        initComponents();
    }

    private void initComponents() {
        setTitle("Bid Management");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel propertyLabel = new JLabel("Property ID:");
        JTextField propertyField = new JTextField();

        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField();

        JButton createBidBtn = new JButton("Create Bid");

        createBidBtn.addActionListener((ActionEvent e) -> {
            String propertyId = propertyField.getText();
            double amount;
            try {
                amount = Double.parseDouble(amountField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount.");
                return;
            }

            String clientId = "client001"; // Replace with actual session data
            String bidId = bidManagement.createBid(propertyId, clientId, amount);
            JOptionPane.showMessageDialog(this, "Bid created! ID: " + bidId);
        });

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(propertyLabel);
        panel.add(propertyField);
        panel.add(amountLabel);
        panel.add(amountField);
        panel.add(new JLabel());
        panel.add(createBidBtn);

        add(panel);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        IBidManagement service = new BidManagement();
        SwingUtilities.invokeLater(() -> new BidManagementGUI(service).setVisible(true));
    }
}
