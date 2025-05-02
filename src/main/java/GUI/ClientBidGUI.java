package GUI;
import UserOperations.IBidManagement;
import UserOperations.IPropertyManagement;
import com.rels.domain.Property;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ClientBidGUI extends JFrame {
    private final IBidManagement bidService;
    private final IPropertyManagement propertyService;
    private final String clientId;

    public ClientBidGUI(IBidManagement bidService, IPropertyManagement propertyService) {
        this.bidService = bidService;
        this.propertyService = propertyService;
        this.clientId = "client1"; // This should come from login - hardcoded for example

        setTitle("Client Bid Management");
        setSize(800, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeUI();
    }

    private void initializeUI() {
        // Main tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: View Properties and Place Bids
        JPanel viewPropertiesPanel = new JPanel(new BorderLayout(10, 10));
        viewPropertiesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Refresh button panel
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh Properties");
        refreshPanel.add(refreshBtn);

        // Properties display area
        JTextArea propertiesArea = new JTextArea();
        propertiesArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(propertiesArea);

        // Bid form panel
        JPanel bidPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        bidPanel.setBorder(BorderFactory.createTitledBorder("Place New Bid"));

        JTextField propertyIdField = new JTextField();
        JTextField bidAmountField = new JTextField();
        JButton placeBidBtn = new JButton("Place Bid");

        bidPanel.add(new JLabel("Property ID:"));
        bidPanel.add(propertyIdField);
        bidPanel.add(new JLabel("Bid Amount:"));
        bidPanel.add(bidAmountField);
        bidPanel.add(new JLabel()); // Empty cell for spacing
        bidPanel.add(placeBidBtn);

        // Add components to view properties panel
        viewPropertiesPanel.add(refreshPanel, BorderLayout.NORTH);
        viewPropertiesPanel.add(scrollPane, BorderLayout.CENTER);
        viewPropertiesPanel.add(bidPanel, BorderLayout.SOUTH);

        // Tab 2: Manage Existing Bids
        JPanel manageBidsPanel = new JPanel(new BorderLayout(10, 10));
        manageBidsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Refresh bids button panel
        JPanel refreshBidsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBidsBtn = new JButton("Refresh My Bids");
        refreshBidsPanel.add(refreshBidsBtn);

        // Bids display area
        JTextArea bidsArea = new JTextArea();
        bidsArea.setEditable(false);
        JScrollPane bidsScroll = new JScrollPane(bidsArea);

        // Bid management form panel
        JPanel bidManagementPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        bidManagementPanel.setBorder(BorderFactory.createTitledBorder("Manage Bids"));

        JTextField bidIdField = new JTextField();
        JTextField newAmountField = new JTextField();
        JButton updateBtn = new JButton("Update Bid");
        JButton checkStatusBtn = new JButton("Check Status");

        bidManagementPanel.add(new JLabel("Bid ID:"));
        bidManagementPanel.add(bidIdField);
        bidManagementPanel.add(new JLabel("New Amount:"));
        bidManagementPanel.add(newAmountField);
        bidManagementPanel.add(updateBtn);
        bidManagementPanel.add(checkStatusBtn);

        // Add components to manage bids panel
        manageBidsPanel.add(refreshBidsPanel, BorderLayout.NORTH);
        manageBidsPanel.add(bidsScroll, BorderLayout.CENTER);
        manageBidsPanel.add(bidManagementPanel, BorderLayout.SOUTH);

        // Add tabs to tabbed pane
        tabbedPane.addTab("Place New Bid", viewPropertiesPanel);
        tabbedPane.addTab("Manage My Bids", manageBidsPanel);

        // Add tabbed pane to frame
        add(tabbedPane);

        // Event handlers (EXACTLY the same as your second implementation)
        refreshBtn.addActionListener(e -> {
            List<Property> activeProperties = propertyService.getActiveProperties();
            StringBuilder sb = new StringBuilder("Active Properties:\n\n");
            activeProperties.forEach(p -> sb.append(p.toString()).append("\n\n"));
            propertiesArea.setText(sb.toString());
        });

        placeBidBtn.addActionListener(e -> {
            try {
                String propertyId = propertyIdField.getText().trim();
                double amount = Double.parseDouble(bidAmountField.getText().trim());
                String bidId = bidService.createBid(propertyId, clientId, amount);
                JOptionPane.showMessageDialog(this, "Bid created with ID: " + bidId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount format", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error creating bid: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        refreshBidsBtn.addActionListener(e -> {
            List<String> bids = bidService.listBidsByClient(clientId);
            bidsArea.setText(String.join("\n", bids));
        });

        updateBtn.addActionListener(e -> {
            try {
                String bidId = bidIdField.getText().trim();
                double newAmount = Double.parseDouble(newAmountField.getText().trim());
                boolean updated = bidService.updateBid(bidId, newAmount);
                if (updated) {
                    JOptionPane.showMessageDialog(this, "Bid updated successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update bid",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount format",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        checkStatusBtn.addActionListener(e -> {
            String bidId = bidIdField.getText().trim();
            String status = bidService.getBidStatus(bidId);
            JOptionPane.showMessageDialog(this, "Bid Status: " + status);
        });
    }
}