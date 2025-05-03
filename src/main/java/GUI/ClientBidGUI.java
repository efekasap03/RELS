package GUI;

import UserOperations.IBidManagement;
import UserOperations.IPropertyManagement;
import com.rels.domain.Property;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientBidGUI extends JFrame {
    private final IBidManagement bidService;
    private final IPropertyManagement propertyService;
    private final String clientId;

    private JTable propertiesTable;
    private DefaultTableModel propertiesTableModel;
    private JTable bidsTable;
    private DefaultTableModel bidsTableModel;
    private JComboBox<String> bidIdCombo;

    public ClientBidGUI(IBidManagement bidService, IPropertyManagement propertyService, String clientID) {
        this.bidService = bidService;
        this.propertyService = propertyService;
        this.clientId = clientID;

        setTitle("Client Bid Management");
        setSize(900, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeUI();
    }

    private void initializeUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Place New Bid
        JPanel viewPropertiesPanel = createPlaceBidTab();

        // Tab 2: Manage Bids
        JPanel manageBidsPanel = createManageBidsTab();

        tabbedPane.addTab("Place New Bid", viewPropertiesPanel);
        tabbedPane.addTab("Manage My Bids", manageBidsPanel);

        add(tabbedPane);
    }

    private JPanel createPlaceBidTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Refresh panel
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh Properties");
        refreshPanel.add(refreshBtn);

        // Properties table
        String[] propertyColumns = {"ID", "Type", "Price", "Location", "Bed", "Bath", "Size (sqft)"};
        propertiesTableModel = new DefaultTableModel(propertyColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        propertiesTable = new JTable(propertiesTableModel);
        JScrollPane scrollPane = new JScrollPane(propertiesTable);

        // Search panel
        JPanel searchPanel = createSearchPanel();

        // Bid form panel
        JPanel bidPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        bidPanel.setBorder(new TitledBorder("Place New Bid"));
        JTextField propertyIdField = new JTextField();
        JTextField bidAmountField = new JTextField();
        JButton placeBidBtn = new JButton("Place Bid");
        JButton backBtn = new JButton("Log Out");

        bidPanel.add(new JLabel("Property ID:"));
        bidPanel.add(propertyIdField);
        bidPanel.add(new JLabel("Bid Amount:"));
        bidPanel.add(bidAmountField);
        bidPanel.add(placeBidBtn);
        bidPanel.add(backBtn);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.add(searchPanel, BorderLayout.NORTH);
        bottomPanel.add(bidPanel, BorderLayout.CENTER);

        panel.add(refreshPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Event Handlers
        refreshBtn.addActionListener(e -> {
            List<Property> activeProperties = propertyService.getActiveProperties();
            updatePropertiesDisplay(activeProperties);
        });

        placeBidBtn.addActionListener(e -> {
            try {
                String propertyId = propertyIdField.getText().trim();
                double amount = Double.parseDouble(bidAmountField.getText().trim());
                String bidId = bidService.createBid(propertyId, clientId, amount);
                JOptionPane.showMessageDialog(this, "Bid created with ID: " + bidId);
                propertyIdField.setText("");
                bidAmountField.setText("");
                refreshBidsTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount format", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error creating bid: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backBtn.addActionListener(e -> {
            this.dispose();
            new UserOperations().setVisible(true);
        });

        return panel;
    }

    private JPanel createManageBidsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Refresh panel
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBidsBtn = new JButton("Refresh My Bids");
        refreshPanel.add(refreshBidsBtn);

        // Bids table
        String[] bidColumns = {"Bid Information"};
        bidsTableModel = new DefaultTableModel(bidColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bidsTable = new JTable(bidsTableModel);
        JScrollPane bidsScroll = new JScrollPane(bidsTable);

        // Bid management panel
        JPanel bidManagementPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        bidManagementPanel.setBorder(new TitledBorder("Manage Bids"));

        bidIdCombo = new JComboBox<>();
        JTextField newAmountField = new JTextField();
        JButton updateBtn = new JButton("Update Bid");
        JButton checkStatusBtn = new JButton("Check Status");

        bidManagementPanel.add(new JLabel("Bid ID:"));
        bidManagementPanel.add(bidIdCombo);
        bidManagementPanel.add(new JLabel("New Amount:"));
        bidManagementPanel.add(newAmountField);
        bidManagementPanel.add(updateBtn);
        bidManagementPanel.add(checkStatusBtn);

        panel.add(refreshPanel, BorderLayout.NORTH);
        panel.add(bidsScroll, BorderLayout.CENTER);
        panel.add(bidManagementPanel, BorderLayout.SOUTH);

        // Event Handlers
        refreshBidsBtn.addActionListener(e -> refreshBidsTable());

        updateBtn.addActionListener(e -> {
            try {
                String bidId = (String) bidIdCombo.getSelectedItem();
                if (bidId == null || bidId.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please select a bid", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double newAmount = Double.parseDouble(newAmountField.getText().trim());
                boolean updated = bidService.updateBid(bidId, newAmount);
                if (updated) {
                    JOptionPane.showMessageDialog(this, "Bid updated successfully");
                    newAmountField.setText("");
                    refreshBidsTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update bid - it may have been accepted/rejected already", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount format", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating bid: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        checkStatusBtn.addActionListener(e -> {
            String bidId = (String) bidIdCombo.getSelectedItem();
            if (bidId == null || bidId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a bid", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String status = bidService.getBidStatus(bidId);
                JOptionPane.showMessageDialog(this,
                        "Bid Status: " + status + "\nBid ID: " + bidId,
                        "Bid Status",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error checking bid status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        refreshBidsTable();

        return panel;
    }

    private void refreshBidsTable() {
        bidsTableModel.setRowCount(0);
        bidIdCombo.removeAllItems();

        List<String> bids = bidService.listBidsByClient(clientId);

        for (String bidInfo : bids) {
            // Add the full bid info to the table
            bidsTableModel.addRow(new Object[]{bidInfo});

            String bidId = extractBidIdFromInfo(bidInfo);
            if (bidId != null) {
                bidIdCombo.addItem(bidId);
            }
        }

        bidsTableModel.setColumnIdentifiers(new String[]{"Bid Information"});
    }

    private String extractBidIdFromInfo(String bidInfo) {
        try {
            String[] parts = bidInfo.split("\\|");
            if (parts.length > 0) {
                String idPart = parts[0].trim();
                return idPart.substring(idPart.indexOf(":") + 1).trim();
            }
        } catch (Exception e) {
            System.err.println("Error extracting bid ID from: " + bidInfo);
        }
        return null;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 10));

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Any", "House", "Apartment", "Condo"});
        JTextField minPriceField = new JTextField();
        JTextField maxPriceField = new JTextField();
        JTextField locationField = new JTextField();
        JButton searchBtn = new JButton("Search");

        panel.add(new JLabel("Property Type:"));
        panel.add(typeCombo);
        panel.add(new JLabel("Min Price:"));
        panel.add(minPriceField);
        panel.add(new JLabel("Max Price:"));
        panel.add(maxPriceField);
        panel.add(new JLabel("Location:"));
        panel.add(locationField);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.CENTER);
        container.add(searchBtn, BorderLayout.EAST);

        searchBtn.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            Double minPrice = parseDoubleOrNull(minPriceField.getText());
            Double maxPrice = parseDoubleOrNull(maxPriceField.getText());
            String location = locationField.getText().trim();

            List<Property> filtered = propertyService.searchProperties(
                    "Any".equals(type) ? null : type, minPrice, maxPrice, location);

            updatePropertiesDisplay(filtered);
        });

        return container;
    }

    private void updatePropertiesDisplay(List<Property> properties) {
        propertiesTableModel.setRowCount(0); // clear previous data
        for (Property p : properties) {
            propertiesTableModel.addRow(new Object[]{
                    p.getPropertyId().substring(0, Math.min(8, p.getPropertyId().length())),
                    p.getPropertyType(),
                    String.format("$%.2f", p.getPrice()),
                    p.getCity(),
                    p.getBedrooms(),
                    p.getBathrooms(),
                    p.getSquareFootage()
            });
        }
    }

    private Double parseDoubleOrNull(String text) {
        try {
            return text.isEmpty() ? null : Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}