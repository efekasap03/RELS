package Data.repository.impl;

import Data.connector.IDatabaseConnector;
import Data.domain.Bid;
import Data.repository.interfaces.IBidRepository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BidRepositoryImpl implements IBidRepository {

    private final IDatabaseConnector connector;

    // SQL Constants
    private static final String INSERT_BID_SQL =
            "INSERT INTO bids (bid_id, property_id, client_id, amount, status, bid_timestamp, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

    private static final String SELECT_BID_BASE_SQL =
            "SELECT bid_id, property_id, client_id, amount, status, bid_timestamp, created_at, updated_at " +
            "FROM bids ";

    private static final String SELECT_BID_BY_ID_SQL = SELECT_BID_BASE_SQL + "WHERE bid_id = ?";
    private static final String SELECT_BIDS_BY_PROPERTY_ID_SQL = SELECT_BID_BASE_SQL + "WHERE property_id = ? ORDER BY bid_timestamp DESC";
    private static final String SELECT_BIDS_BY_CLIENT_ID_SQL = SELECT_BID_BASE_SQL + "WHERE client_id = ? ORDER BY bid_timestamp DESC";

    private static final String UPDATE_BID_SQL =
            "UPDATE bids SET property_id = ?, client_id = ?, amount = ?, status = ?, bid_timestamp = ?, updated_at = CURRENT_TIMESTAMP " +
            "WHERE bid_id = ?";

    public BidRepositoryImpl(IDatabaseConnector connector) {
        if (connector == null) {
            throw new IllegalArgumentException("Database connector cannot be null.");
        }
        this.connector = connector;
    }

    @Override
    public boolean addBid(Bid bid) {
        if (bid == null || bid.getBidId() == null || bid.getPropertyId() == null || bid.getClientId() == null || bid.getStatus() == null) {
            System.err.println("Error adding bid: Bid or required IDs/status are null.");
            return false;
        }

        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_BID_SQL)) {

            ps.setString(1, bid.getBidId());
            ps.setString(2, bid.getPropertyId());
            ps.setString(3, bid.getClientId());
            ps.setBigDecimal(4, bid.getAmount());
            ps.setString(5, bid.getStatus()); // ENUM mapped to String
            setNullableTimestamp(ps, 6, bid.getBidTimestamp());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding bid: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Bid getBidById(String bidId) {
        if (bidId == null || bidId.trim().isEmpty()) {
            System.err.println("Error getting bid: bidId is null or empty.");
            return null;
        }
        return getSingleBidBy(SELECT_BID_BY_ID_SQL, bidId);
    }

    @Override
    public boolean updateBid(Bid bid) {
        if (bid == null || bid.getBidId() == null) {
            System.err.println("Error updating bid: Bid or bidId is null.");
            return false;
        }

        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_BID_SQL)) {

            ps.setString(1, bid.getPropertyId());
            ps.setString(2, bid.getClientId());
            ps.setBigDecimal(3, bid.getAmount());
            ps.setString(4, bid.getStatus());
            setNullableTimestamp(ps, 5, bid.getBidTimestamp());
            ps.setString(6, bid.getBidId()); // WHERE clause

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating bid: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Bid> getBidsByPropertyId(String propertyId) {
        if (propertyId == null || propertyId.trim().isEmpty()) {
            System.err.println("Error getting bids: propertyId is null or empty.");
            return new ArrayList<>(); // Return empty list
        }
        return getBidListBy(SELECT_BIDS_BY_PROPERTY_ID_SQL, propertyId);
    }

    @Override
    public List<Bid> getBidsByUserId(String userId) {
         if (userId == null || userId.trim().isEmpty()) {
            System.err.println("Error getting bids: userId is null or empty.");
            return new ArrayList<>(); // Return empty list
        }
        return getBidListBy(SELECT_BIDS_BY_CLIENT_ID_SQL, userId);
    }

    // --- Helper Methods ---

    private Bid getSingleBidBy(String sql, String parameter) {
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, parameter);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBid(rs);
                } else {
                    return null; // Not found
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing single bid query: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private List<Bid> getBidListBy(String sql, String parameter) {
        List<Bid> bids = new ArrayList<>();
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, parameter);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bids.add(mapRowToBid(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing bid list query: " + e.getMessage());
            e.printStackTrace();
            // Return potentially partially filled list or empty list on error
        }
        return bids;
    }

    /**
     * Maps a row from the ResultSet to a Bid object.
     */
    private Bid mapRowToBid(ResultSet rs) throws SQLException {
        Bid bid = new Bid();
        bid.setBidId(rs.getString("bid_id"));
        bid.setPropertyId(rs.getString("property_id"));
        bid.setClientId(rs.getString("client_id"));
        bid.setAmount(rs.getBigDecimal("amount"));
        bid.setStatus(rs.getString("status")); // ENUM read as String
        bid.setBidTimestamp(getNullableTimestamp(rs, "bid_timestamp"));
        bid.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        bid.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return bid;
    }

    /** Helper to set nullable Timestamp in PreparedStatement from LocalDateTime */
    private void setNullableTimestamp(PreparedStatement ps, int index, LocalDateTime value) throws SQLException {
        if (value != null) {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        } else {
            ps.setNull(index, Types.TIMESTAMP);
        }
    }

     /** Helper to get nullable LocalDateTime from ResultSet */
    private LocalDateTime getNullableTimestamp(ResultSet rs, String columnName) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnName);
        return (ts != null) ? ts.toLocalDateTime() : null;
    }
}
