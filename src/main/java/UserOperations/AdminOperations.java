package UserOperations;

import Data.domain.Landlord;
import Data.domain.Property;
import Data.domain.Bid;
import Data.connector.DatabaseConnectorImpl;

import java.sql.*;
        import java.util.ArrayList;
import java.util.List;

public class AdminOperations implements IAdminOperations {
    private final Connection conn;

    public AdminOperations(DatabaseConnectorImpl connector) {
        try {
            this.conn = connector.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    @Override
    public boolean addLandlord(Landlord landlord) {
        String sql = "INSERT INTO users(user_id, name, email, password_hash, role, agent_license_number) " +
                "VALUES (?, ?, ?, ?, 'LANDLORD', ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, landlord.getUserId());
            pstmt.setString(2, landlord.getName());
            pstmt.setString(3, landlord.getEmail());
            pstmt.setString(4, landlord.getPasswordHash());
            pstmt.setString(5, landlord.getAgentLicenseNumber());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add landlord", e);
        }
    }

    @Override
    public boolean editLandlord(Landlord landlord) {
        String sql = "UPDATE users SET name = ?, email = ?, password_hash = ?, agent_license_number = ? " +
                "WHERE user_id = ? AND role = 'LANDLORD'";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, landlord.getName());
            pstmt.setString(2, landlord.getEmail());
            pstmt.setString(3, landlord.getPasswordHash());
            pstmt.setString(4, landlord.getAgentLicenseNumber());
            pstmt.setString(5, landlord.getUserId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to edit landlord", e);
        }
    }

    @Override
    public List<Property> monitorProperties() {
        String sql = "SELECT * FROM properties";
        List<Property> properties = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
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
                properties.add(property);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch properties", e);
        }
        return properties;
    }

    @Override
    public List<Bid> monitorBids() {
        String sql = "SELECT * FROM bids";
        List<Bid> bids = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Bid bid = new Bid();
                bid.setBidId(rs.getString("bid_id"));
                bid.setPropertyId(rs.getString("property_id"));
                bid.setClientId(rs.getString("client_id"));
                bid.setAmount(rs.getBigDecimal("amount"));
                bid.setStatus(rs.getString("status"));
                bid.setBidTimestamp(rs.getTimestamp("bid_timestamp").toLocalDateTime());
                bids.add(bid);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch bids", e);
        }
        return bids;
    }

    @Override
    public String generateReports() {
        StringBuilder report = new StringBuilder();

        // Count landlords
        int landlordCount = countRecords("SELECT COUNT(*) FROM users WHERE role = 'LANDLORD'");
        report.append("Total Landlords: ").append(landlordCount).append("\n");

        // Count properties
        int activeProperties = countRecords("SELECT COUNT(*) FROM properties WHERE is_active = TRUE");
        int inactiveProperties = countRecords("SELECT COUNT(*) FROM properties WHERE is_active = FALSE");
        report.append("Active Properties: ").append(activeProperties).append("\n");
        report.append("Inactive Properties: ").append(inactiveProperties).append("\n");

        // Count bids
        int pendingBids = countRecords("SELECT COUNT(*) FROM bids WHERE status = 'PENDING'");
        int acceptedBids = countRecords("SELECT COUNT(*) FROM bids WHERE status = 'ACCEPTED'");
        int rejectedBids = countRecords("SELECT COUNT(*) FROM bids WHERE status = 'REJECTED'");
        report.append("Pending Bids: ").append(pendingBids).append("\n");
        report.append("Accepted Bids: ").append(acceptedBids).append("\n");
        report.append("Rejected Bids: ").append(rejectedBids).append("\n");

        return report.toString();
    }

    public List<Landlord> getAllLandlords() {
        String sql = "SELECT * FROM users WHERE role = 'LANDLORD'";
        List<Landlord> landlords = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Landlord landlord = new Landlord();
                landlord.setUserId(rs.getString("user_id"));
                landlord.setName(rs.getString("name"));
                landlord.setEmail(rs.getString("email"));
                landlord.setPasswordHash(rs.getString("password_hash"));
                landlord.setAgentLicenseNumber(rs.getString("agent_license_number"));
                landlords.add(landlord);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch landlords", e);
        }
        return landlords;
    }

    private int countRecords(String sql) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count records", e);
        }
    }

}