package GUI;

import UserOperations.IBidManagement;
import UserOperations.IPropertyManagement;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import com.rels.domain.Property;
import java.util.List;

public class ClientBidGUI extends JFrame {
    private final IBidManagement bidManagement;
    private final IPropertyManagement propertyManagement;
    private JList<Property> propertyList;

    public ClientBidGUI(IBidManagement bidManagement, IPropertyManagement propertyManagement) {

        this.bidManagement = bidManagement;
        this.propertyManagement = propertyManagement;
        initComponents();
    }

    private void initComponents() {
        setTitle("Client - Create Bid");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        JPanel clientInfoPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        JTextField clientIdField = new JTextField();
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        clientInfoPanel.add(new JLabel("Client ID:"));
        clientInfoPanel.add(clientIdField);
        clientInfoPanel.add(new JLabel("First Name:"));
        clientInfoPanel.add(firstNameField);
        clientInfoPanel.add(new JLabel("Last Name:"));
        clientInfoPanel.add(lastNameField);

        JPanel propertyPanel = new JPanel(new BorderLayout());
        propertyPanel.setBorder(BorderFactory.createTitledBorder("Select Property"));


        List<Property> activeProperties = propertyManagement.getProperties().stream()
                .filter(Property::isActive)
                .toList();

        propertyList = new JList<>(activeProperties.toArray(new Property[0]));
        propertyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        propertyList.setCellRenderer(new PropertyListRenderer());

        propertyPanel.add(new JScrollPane(propertyList), BorderLayout.CENTER);

        JPanel propertyDetailsPanel = new JPanel(new BorderLayout());
        propertyDetailsPanel.setBorder(BorderFactory.createTitledBorder("Property Details"));
        JTextArea propertyDetailsArea = new JTextArea(6, 30);
        propertyDetailsArea.setEditable(false);
        propertyDetailsPanel.add(new JScrollPane(propertyDetailsArea), BorderLayout.CENTER);


        propertyList.addListSelectionListener(e -> {
            Property selected = propertyList.getSelectedValue();
            if (selected != null) {
                propertyDetailsArea.setText(formatPropertyDetails(selected));
            }
        });

        JPanel bidPanel = new JPanel(new BorderLayout());
        JTextField amountField = new JTextField();
        bidPanel.setBorder(BorderFactory.createTitledBorder("Bid Information"));
        bidPanel.add(new JLabel("Bid Amount:"), BorderLayout.WEST);
        bidPanel.add(amountField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton refreshBtn = new JButton("Refresh Properties");
        JButton submitBtn = new JButton("Create Bid");
        JButton backBtn = new JButton("Back to Menu");
        buttonPanel.add(submitBtn);
        buttonPanel.add(backBtn);
        buttonPanel.add(refreshBtn);

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);

        submitBtn.addActionListener((ActionEvent e) -> {
            try {
                String clientId = clientIdField.getText().trim();
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                Property selectedProperty = propertyList.getSelectedValue();
                String propertyId = (String)((JLabel)propertyList.getCellRenderer())
                        .getClientProperty("propertyId");
                double amount = Double.parseDouble(amountField.getText().trim());

                if (clientId.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                    resultArea.setText("Please fill in all client fields.");
                    return;
                }

                if (selectedProperty == null) {
                    resultArea.setText("Please select a property.");
                    return;
                }

                if (amount <= 0) {
                    resultArea.setText("Bid amount must be positive.");
                    return;
                }

                String bidId = bidManagement.createBid(selectedProperty.getPropertyId(), clientId, amount);
                resultArea.setText("Bid created!\nBid ID: " + bidId + "\nClient: " + firstName + " " + lastName);
            } catch (NumberFormatException ex) {
                resultArea.setText("Amount must be a valid number.");
            }
            catch (Exception ex) {
                resultArea.setText("Error: " + ex.getMessage());
            }
        });
        refreshBtn.addActionListener(e -> {
            List<Property> refreshedProperties = propertyManagement.getProperties().stream()
                    .filter(Property::isActive)
                    .toList();
            propertyList.setListData(refreshedProperties.toArray(new Property[0]));
            resultArea.setText("Property list refreshed.");
        });


        backBtn.addActionListener(e -> {
            this.dispose();
            new UserOperations().setVisible(true);
        });
        mainPanel.add(clientInfoPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(propertyPanel, BorderLayout.CENTER);
        centerPanel.add(propertyDetailsPanel, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(bidPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        southPanel.add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }
    private String formatPropertyDetails(Property property) {
        return "ID: " + property.getPropertyId() + "\n" +
                "Address: " + property.getAddress() + ", " + property.getCity() + "\n" +
                "Type: " + property.getPropertyType() + " | Price: $" + property.getPrice() + "\n" +
                "Bed/Bath: " + property.getBedrooms() + "/" + property.getBathrooms() + "\n" +
                "SqFt: " + property.getSquareFootage() + "\n" +
                "Description: " + property.getDescription();
    }

    // Custom renderer for Property objects in the JList
    private static class PropertyListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Property) {
                Property property = (Property) value;
                setText(property.getAddress() + " - " + property.getCity() + " ($" + property.getPrice() + ")");
            }
            return this;
        }
    }

}
