package GUI;
import UserOperations.IPropertyManagement;
import UserOperations.IBidManagement;
import UserOperations.PropertyManagement;
import UserOperations.BidManagement;
import Data.domain.Property;
import Data.connector.DatabaseConnectorImpl;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PropertyManagementGUI extends JFrame {
    private final IPropertyManagement propertyService;
    private final IBidManagement bidservice;
    private final String landlordId;

    public PropertyManagementGUI(String landlordId) {
        String url = "jdbc:mysql://localhost:3306/relsdb";
        String user = "admin";
        String password = "adminpass";

        DatabaseConnectorImpl dbConnector = new DatabaseConnectorImpl(url, user, password);
        this.propertyService = new PropertyManagement(dbConnector);
        this.bidservice = new BidManagement(dbConnector);
        this.landlordId = landlordId;

        initComponents();
    }

    private void initComponents() {
        setTitle("Property Management");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);

        JButton addBtn = new JButton("Add Property");
        JButton editBtn = new JButton("Edit Property");
        JButton deactivateBtn = new JButton("Deactivate Property");
        JButton viewBtn = new JButton("View my Properties");

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deactivateBtn);
        buttonPanel.add(viewBtn);

        addBtn.addActionListener(e -> showAddPropertyDialog(outputArea));
        editBtn.addActionListener(e -> showEditPropertyDialog(outputArea));
        deactivateBtn.addActionListener(e -> showDeactivatePropertyDialog(outputArea));
        viewBtn.addActionListener(e -> {
            List<Property> properties = propertyService.getPropertiesByLandlord(landlordId);
            outputArea.setText(formatPropertiesList(properties));
        });

        mainPanel.add(buttonPanel, BorderLayout.WEST);
        mainPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        add(mainPanel);
    }

    private void showAddPropertyDialog(JTextArea output) {
        JTextField idField = new JTextField();
        JTextField landlordIdField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField postalCodeField = new JTextField();
        JTextField typeField = new JTextField();
        JTextArea descriptionArea = new JTextArea(3, 20);
        JTextField priceField = new JTextField();
        JTextField sqftField = new JTextField();
        JTextField bedroomsField = new JTextField();
        JTextField bathroomsField = new JTextField();

        Object[] inputs = {
                "Property ID:", idField,
                "Landlord ID:", landlordIdField,
                "Address:", addressField,
                "City:", cityField,
                "Postal Code:", postalCodeField,
                "Type:", typeField,
                "Description:", new JScrollPane(descriptionArea),
                "Price:", priceField,
                "Square Footage:", sqftField,
                "Bedrooms:", bedroomsField,
                "Bathrooms:", bathroomsField
        };

        int result = JOptionPane.showConfirmDialog(
                this, inputs, "Add New Property", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String priceText = priceField.getText().trim();
                String sqftText = sqftField.getText().trim();
                String bedroomsText = bedroomsField.getText().trim();
                String bathroomsText = bathroomsField.getText().trim();

                if (!priceText.matches("\\d+(\\.\\d+)?") ||
                        !sqftText.matches("\\d+(\\.\\d+)?") ||
                        !bedroomsText.matches("\\d+") ||
                        !bathroomsText.matches("\\d+")) {
                    output.setText("Error: Price and Square Footage must be valid decimal numbers. Bedrooms and Bathrooms must be whole numbers.");
                    return;
                }

                Property property = new Property();
                property.setPropertyId(idField.getText().trim());
                property.setLandlordId(landlordIdField.getText().trim());
                property.setAddress(addressField.getText().trim());
                property.setCity(cityField.getText().trim());
                property.setPostalCode(postalCodeField.getText().trim());
                property.setPropertyType(typeField.getText().trim());
                property.setDescription(descriptionArea.getText().trim());
                property.setPrice(new java.math.BigDecimal(priceText));
                property.setSquareFootage(new java.math.BigDecimal(sqftText));
                property.setBedrooms(Integer.parseInt(bedroomsText));
                property.setBathrooms(Integer.parseInt(bathroomsText));
                property.setActive(true);

                propertyService.addProperty(property);
                output.setText("Property added successfully!");
            } catch (NumberFormatException e) {
                output.setText("Error: Invalid number format.");
            } catch (Exception e) {
                output.setText("Error: " + e.getMessage());
            }
        }
    }

    private void showEditPropertyDialog(JTextArea output) {
            List<Property> properties = propertyService.getPropertiesByLandlord(landlordId);

            if (properties.isEmpty()) {
                output.setText("No properties available to edit.");
                return;
            }

            String[] propertyOptions = properties.stream()
                    .map(p -> p.getPropertyId() + " - " + p.getAddress() + ", " + p.getCity())
                    .toArray(String[]::new);

            String selectedProperty = (String) JOptionPane.showInputDialog(
                    this,
                    "Select property to edit:",
                    "Edit Property",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    propertyOptions,
                    propertyOptions[0]);

            if (selectedProperty == null) {
                return;
            }

            // Extract property ID from selection
            String propertyId = selectedProperty.split(" - ")[0];
            Property property = properties.stream()
                    .filter(p -> p.getPropertyId().equals(propertyId))
                    .findFirst()
                    .orElse(null);

            if (property == null) {
                output.setText("Error: Property not found");
                return;
            }

            // Create edit form with current values
            JTextField addressField = new JTextField(property.getAddress());
            JTextField cityField = new JTextField(property.getCity());
            JTextField postalCodeField = new JTextField(property.getPostalCode());
            JTextField typeField = new JTextField(property.getPropertyType());
            JTextArea descriptionArea = new JTextArea(property.getDescription(), 3, 20);
            JTextField priceField = new JTextField(property.getPrice().toString());
            JTextField sqftField = new JTextField(property.getSquareFootage().toString());
            JTextField bedroomsField = new JTextField(String.valueOf(property.getBedrooms()));
            JTextField bathroomsField = new JTextField(String.valueOf(property.getBathrooms()));
            JCheckBox activeCheckbox = new JCheckBox("Active", property.isActive());
            JCheckBox soldCheckbox = new JCheckBox("Sold", property.isSold());

        // Disable Active checkbox if property is sold
        soldCheckbox.addItemListener(e -> {
            if (soldCheckbox.isSelected()) {
                activeCheckbox.setSelected(false);
                activeCheckbox.setEnabled(false);
            } else {
                activeCheckbox.setEnabled(true);
            }
        });

        // Initialize based on current sold status
        if (property.isSold()) {
            activeCheckbox.setSelected(false);
            activeCheckbox.setEnabled(false);
        }

            Object[] inputs = {
                    "Address:", addressField,
                    "City:", cityField,
                    "Postal Code:", postalCodeField,
                    "Type:", typeField,
                    "Description:", new JScrollPane(descriptionArea),
                    "Price:", priceField,
                    "Square Footage:", sqftField,
                    "Bedrooms:", bedroomsField,
                    "Bathrooms:", bathroomsField,
                    "Status:", activeCheckbox,
                    "Sold:" , soldCheckbox
            };

            int result = JOptionPane.showConfirmDialog(
                    this, inputs, "Edit Property", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    // Update property object with new values
                    property.setAddress(addressField.getText().trim());
                    property.setCity(cityField.getText().trim());
                    property.setPostalCode(postalCodeField.getText().trim());
                    property.setPropertyType(typeField.getText().trim());
                    property.setDescription(descriptionArea.getText().trim());
                    property.setPrice(new java.math.BigDecimal(priceField.getText().trim()));
                    property.setSquareFootage(new java.math.BigDecimal(sqftField.getText().trim()));
                    property.setBedrooms(Integer.parseInt(bedroomsField.getText().trim()));
                    property.setBathrooms(Integer.parseInt(bathroomsField.getText().trim()));
                    property.setActive(activeCheckbox.isSelected());
                    property.setSold(soldCheckbox.isSelected());

                    // Save changes
                    propertyService.editProperty(property, landlordId);
                    output.setText("Property updated successfully!\n\nCurrent Details:\n" +
                            "Address: " + property.getAddress() + "\n" +
                            "Status: " + (property.isSold() ? "SOLD" :
                            (property.isActive() ? "ACTIVE" : "INACTIVE")) + "\n" +
                            "Last updated: " + new java.util.Date());
                } catch (NumberFormatException e) {
                    output.setText("Error: Invalid number format in one of the fields");
                } catch (Exception e) {
                    output.setText("Error: " + e.getMessage());
                }
            }
    }

    private void showDeactivatePropertyDialog(JTextArea output) {
        String propertyId = JOptionPane.showInputDialog(this, "Enter Property ID to deactivate:");
        if (propertyId != null && !propertyId.trim().isEmpty()) {
            try {
                propertyService.deactivateProperty(propertyId.trim(), landlordId);
                output.setText("Property deactivated: " + propertyId);
            } catch (Exception e) {
                output.setText("Error: " + e.getMessage());
            }
        }
    }



    private String formatPropertiesList(List<Property> properties) {
        if (properties == null || properties.isEmpty()) {
            return "No properties found";
        }

        StringBuilder sb = new StringBuilder("PROPERTIES:\n\n");
        for (Property p : properties) {
            sb.append("ID: ").append(p.getPropertyId()).append("\n");
            sb.append("Address: ").append(p.getAddress()).append(", ").append(p.getCity())
                    .append(" ").append(p.getPostalCode()).append("\n");
            sb.append("Type: ").append(p.getPropertyType()).append(" | ");
            sb.append("Price: $").append(p.getPrice()).append(" | ");
            sb.append("Size: ").append(p.getSquareFootage()).append(" sq.ft.\n");
            sb.append("Rooms: ").append(p.getBedrooms()).append(" bed | ");
            sb.append(p.getBathrooms()).append(" bath\n");

            // Updated status display
            String status;
            if (p.isSold()) {
                status = "SOLD";
            } else {
                status = p.isActive() ? "ACTIVE" : "INACTIVE";
            }
            sb.append("Status: ").append(status).append(" | ");
            sb.append("Listed: ").append(p.getDateListed()).append("\n");
            sb.append("Description: ").append(p.getDescription()).append("\n\n");
        }
        return sb.toString();
    }
}