package GUI;

import UserOperations.AdminOperations;
import UserOperations.IAdminOperations;
import com.rels.domain.Landlord;
import com.rels.domain.Property;
import com.rels.domain.Bid;
import com.rels.connector.DatabaseConnectorImpl;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AdminOperationsGUI extends JFrame {

    private final IAdminOperations adminService;

    public AdminOperationsGUI() {
        String url      = "jdbc:postgresql://localhost:5432/relsdb";
        String user     = "db_username";
        String password = "db_password";

        DatabaseConnectorImpl connector = new DatabaseConnectorImpl(url, user, password);
        this.adminService = new AdminOperations(connector);

        setTitle("Admin Operations");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initComponents();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10,10));

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        JButton addBtn    = new JButton("Add Landlord");
        JButton editBtn   = new JButton("Edit Landlord");
        JButton propsBtn  = new JButton("Monitor Properties");
        JButton bidsBtn   = new JButton("Monitor Bids");
        JButton reportBtn = new JButton("Generate Report");
        JButton backBtn   = new JButton("Back");

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(propsBtn);
        buttonPanel.add(bidsBtn);
        buttonPanel.add(reportBtn);

        JTextArea outputArea = new JTextArea(20, 40);
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(backBtn);

        add(buttonPanel, BorderLayout.WEST);
        add(outputScroll, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> onAddLandlord(outputArea));
        editBtn.addActionListener(e -> onEditLandlord(outputArea));
        propsBtn.addActionListener(e -> {
            List<Property> props = adminService.monitorProperties();
            StringBuilder sb = new StringBuilder("Properties:\n");
            props.forEach(p -> sb.append(" - ").append(p).append("\n"));
            outputArea.setText(sb.toString());
        });
        bidsBtn.addActionListener(e -> {
            List<Bid> bids = adminService.monitorBids();
            StringBuilder sb = new StringBuilder("Bids:\n");
            bids.forEach(b -> sb.append(" - ").append(b).append("\n"));
            outputArea.setText(sb.toString());
        });
        reportBtn.addActionListener(e -> {
            // eskiden Report POJO’ydu, şimdi String dönüyor:
            outputArea.setText(adminService.generateReports());
        });
        backBtn.addActionListener(e -> {
            dispose();
            new UserOperations().setVisible(true);
        });
    }

    private void onAddLandlord(JTextArea output) {
        JTextField idField      = new JTextField();
        JTextField nameField    = new JTextField();
        JTextField emailField   = new JTextField();
        JTextField pwdHashField = new JTextField();
        JTextField licenseField = new JTextField();
        Object[] inputs = {
                "Landlord ID:", idField,
                "Name:", nameField,
                "Email:", emailField,
                "Password Hash:", pwdHashField,
                "Agent License #:", licenseField
        };
        int ok = JOptionPane.showConfirmDialog(this, inputs, "Add Landlord", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            Landlord l = new Landlord();
            l.setUserId(idField.getText().trim());
            l.setName(nameField.getText().trim());
            l.setEmail(emailField.getText().trim());
            l.setPasswordHash(pwdHashField.getText().trim());
            l.setAgentLicenseNumber(licenseField.getText().trim());

            boolean success = adminService.addLandlord(l);
            output.setText(success
                    ? "Landlord added: " + l.getUserId()
                    : "Add failed");
        }
    }

    private void onEditLandlord(JTextArea output) {
        JTextField idField      = new JTextField();
        JTextField nameField    = new JTextField();
        JTextField emailField   = new JTextField();
        JTextField pwdHashField = new JTextField();
        JTextField licenseField = new JTextField();
        Object[] inputs = {
                "Landlord ID to edit:", idField,
                "New Name:", nameField,
                "New Email:", emailField,
                "New Password Hash:", pwdHashField,
                "New Agent License #:", licenseField
        };
        int ok = JOptionPane.showConfirmDialog(this, inputs, "Edit Landlord", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            Landlord l = new Landlord();
            l.setUserId(idField.getText().trim());
            l.setName(nameField.getText().trim());
            l.setEmail(emailField.getText().trim());
            l.setPasswordHash(pwdHashField.getText().trim());
            l.setAgentLicenseNumber(licenseField.getText().trim());

            boolean success = adminService.editLandlord(l);
            output.setText(success
                    ? "Landlord updated: " + l.getUserId()
                    : "Edit failed");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminOperationsGUI::new);
    }
}