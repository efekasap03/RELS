package GUI;

import UserOperations.BidManagement;
import UserOperations.IBidManagement;
import UserOperations.IPropertyManagement;
import UserOperations.PropertyManagement;
import Data.connector.DatabaseConnectorImpl;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class UserOperations extends JFrame {
    private final IBidManagement bidService;
    private final IPropertyManagement propertyManagement;
    private final DatabaseConnectorImpl connector;

    public UserOperations() {
        this.connector = new DatabaseConnectorImpl(
                "jdbc:mysql://localhost:3306/relsdb",
                "admin",
                "adminpass");

        this.bidService = new BidManagement(connector);
        this.propertyManagement = new PropertyManagement(connector);

        setTitle("User Role Selection");
        setSize(350, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Please select user type to login", SwingConstants.CENTER);

        JButton clientBtn = new JButton("Client");
        JButton landlordBtn = new JButton("Landlord");
        JButton adminBtn = new JButton("Admin");

        clientBtn.addActionListener(e -> showLoginDialog("CLIENT"));

        landlordBtn.addActionListener(e -> showLoginDialog("LANDLORD"));

        adminBtn.addActionListener(e -> showLoginDialog("ADMIN"));

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

            try {
                if (validateCredentials(role, username, password)) {
                    if (role.equals("ADMIN")) {
                        new AdminOperationsGUI(bidService, propertyManagement).setVisible(true);
                    } else if (role.equals("LANDLORD")) {
                        new LandlordBidGUI(bidService, propertyManagement, username).setVisible(true);
                    } else if (role.equals("CLIENT")) {
                        new ClientBidGUI(bidService, propertyManagement, username).setVisible(true);
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials or role mismatch.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validateCredentials(String role, String username, String password) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ? AND password_hash = ? AND role = ?";

        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserOperations().setVisible(true));
    }
}