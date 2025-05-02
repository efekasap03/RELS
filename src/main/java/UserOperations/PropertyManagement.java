package UserOperations;

import com.rels.connector.DatabaseConnectorImpl;
import com.rels.domain.Property;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class PropertyManagement implements UserOperations.IPropertyManagement {
    private final DatabaseConnectorImpl dbConnector;

    public PropertyManagement(DatabaseConnectorImpl dbConnector) {
        this.dbConnector = dbConnector;
    }

    @Override
    public void addProperty(Property property) {
        String sql = "INSERT INTO properties (property_id, landlord_id, address, city, postal_code, " +
                "property_type, description, price, square_footage, bedrooms, bathrooms, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add property", e);
        }
    }

    @Override
    public void editProperty(Property property) {
        String sql = "UPDATE properties SET " +
                "address = ?, city = ?, postal_code = ?, property_type = ?, " +
                "description = ?, price = ?, square_footage = ?, bedrooms = ?, " +
                "bathrooms = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE property_id = ?";

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
            pstmt.setString(11, property.getPropertyId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update property", e);
        }
    }

    @Override
    public void deactivateProperty(String propertyId) {
        String sql = "UPDATE properties SET is_active = FALSE, updated_at = CURRENT_TIMESTAMP " +
                "WHERE property_id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, propertyId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to deactivate property", e);
        }
    }

    @Override
    public List<Property> getProperties() {
        String sql = "SELECT * FROM properties WHERE is_active = TRUE";
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
        property.setDateListed(rs.getTimestamp("date_listed"));
        return property;
    }
}

