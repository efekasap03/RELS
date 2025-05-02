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
        String bidId = UUID.randomUUID().toString();
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
        String sql = "SELECT status FROM bids WHERE bid_id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, bidId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString("status") : "NOT FOUND";
            }
        } catch (SQLException e) {
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
}

//package UserOperations;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import com.rels.domain.Bid;
//import java.math.BigDecimal;
//import java.sql.*;
//import java.util.List;
//import java.util.UUID;
//import com.rels.connector.DatabaseConnectorImpl;
//
//public class BidManagement implements IBidManagement {
//    private final DatabaseConnectorImpl dbConnector;
//
//    public BidManagement(DatabaseConnectorImpl dbConnector) {
//        this.dbConnector = dbConnector;
//    }
//    @Override
//    public String createBid(String propertyId, String clientId, double amount) {
//        String bidId = UUID.randomUUID().toString();
//        String sql = "INSERT INTO bids (bid_id, property_id, client_id, amount, status, bid_timestamp) " +
//                "VALUES (?, ?, ?, ?, 'PENDING', ?)";
//
//        try (Connection conn = dbConnector.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setString(1, bidId);
//            pstmt.setString(2, propertyId);
//            pstmt.setString(3, clientId);
//            pstmt.setBigDecimal(4, BigDecimal.valueOf(amount));
//            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
//
//            pstmt.executeUpdate();
//            return bidId;
//        } catch (SQLException e) {
//            throw new RuntimeException("Failed to create bid", e);
//        }
//    }
//
//    @Override
//    public boolean updateBid(String bidId, double newAmount) {
//        String sql = "UPDATE bids SET amount = ?, updated_at = CURRENT_TIMESTAMP " +
//                "WHERE bid_id = ? AND status = 'PENDING'";
//
//        try (Connection conn = dbConnector.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setBigDecimal(1, BigDecimal.valueOf(newAmount));
//            pstmt.setString(2, bidId);
//
//            int rowsAffected = pstmt.executeUpdate();
//            return rowsAffected > 0;
//        } catch (SQLException e) {
//            throw new RuntimeException("Failed to update bid", e);
//        }
//    }
//
//
//@Override
//    public String getBidStatus(String bidId) {
//
//    String sql = "SELECT status FROM bids WHERE bid_id = ?";
//
//    try (Connection conn = dbConnector.getConnection();
//         PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//        pstmt.setString(1, bidId);
//        try (ResultSet rs = pstmt.executeQuery()) {
//            return rs.next() ? rs.getString("status") : null;
//        }
//    } catch (SQLException e) {
//        throw new RuntimeException("Failed to get bid status", e);
//    }
//}
//
//
//    @Override
//    public List<String> listBidsByProperty(String propertyId) {
//        String sql = "SELECT b.*, u.name as client_name FROM bids b " +
//                "JOIN users u ON b.client_id = u.user_id " +
//                "WHERE b.property_id = ? ORDER BY b.amount DESC";
//        List<String> results = new ArrayList<>();
//
//        try (Connection conn = dbConnector.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setString(1, propertyId);
//            try (ResultSet rs = pstmt.executeQuery()) {
//                while (rs.next()) {
//                    String bidInfo = String.format(
//                            "Bid ID: %s | Client: %s | Amount: $%,.2f | Status: %s | Date: %s",
//                            rs.getString("bid_id"),
//                            rs.getString("client_name"),
//                            rs.getBigDecimal("amount"),
//                            rs.getString("status"),
//                            rs.getTimestamp("bid_timestamp")
//                    );
//                    results.add(bidInfo);
//                }
//            }
//            return results;
//        } catch (SQLException e) {
//            throw new RuntimeException("Failed to list bids by property", e);
//        }
//    }
//    private Bid mapResultSetToBid(ResultSet rs) throws SQLException {
//        Bid bid = new Bid();
//        bid.setBidId(rs.getString("bid_id"));
//        bid.setPropertyId(rs.getString("property_id"));
//        bid.setClientId(rs.getString("client_id"));
//        bid.setAmount(rs.getBigDecimal("amount"));
//        bid.setStatus(rs.getString("status"));
//        bid.setBidTimestamp(rs.getTimestamp("bid_timestamp").toLocalDateTime());
//        bid.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
//        bid.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
//        return bid;
//    }
//
//
//}
