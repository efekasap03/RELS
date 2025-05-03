package GUI;

import UserOperations.AdminOperations;
import UserOperations.IAdminOperations;
import UserOperations.IPropertyManagement;
import UserOperations.IBidManagement;
import com.rels.domain.Landlord;
import com.rels.domain.Property;
import com.rels.domain.Bid;
import com.rels.connector.DatabaseConnectorImpl;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AdminOperationsGUI extends JFrame {
    private final IAdminOperations adminService;
    private final IPropertyManagement propertyService;

    public AdminOperationsGUI(IBidManagement bidService, IPropertyManagement propertyService) {
        String url = "jdbc:mysql://localhost:3306/relsdb";
        String user = "admin";
        String password = "adminpass";

        DatabaseConnectorImpl dbConnector = new DatabaseConnectorImpl(url, user, password);
        this.adminService = new AdminOperations(dbConnector);
        this.propertyService = propertyService;

        initComponents();
    }

    private void initComponents() {
        setTitle("Admin Operations");
        setSize(700, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Landlord Management Tab
        JPanel landlordPanel = createLandlordPanel();
        tabbedPane.addTab("Landlord Management", landlordPanel);

        // Property Monitoring Tab
        JPanel propertyPanel = createPropertyPanel();
        tabbedPane.addTab("Property Monitoring", propertyPanel);

        // Bid Monitoring Tab
        JPanel bidPanel = createBidPanel();
        tabbedPane.addTab("Bid Monitoring", bidPanel);

        // Reports Tab
        JPanel reportPanel = createReportPanel();
        tabbedPane.addTab("Reports", reportPanel);

        // Logout Button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            new UserOperations().setVisible(true);
            dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(logoutBtn);

        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createLandlordPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JButton addLandlordBtn = new JButton("Add Landlord");
        JButton editLandlordBtn = new JButton("Edit Landlord");
        JButton viewLandlordsBtn = new JButton("View All Landlords");

        addLandlordBtn.addActionListener(e -> showAddLandlordDialog(outputArea));
        editLandlordBtn.addActionListener(e -> showEditLandlordDialog(outputArea));
        viewLandlordsBtn.addActionListener(e -> {
            List<Landlord> landlords = adminService.getAllLandlords();
            outputArea.setText(formatLandlordsList(landlords));
        });

        buttonPanel.add(addLandlordBtn);
        buttonPanel.add(editLandlordBtn);
        buttonPanel.add(viewLandlordsBtn);

        panel.add(buttonPanel, BorderLayout.WEST);
        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPropertyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);

        JButton refreshBtn = new JButton("Refresh Properties");
        refreshBtn.addActionListener(e -> {
            List<Property> properties = adminService.monitorProperties();
            outputArea.setText(formatPropertiesList(properties));
        });

        panel.add(refreshBtn, BorderLayout.NORTH);
        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBidPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);

        JButton refreshBtn = new JButton("Refresh Bids");
        refreshBtn.addActionListener(e -> {
            List<Bid> bids = adminService.monitorBids();
            outputArea.setText(formatBidsList(bids));
        });

        panel.add(refreshBtn, BorderLayout.NORTH);
        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);

        JButton generateBtn = new JButton("Generate Report");
        generateBtn.addActionListener(e -> {
            String report = adminService.generateReports();
            outputArea.setText(report);
        });

        panel.add(generateBtn, BorderLayout.NORTH);
        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        return panel;
    }

    private void showAddLandlordDialog(JTextArea output) {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField licenseField = new JTextField();

        Object[] inputs = {
                "Landlord ID:", idField,
                "Name:", nameField,
                "Email:", emailField,
                "Password:", passwordField,
                "License Number:", licenseField
        };

        int result = JOptionPane.showConfirmDialog(
                this, inputs, "Add New Landlord", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Landlord landlord = new Landlord();
                landlord.setUserId(idField.getText().trim());
                landlord.setName(nameField.getText().trim());
                landlord.setEmail(emailField.getText().trim());
                landlord.setPasswordHash(new String(passwordField.getPassword()));
                landlord.setAgentLicenseNumber(licenseField.getText().trim());

                boolean success = adminService.addLandlord(landlord);
                if (success) {
                    output.setText("Landlord added successfully!");
                } else {
                    output.setText("Failed to add landlord.");
                }
            } catch (Exception e) {
                output.setText("Error: " + e.getMessage());
            }
        }
    }

    private void showEditLandlordDialog(JTextArea output) {
        List<Landlord> landlords = adminService.getAllLandlords();
        if (landlords.isEmpty()) {
            output.setText("No landlords available to edit.");
            return;
        }

        String[] landlordOptions = landlords.stream()
                .map(l -> l.getUserId() + " - " + l.getName())
                .toArray(String[]::new);

        String selectedLandlord = (String) JOptionPane.showInputDialog(
                this,
                "Select landlord to edit:",
                "Edit Landlord",
                JOptionPane.PLAIN_MESSAGE,
                null,
                landlordOptions,
                landlordOptions[0]);

        if (selectedLandlord == null) return;

        String landlordId = selectedLandlord.split(" - ")[0];
        Landlord landlord = landlords.stream()
                .filter(l -> l.getUserId().equals(landlordId))
                .findFirst()
                .orElse(null);

        if (landlord == null) {
            output.setText("Error: Landlord not found");
            return;
        }

        JTextField nameField = new JTextField(landlord.getName());
        JTextField emailField = new JTextField(landlord.getEmail());
        JPasswordField passwordField = new JPasswordField();
        JTextField licenseField = new JTextField(landlord.getAgentLicenseNumber());

        Object[] inputs = {
                "Name:", nameField,
                "Email:", emailField,
                "Password (leave blank to keep current):", passwordField,
                "License Number:", licenseField
        };

        int result = JOptionPane.showConfirmDialog(
                this, inputs, "Edit Landlord", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                landlord.setName(nameField.getText().trim());
                landlord.setEmail(emailField.getText().trim());
                String newPassword = new String(passwordField.getPassword());
                if (!newPassword.isEmpty()) {
                    landlord.setPasswordHash(newPassword);
                }
                landlord.setAgentLicenseNumber(licenseField.getText().trim());

                boolean success = adminService.editLandlord(landlord);
                if (success) {
                    output.setText("Landlord updated successfully!");
                } else {
                    output.setText("Failed to update landlord.");
                }
            } catch (Exception e) {
                output.setText("Error: " + e.getMessage());
            }
        }
    }

    private String formatLandlordsList(List<Landlord> landlords) {
        if (landlords.isEmpty()) return "No landlords found";

        StringBuilder sb = new StringBuilder("All Landlords:\n\n");
        for (Landlord l : landlords) {
            sb.append("ID: ").append(l.getUserId()).append("\n");
            sb.append("Name: ").append(l.getName()).append("\n");
            sb.append("Email: ").append(l.getEmail()).append("\n");
            sb.append("License: ").append(l.getAgentLicenseNumber()).append("\n\n");
        }
        return sb.toString();
    }

    private String formatPropertiesList(List<Property> properties) {
        if (properties.isEmpty()) return "No properties found";

        StringBuilder sb = new StringBuilder("All Properties:\n\n");
        for (Property p : properties) {
            sb.append("ID: ").append(p.getPropertyId()).append("\n");
            sb.append("Address: ").append(p.getAddress()).append(", ").append(p.getCity()).append("\n");
            sb.append("Type: ").append(p.getPropertyType()).append(" | Price: $").append(p.getPrice()).append("\n");
            sb.append("Status: ").append(p.isActive() ? "Active" : "Inactive").append("\n\n");
        }
        return sb.toString();
    }

    private String formatBidsList(List<Bid> bids) {
        if (bids.isEmpty()) return "No bids found";

        StringBuilder sb = new StringBuilder("All Bids:\n\n");
        for (Bid b : bids) {
            sb.append("ID: ").append(b.getBidId()).append("\n");
            sb.append("Property: ").append(b.getPropertyId()).append("\n");
            sb.append("Client: ").append(b.getClientId()).append("\n");
            sb.append("Amount: $").append(b.getAmount()).append("\n");
            sb.append("Status: ").append(b.getStatus()).append("\n");
            sb.append("Date: ").append(b.getBidTimestamp()).append("\n\n");
        }
        return sb.toString();
    }
}