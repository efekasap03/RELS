package Data.repository.impl;

import Data.connector.IDatabaseConnector;
import Data.domain.Filter;
import Data.domain.Property;
import Data.repository.interfaces.IPropertyRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Implementation of IPropertyRepository using JDBC.
 */
public class PropertyRepositoryImpl implements IPropertyRepository {

    private final IDatabaseConnector connector;

    // SQL Constants
    private static final String INSERT_PROPERTY_SQL =
            "INSERT INTO properties (property_id, landlord_id, address, city, postal_code, property_type, description, price, square_footage, bedrooms, bathrooms, is_active, date_listed, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

    private static final String SELECT_PROPERTY_BASE_SQL =
            "SELECT property_id, landlord_id, address, city, postal_code, property_type, description, price, square_footage, bedrooms, bathrooms, is_active, date_listed, created_at, updated_at " +
                    "FROM properties ";

    private static final String SELECT_PROPERTY_BY_ID_SQL = SELECT_PROPERTY_BASE_SQL + "WHERE property_id = ?";

    private static final String UPDATE_PROPERTY_SQL =
            "UPDATE properties SET landlord_id = ?, address = ?, city = ?, postal_code = ?, property_type = ?, description = ?, price = ?, square_footage = ?, bedrooms = ?, bathrooms = ?, is_active = ?, date_listed = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE property_id = ?";

    // Using logical delete (setting is_active to false) as per interface name 'deactivate'
    private static final String DEACTIVATE_PROPERTY_SQL = "UPDATE properties SET is_active = false, updated_at = CURRENT_TIMESTAMP WHERE property_id = ?";

    // Dynamic query base for findProperties
    private static final String FIND_PROPERTIES_BASE_SQL = SELECT_PROPERTY_BASE_SQL + "WHERE 1=1";


    public PropertyRepositoryImpl(IDatabaseConnector connector) {
        if (connector == null) {
            throw new IllegalArgumentException("Database connector cannot be null.");
        }
        this.connector = connector;
    }

