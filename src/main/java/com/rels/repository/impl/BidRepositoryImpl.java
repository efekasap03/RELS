package com.rels.repository.impl;

import com.rels.domain.Bid;
import com.rels.repository.interfaces.IBidRepository;
import com.rels.connector.DatabaseConnectorImpl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BidRepositoryImpl implements IBidRepository {

    private final DatabaseConnectorImpl db;

    public BidRepositoryImpl(DatabaseConnectorImpl db) {
        this.db = db;
    }

    @Override
    public void save(Bid bid) {
        String sql = "INSERT INTO bids (bid_id, property_id, client_id, amount, status, bid_timestamp, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bid.getBidId());
            stmt.setString(2, bid.getPropertyId());
            stmt.setString(3, bid.getClientId());
            stmt.setBigDecimal(4, bid.getAmount());
            stmt.setString(5, bid.getStatus());
            stmt.setTimestamp(6, Timestamp.valueOf(bid.getBidTimestamp()));
            stmt.setTimestamp(7, Timestamp.valueOf(bid.getCreatedAt()));
            stmt.setTimestamp(8, Timestamp.valueOf(bid.getUpdatedAt()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // ideally log
        }
    }

    @Override
    public boolean update(Bid bid) {
        String sql = "UPDATE bids SET amount = ?, updated_at = ? WHERE bid_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, bid.getAmount());
            stmt.setTimestamp(2, Timestamp.valueOf(bid.getUpdatedAt()));
            stmt.setString(3, bid.getBidId());
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Bid findById(String bidId) {
        String sql = "SELECT * FROM bids WHERE bid_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bidId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return buildBid(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Bid> findByPropertyId(String propertyId) {
        List<Bid> bids = new ArrayList<>();
        String sql = "SELECT * FROM bids WHERE property_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, propertyId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bids.add(buildBid(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bids;
    }

    private Bid buildBid(ResultSet rs) throws SQLException {
        Bid bid = new Bid();
        bid.setBidId(rs.getString("bid_id"));
        bid.setPropertyId(rs.getString("property_id"));
        bid.setClientId(rs.getString("client_id"));
        bid.setAmount(rs.getBigDecimal("amount"));
        bid.setStatus(rs.getString("status"));
        bid.setBidTimestamp(rs.getTimestamp("bid_timestamp").toLocalDateTime());
        bid.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        bid.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return bid;
    }
}
