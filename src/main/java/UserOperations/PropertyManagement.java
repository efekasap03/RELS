package UserOperations;

import Data.connector.DatabaseConnectorImpl;
import Data.domain.Property;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyManagement implements IPropertyManagement {
    private final DatabaseConnectorImpl dbConnector;

    public PropertyManagement(DatabaseConnectorImpl dbConnector) {
        this.dbConnector = dbConnector;
    }

    @Override
    public void addProperty(Property property) {
        String sql = "INSERT INTO properties (property_id, landlord_id, address, city, postal_code, " +
                "property_type, description, price, square_footage, bedrooms, bathrooms, is_active, is_sold) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, property.getPropertyId());
            pstmt.setString(2, property.getLandlordId());
            pstmt.setString(3, property.getAddress());
            pstmt.setString(4, property.getCity());
            pstmt.setString(5, property.getPostalCode());
            pstmt.setString(6, property.getPropertyType());
            pstmt.setString(7, property.getDescription());
            pstmt.setBigDecimal(8, property.getPrice());
            pstmt.setBigDecimal(9, property.getSquareFootage());
            pstmt.setInt(10, property.getBedrooms());
            pstmt.setInt(11, property.getBathrooms());
            pstmt.setBoolean(12, property.isActive());
            pstmt.setBoolean(13, property.isSold());  // Add this line

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add property", e);
        }
    }

    @Override
    public void editProperty(Property property, String landlordId) {
        String sql = "UPDATE properties SET " +
                "address = ?, city = ?, postal_code = ?, property_type = ?, " +
                "description = ?, price = ?, square_footage = ?, bedrooms = ?, " +
                "bathrooms = ?, is_active = ?, is_sold = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE property_id = ? AND landlord_id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, property.getAddress());
            pstmt.setString(2, property.getCity());
            pstmt.setString(3, property.getPostalCode());
            pstmt.setString(4, property.getPropertyType());
            pstmt.setString(5, property.getDescription());
            pstmt.setBigDecimal(6, property.getPrice());
            pstmt.setBigDecimal(7, property.getSquareFootage());
            pstmt.setInt(8, property.getBedrooms());
            pstmt.setInt(9, property.getBathrooms());
            pstmt.setBoolean(10, property.isActive());
            pstmt.setBoolean(11, property.isSold());  // Add this line
            pstmt.setString(12, property.getPropertyId());
            pstmt.setString(13, landlordId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Property not Edited");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update property", e);
        }
    }

    @Override
    public void deactivateProperty(String propertyId, String landlordID) {
        String sql = "UPDATE properties SET is_active = FALSE, updated_at = CURRENT_TIMESTAMP " +
                "WHERE property_id = ? AND landlord_id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, propertyId);
            pstmt.setString(2, landlordID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to deactivate property as it was not found or does not belong to you", e);
        }
    }

    @Override
    public void markPropertyAsSold(String propertyId, String landlordID) {
        String sql = "UPDATE properties SET is_sold = TRUE, is_active = FALSE, updated_at = CURRENT_TIMESTAMP " +
                "WHERE property_id = ? AND landlord_id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, propertyId);
            pstmt.setString(2, landlordID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark property as sold", e);
        }
    }

    @Override
    public List<Property> getProperties() {
        String sql = "SELECT * FROM properties WHERE is_active = TRUE AND is_sold = FALSE";
        List<Property> properties = new ArrayList<>();

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Property property = mapResultSetToProperty(rs);
                properties.add(property);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch properties", e);
        }
        return properties;
    }

    public List<Property> getActiveProperties() {
        String sql = "SELECT * FROM properties WHERE is_active = TRUE AND is_sold = FALSE";
        List<Property> properties = new ArrayList<>();

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (!conn.isValid(2)) { // Check if connection is valid
                throw new SQLException("Database connection is invalid");
            }

            while (rs.next()) {
                Property property = mapResultSetToProperty(rs);
                properties.add(property);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Vendor Error: " + e.getErrorCode());
            throw new RuntimeException("Failed to fetch active properties. Please check database connection.", e);
        }
        return properties;
    }

    @Override
    public List<Property> getPropertiesByLandlord(String landlordId) {
        String sql = "SELECT * FROM properties WHERE landlord_id = ?";
        List<Property> properties = new ArrayList<>();

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, landlordId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Property property = mapResultSetToProperty(rs);
                    properties.add(property);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch properties by landlord", e);
        }
        return properties;
    }

    private Property mapResultSetToProperty(ResultSet rs) throws SQLException {
        Property property = new Property();
        property.setPropertyId(rs.getString("property_id"));
        property.setLandlordId(rs.getString("landlord_id"));
        property.setAddress(rs.getString("address"));
        property.setCity(rs.getString("city"));
        property.setPostalCode(rs.getString("postal_code"));
        property.setPropertyType(rs.getString("property_type"));
        property.setDescription(rs.getString("description"));
        property.setPrice(rs.getBigDecimal("price"));
        property.setSquareFootage(rs.getBigDecimal("square_footage"));
        property.setBedrooms(rs.getInt("bedrooms"));
        property.setBathrooms(rs.getInt("bathrooms"));
        property.setActive(rs.getBoolean("is_active"));
        property.setSold(rs.getBoolean("is_sold"));  // Add this line
        property.setDateListed(rs.getTimestamp("date_listed"));
        return property;
    }

    @Override
    public List<Property> searchProperties(String type, Double minPrice, Double maxPrice, String location) {
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM properties WHERE is_active = TRUE AND is_sold = FALSE");

        List<Object> params = new ArrayList<>();

        if (type != null) {
            sql.append(" AND property_type = ?");
            params.add(type);
        }
        if (minPrice != null) {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }
        if (location != null && !location.isEmpty()) {
            sql.append(" AND (city LIKE ? OR address LIKE ?)");
            params.add("%" + location + "%");
            params.add("%" + location + "%");
        }

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                } else if (param instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) param);
                }
            }

            List<Property> properties = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    properties.add(mapResultSetToProperty(rs));
                }
            }
            return properties;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search properties", e);
        }
    }
}

