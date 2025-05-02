package GUI;

import UserOperations.BidManagement;
import UserOperations.IBidManagement;
import UserOperations.AdminOperations;
import UserOperations.IAdminOperations;

import com.rels.connector.DatabaseConnectorImpl;
import com.rels.repository.interfaces.IBidRepository;
import com.rels.repository.impl.BidRepositoryImpl;

import javax.swing.*;
import java.awt.*;

public class UserOperations extends JFrame {
    private final IBidManagement bidService;

    public UserOperations() {
        DatabaseConnectorImpl connector = new DatabaseConnectorImpl(
                "jdbc:mysql://localhost:3306/rels_db",
                "root",
                "yourpassword"
        );

        IBidRepository bidRepo = new BidRepositoryImpl(connector);
        this.bidService = new BidManagement(bidRepo);

        setTitle("User Role Selection");
        setSize(350, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Please select user type to login", SwingConstants.CENTER);

        JButton clientBtn = new JButton("Client");
        JButton landlordBtn = new JButton("Landlord");
        JButton adminBtn = new JButton("Admin");

        clientBtn.addActionListener(e -> {
            new ClientBidGUI(bidService).setVisible(true);
            dispose();
        });

        landlordBtn.addActionListener(e -> showLoginDialog("Landlord"));

        adminBtn.addActionListener(e -> showLoginDialog("Admin"));

        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.add(clientBtn);
        buttonPanel.add(landlordBtn);
        buttonPanel.add(adminBtn);

        add(label, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }

    private void showLoginDialog(String role) {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        Object[] message = {
                "Username:", userField,
                "Password:", passField
        };

        int option = JOptionPane.showConfirmDialog(this, message,
                role + " Login", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (isValidCredentials(role, username, password)) {
                if (role.equals("Admin")) {
                    new AdminOperationsGUI().setVisible(true);
                } else if (role.equals("Landlord")) {
                    new LandlordBidGUI(bidService).setVisible(true);
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.");
            }
        }
    }

    private boolean isValidCredentials(String role, String username, String password) {
        if (role.equals("Admin"))
            return username.equals("admin") && password.equals("1234");
        if (role.equals("Landlord"))
            return username.equals("landlord") && password.equals("abcd");
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserOperations().setVisible(true));
    }
}
