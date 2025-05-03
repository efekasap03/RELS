package UserOperations;

import com.rels.domain.Bid;
import com.rels.connector.DatabaseConnectorImpl;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BidManagement implements IBidManagement {
    private final DatabaseConnectorImpl dbConnector;

    public BidManagement(DatabaseConnectorImpl dbConnector) {
        this.dbConnector = dbConnector;
    }

    @Override
    public String createBid(String propertyId, String clientId, double amount) {
        String bidId = getNextBidId();  // Get the next sequential bid ID
        String sql = "INSERT INTO bids (bid_id, property_id, client_id, amount, status, bid_timestamp) " +
                "VALUES (?, ?, ?, ?, 'PENDING', ?)";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, bidId);
            pstmt.setString(2, propertyId);
            pstmt.setString(3, clientId);
            pstmt.setBigDecimal(4, BigDecimal.valueOf(amount));
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

            pstmt.executeUpdate();
            return bidId;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create bid", e);
        }
    }

    private String getNextBidId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(bid_id, 4) AS UNSIGNED)) FROM bids";

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int maxId = rs.getInt(1);  // Gets the highest numeric part
                return "bid" + (maxId + 1);
            }
            return "bid1";  // If no bids exist yet
        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate bid ID", e);
        }
    }
    @Override
    public boolean updateBid(String bidId, double newAmount) {
        String sql = "UPDATE bids SET amount = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE bid_id = ? AND status = 'PENDING'";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, BigDecimal.valueOf(newAmount));
            pstmt.setString(2, bidId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update bid", e);
        }
    }

    @Override
    public String getBidStatus(String bidId) {
        System.out.println("Checking status for bid: " + bidId); // Debug log

        String sql = "SELECT status FROM bids WHERE bid_id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, bidId.trim()); // Added trim() to ensure clean input
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    System.out.println("Found status: " + status); // Debug log
                    return status;
                } else {
                    System.out.println("No bid found with ID: " + bidId); // Debug log
                    return "NOT FOUND";
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking bid status: " + e.getMessage()); // Debug log
            throw new RuntimeException("Failed to get bid status", e);
        }
    }

    @Override
    public List<String> listBidsByProperty(String propertyId) {
        return listBids("property_id", propertyId);
    }

    public List<String> listBidsByClient(String clientId) {
        return listBids("client_id", clientId);
    }

    private List<String> listBids(String column, String value) {
        String sql = "SELECT b.*, p.address, u.name as client_name FROM bids b " +
                "JOIN properties p ON b.property_id = p.property_id " +
                "JOIN users u ON b.client_id = u.user_id " +
                "WHERE b." + column + " = ? ORDER BY b.bid_timestamp DESC";
        List<String> bids = new ArrayList<>();

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, value);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String bidInfo = String.format(
                            "ID: %s | Property: %s | Amount: $%,.2f | Status: %s | Date: %s",
                            rs.getString("bid_id"),
                            rs.getString("address"),
                            rs.getBigDecimal("amount"),
                            rs.getString("status"),
                            rs.getTimestamp("bid_timestamp")
                    );
                    bids.add(bidInfo);
                }
            }
            return bids;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list bids", e);
        }
    }

    public List<Bid> getBidsByLandlord(String landlordId) {
        String sql = "SELECT b.* FROM bids b " +
                "JOIN properties p ON b.property_id = p.property_id " +
                "WHERE p.landlord_id = ?";

        List<Bid> bids = new ArrayList<>();
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, landlordId);
            try (ResultSet rs = pstmt.executeQuery()) {
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
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch bids by landlord", e);
        }
        return bids;
    }


    public boolean updateBidStatus(String bidId, String newStatus, String landlordId) {
        String sql = "UPDATE bids b " +
                "JOIN properties p ON b.property_id = p.property_id " +
                "SET b.status = ?, b.updated_at = CURRENT_TIMESTAMP " +
                "WHERE b.bid_id = ? AND p.landlord_id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setString(2, bidId);
            pstmt.setString(3, landlordId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Bid not found or doesn't belong to your properties");
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update bid status", e);
        }
    }
}