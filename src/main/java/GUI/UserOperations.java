package GUI;

import UserOperations.BidManagement;
import UserOperations.IBidManagement;
import UserOperations.IPropertyManagement;
import UserOperations.PropertyManagement;

import com.rels.connector.DatabaseConnectorImpl;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserOperations extends JFrame {
    private final IBidManagement bidService;
<<<<<<< Updated upstream
    private final IPropertyManagement propertyManagement;
    private final DatabaseConnectorImpl connector;

    public UserOperations() {
        this.connector = new DatabaseConnectorImpl(
=======
    private final DatabaseConnectorImpl dbConnector;
    public UserOperations() {
        this.dbConnector = new DatabaseConnectorImpl(
>>>>>>> Stashed changes
                "jdbc:mysql://localhost:3306/relsdb",
                "root",
                "yourpassword");

<<<<<<< Updated upstream
        this.bidService = new BidManagement(connector);
        this.propertyManagement = new PropertyManagement(connector);
=======
        IBidRepository bidRepo = new BidRepositoryImpl(dbConnector);
        this.bidService = new BidManagement(bidRepo);
>>>>>>> Stashed changes

        setTitle("User Role Selection");
        setSize(350, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Please select user type to login", SwingConstants.CENTER);

        JButton clientBtn = new JButton("Client");
        JButton landlordBtn = new JButton("Landlord");
        JButton adminBtn = new JButton("Admin");

        clientBtn.addActionListener(e -> {
            new ClientBidGUI(bidService,propertyManagement).setVisible(true);
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
        String sql = "SELECT password_hash FROM users WHERE email = ? AND role = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set parameters (convert role to uppercase to match enum in database)
            pstmt.setString(1, username);
            pstmt.setString(2, role.toUpperCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // For testing: Just compare plain text passwords
                    String dbPassword = rs.getString("password_hash");
                    return dbPassword.equals(password);
                }
                return false; // No user found with these credentials
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new UserOperations().setVisible(true));
    }
}
