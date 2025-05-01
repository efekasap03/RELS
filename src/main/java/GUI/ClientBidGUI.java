package GUI;

import UserOperations.IBidManagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ClientBidGUI extends JFrame {
    private final IBidManagement bidManagement;

    public ClientBidGUI(IBidManagement bidManagement) {
        this.bidManagement = bidManagement;
        initComponents();
    }

    private void initComponents() {
        setTitle("Client - Create Bid");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTextField clientIdField = new JTextField();
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField propertyIdField = new JTextField();
        JTextField amountField = new JTextField();

        JButton submitBtn = new JButton("Create Bid");
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);

        submitBtn.addActionListener((ActionEvent e) -> {
            try {
                String clientId = clientIdField.getText().trim();
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                String propertyId = propertyIdField.getText().trim();
                double amount = Double.parseDouble(amountField.getText().trim());

                if (clientId.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || propertyId.isEmpty()) {
                    resultArea.setText("Please fill in all fields.");
                    return;
                }

                String bidId = bidManagement.createBid(propertyId, clientId, amount);
                resultArea.setText("Bid created!\nBid ID: " + bidId + "\nClient: " + firstName + " " + lastName);
            } catch (NumberFormatException ex) {
                resultArea.setText("Amount must be a valid number.");
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        inputPanel.add(new JLabel("Client ID:"));
        inputPanel.add(clientIdField);
        inputPanel.add(new JLabel("First Name:"));
        inputPanel.add(firstNameField);
        inputPanel.add(new JLabel("Last Name:"));
        inputPanel.add(lastNameField);
        inputPanel.add(new JLabel("Property ID:"));
        inputPanel.add(propertyIdField);
        inputPanel.add(new JLabel("Bid Amount:"));
        inputPanel.add(amountField);
        inputPanel.add(new JLabel());
        inputPanel.add(submitBtn);

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
    }
}