    @Override
    public boolean addProperty(Property property) {
        if (property == null || property.getPropertyId() == null || property.getLandlordId() == null) {
            System.err.println("Error adding property: Property or required IDs are null.");
            return false;
        }

        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_PROPERTY_SQL)) {

            ps.setString(1, property.getPropertyId());
            ps.setString(2, property.getLandlordId());
            ps.setString(3, property.getAddress());
            ps.setString(4, property.getCity());
            ps.setString(5, property.getPostalCode());
            ps.setString(6, property.getPropertyType());
            ps.setString(7, property.getDescription());
            ps.setBigDecimal(8, property.getPrice());
            setNullableBigDecimal(ps, 9, property.getSquareFootage());
            setNullableInteger(ps, 10, property.getBedrooms());
            setNullableInteger(ps, 11, property.getBathrooms());
            ps.setBoolean(12, property.isActive());
            setNullableTimestamp(ps, 13, property.getDateListed());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Consider logging the exception properly
            System.err.println("Error adding property: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Property getPropertyById(String propertyId) {
        if (propertyId == null || propertyId.trim().isEmpty()) {
            System.err.println("Error getting property: propertyId is null or empty.");
            return null;
        }

        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_PROPERTY_BY_ID_SQL)) {

            ps.setString(1, propertyId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToProperty(rs);
                } else {
                    return null; // Not found
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting property by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateProperty(Property property) {
        if (property == null || property.getPropertyId() == null) {
            System.err.println("Error updating property: Property or propertyId is null.");
            return false;
        }

        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_PROPERTY_SQL)) {

            ps.setString(1, property.getLandlordId());
            ps.setString(2, property.getAddress());
            ps.setString(3, property.getCity());
            ps.setString(4, property.getPostalCode());
            ps.setString(5, property.getPropertyType());
            ps.setString(6, property.getDescription());
            ps.setBigDecimal(7, property.getPrice());
            setNullableBigDecimal(ps, 8, property.getSquareFootage());
            setNullableInteger(ps, 9, property.getBedrooms());
            setNullableInteger(ps, 10, property.getBathrooms());
            ps.setBoolean(11, property.isActive());
            setNullableTimestamp(ps, 12, property.getDateListed());
            ps.setString(13, property.getPropertyId()); // WHERE clause

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating property: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deactivateProperty(String propertyId) {
        if (propertyId == null || propertyId.trim().isEmpty()) {
            System.err.println("Error deactivating property: propertyId is null or empty.");
            return false;
        }
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(DEACTIVATE_PROPERTY_SQL)) {

            ps.setString(1, propertyId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deactivating property: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Property> findProperties(Filter filter) {
        List<Property> properties = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder(FIND_PROPERTIES_BASE_SQL);
        List<Object> params = new ArrayList<>();

        // Build dynamic WHERE clause based on filter
        if (filter != null) {
            if (filter.getLocation() != null && !filter.getLocation().trim().isEmpty()) {
                sqlBuilder.append(" AND (city LIKE ? OR postal_code LIKE ?)");
                String locationParam = "%" + filter.getLocation() + "%";
                params.add(locationParam);
                params.add(locationParam);
            }
            if (filter.getPropertyType() != null && !filter.getPropertyType().trim().isEmpty()) {
                sqlBuilder.append(" AND property_type = ?");
                params.add(filter.getPropertyType());
            }
            if (filter.getMinPrice() != null) {
                sqlBuilder.append(" AND price >= ?");
                params.add(filter.getMinPrice());
            }
            if (filter.getMaxPrice() != null) {
                sqlBuilder.append(" AND price <= ?");
                params.add(filter.getMaxPrice());
            }
            if (filter.getMinBedrooms() != null && filter.getMinBedrooms() > 0) { // Ignore if 0 or null
                sqlBuilder.append(" AND bedrooms >= ?");
                params.add(filter.getMinBedrooms());
            }
            if (filter.getMinBathrooms() != null && filter.getMinBathrooms() > 0) { // Ignore if 0 or null
                sqlBuilder.append(" AND bathrooms >= ?");
                params.add(filter.getMinBathrooms());
            }
            if (filter.getKeywords() != null && !filter.getKeywords().trim().isEmpty()) {
                sqlBuilder.append(" AND description LIKE ?");
                params.add("%" + filter.getKeywords() + "%");
            }
            if (filter.getMustBeActive() != null) {
                sqlBuilder.append(" AND is_active = ?");
                params.add(filter.getMustBeActive());
            }
        }

        sqlBuilder.append(" ORDER BY created_at DESC"); // Default ordering

        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {

            // Set parameters dynamically
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    properties.add(mapRowToProperty(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding properties: " + e.getMessage());
            e.printStackTrace();
            // Return empty list in case of error
        }
        return properties;
    }

    // --- Helper Methods ---

    /**
     * Maps a row from the ResultSet to a Property object.
     * @param rs The ResultSet containing property data.
     * @return A populated Property object.
     * @throws SQLException if a database access error occurs.
     */
    private Property mapRowToProperty(ResultSet rs) throws SQLException {
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
        property.setBedrooms(getNullableInteger(rs, "bedrooms"));
        property.setBathrooms(getNullableInteger(rs, "bathrooms"));
        property.setActive(rs.getBoolean("is_active"));
        property.setDateListed(getNullableTimestamp(rs, "date_listed"));
        property.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        property.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return property;
    }

    /** Helper to set nullable Integer in PreparedStatement */
    private void setNullableInteger(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value != null) {
            ps.setInt(index, value);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }

    /** Helper to set nullable BigDecimal in PreparedStatement */
    private void setNullableBigDecimal(PreparedStatement ps, int index, BigDecimal value) throws SQLException {
        if (value != null) {
            ps.setBigDecimal(index, value);
        } else {
            ps.setNull(index, Types.DECIMAL);
        }
    }

    /** Helper to set nullable Timestamp in PreparedStatement from LocalDateTime */
    private void setNullableTimestamp(PreparedStatement ps, int index, LocalDateTime value) throws SQLException {
        if (value != null) {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        } else {
            ps.setNull(index, Types.TIMESTAMP);
        }
    }

    /** Helper to get nullable Integer from ResultSet */
    private Integer getNullableInteger(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

     /** Helper to get nullable LocalDateTime from ResultSet */
    private LocalDateTime getNullableTimestamp(ResultSet rs, String columnName) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnName);
        return (ts != null) ? ts.toLocalDateTime() : null;
    }

} 