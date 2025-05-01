package GUI;

import UserOperations.IBidManagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class LandlordBidGUI extends JFrame {
    private final IBidManagement bidManagement;

    public LandlordBidGUI(IBidManagement bidManagement) {
        this.bidManagement = bidManagement;
        initComponents();
    }

    private void initComponents() {
        setTitle("Landlord - Manage Bids");
        setSize(500, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTextField propertyIdField = new JTextField();
        JTextField bidIdField = new JTextField();
        JTextField newAmountField = new JTextField();
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);

        JButton listBtn = new JButton("List Bids by Property");
        JButton updateBtn = new JButton("Update Bid Amount");

        listBtn.addActionListener((ActionEvent e) -> {
            String propertyId = propertyIdField.getText().trim();
            List<String> bids = bidManagement.listBidsByProperty(propertyId);
            if (bids.isEmpty()) {
                resultArea.setText("No bids found for this property.");
            } else {
                resultArea.setText("Bids for property " + propertyId + ":\n\n" + String.join("\n", bids));
            }
        });

        updateBtn.addActionListener((ActionEvent e) -> {
            try {
                String bidId = bidIdField.getText().trim();
                double newAmount = Double.parseDouble(newAmountField.getText().trim());

                boolean updated = bidManagement.updateBid(bidId, newAmount);
                resultArea.setText(updated ? "Bid updated successfully." : "Failed to update bid.");
            } catch (NumberFormatException ex) {
                resultArea.setText("Amount must be a valid number.");
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.add(new JLabel("Property ID:"));
        inputPanel.add(propertyIdField);
        inputPanel.add(new JLabel("Bid ID to Update:"));
        inputPanel.add(bidIdField);
        inputPanel.add(new JLabel("New Amount:"));
        inputPanel.add(newAmountField);
        inputPanel.add(listBtn);
        inputPanel.add(updateBtn);

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
    }
}
