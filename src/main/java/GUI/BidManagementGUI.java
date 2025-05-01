package GUI;

import UserOperations.IBidManagement;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class BidManagementGUI extends JFrame {
    private final IBidManagement bidManagement;
    private final String userRole;

    public BidManagementGUI(String userRole, IBidManagement bidManagement) {
        this.userRole = userRole;
        this.bidManagement = bidManagement;
        initComponents();
    }

    private void initComponents() {
        setTitle("Bid Management - " + userRole);
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Ortak alanlar
        JTextField propertyField = new JTextField();
        panel.add(new JLabel("Property ID:"));
        panel.add(propertyField);

        if (userRole.equals("Client")) {
            JTextField amountField = new JTextField();
            JTextField bidIdField = new JTextField();
            panel.add(new JLabel("Amount:"));
            panel.add(amountField);

            JButton createBtn = new JButton("Create Bid");
            createBtn.addActionListener((ActionEvent e) -> {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    String bidId = bidManagement.createBid(propertyField.getText(), "client001", amount);
                    outputArea.setText("Bid created! ID: " + bidId);
                } catch (NumberFormatException ex) {
                    outputArea.setText("Invalid amount.");
                }
            });

            panel.add(new JLabel());
            panel.add(createBtn);

            panel.add(new JLabel("Check Status (Bid ID):"));
            panel.add(bidIdField);

            JButton checkBtn = new JButton("Check Status");
            checkBtn.addActionListener((ActionEvent e) -> {
                String status = bidManagement.getBidStatus(bidIdField.getText());
                outputArea.setText("Status: " + status);
            });

            panel.add(new JLabel());
            panel.add(checkBtn);
        }

        if (userRole.equals("Landlord")) {
            JTextField removeBidId = new JTextField();
            JButton listBtn = new JButton("List Bids");

            listBtn.addActionListener((ActionEvent e) -> {
                List<String> bids = bidManagement.listBidsByProperty(propertyField.getText());
                outputArea.setText(String.join("\n", bids));
            });

            panel.add(new JLabel("Bid ID to Remove:"));
            panel.add(removeBidId);

            JButton removeBtn = new JButton("Remove Bid");
            removeBtn.addActionListener((ActionEvent e) -> {
                boolean success = bidManagement.updateBid(removeBidId.getText(), -1);
                outputArea.setText(success ? "Bid removed." : "Failed to remove bid.");
            });

            panel.add(listBtn);
            panel.add(removeBtn);
        }

        getContentPane().add(panel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }
}
