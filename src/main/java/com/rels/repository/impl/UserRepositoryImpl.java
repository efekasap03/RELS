package com.rels.repository.impl;

import com.rels.connector.IDatabaseConnector;
import com.rels.domain.Client;
import com.rels.domain.Landlord;
import com.rels.domain.User;
import com.rels.repository.interfaces.IUserRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of IUserRepository using JDBC.
 * Handles Single Table Inheritance for User, Landlord, and Client.
 */
public class UserRepositoryImpl implements IUserRepository {

    private final IDatabaseConnector connector;

    // SQL Constants
    private static final String INSERT_USER_SQL =
            "INSERT INTO users (user_id, name, email, password_hash, phone_number, role, is_verified, agent_license_number, receives_market_updates, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
    private static final String SELECT_USER_BASE_SQL =
            "SELECT user_id, name, email, password_hash, phone_number, role, is_verified, created_at, updated_at, agent_license_number, receives_market_updates " +
                    "FROM users ";
    private static final String SELECT_USER_BY_ID_SQL    = SELECT_USER_BASE_SQL + "WHERE user_id = ?";
    private static final String SELECT_USER_BY_EMAIL_SQL = SELECT_USER_BASE_SQL + "WHERE email = ?";
    private static final String SELECT_LANDLORDS_SQL     = SELECT_USER_BASE_SQL + "WHERE role = 'LANDLORD'";
    private static final String SELECT_CLIENTS_SQL       = SELECT_USER_BASE_SQL + "WHERE role = 'CLIENT'";
    private static final String UPDATE_USER_SQL =
            "UPDATE users SET name = ?, email = ?, password_hash = ?, phone_number = ?, role = ?, is_verified = ?, " +
                    "agent_license_number = ?, receives_market_updates = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
    private static final String DELETE_USER_SQL = "DELETE FROM users WHERE user_id = ?";

    public UserRepositoryImpl(IDatabaseConnector connector) {
        if (connector == null) {
            throw new IllegalArgumentException("Database connector cannot be null.");
        }
        this.connector = connector;
    }

    @Override
    public boolean addUser(User user) throws SQLException {
        if (user == null || user.getUserId() == null || user.getRole() == null) {
            return false;
        }

        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_USER_SQL)) {

            ps.setString(1, user.getUserId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getPhoneNumber());
            ps.setString(6, user.getRole());
            ps.setBoolean(7, user.isVerified());

            if (user instanceof Landlord) {
                ps.setString(8, ((Landlord) user).getAgentLicenseNumber());
                ps.setNull(9, Types.BOOLEAN);
            } else if (user instanceof Client) {
                ps.setNull(8, Types.VARCHAR);
                ps.setBoolean(9, ((Client) user).isReceivesMarketUpdates());
            } else {
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.BOOLEAN);
            }

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<User> getUserById(String userId) throws SQLException {
        if (userId == null || userId.trim().isEmpty()) {
            return Optional.empty();
        }
        return getUserBy(SELECT_USER_BY_ID_SQL, userId);
    }

    @Override
    public Optional<User> getUserByEmail(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return getUserBy(SELECT_USER_BY_EMAIL_SQL, email);
    }

    private Optional<User> getUserBy(String sql, String parameter) throws SQLException {
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, parameter);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                String role = rs.getString("role");
                User user;
                if ("LANDLORD".equalsIgnoreCase(role)) {
                    Landlord landlord = new Landlord();
                    landlord.setAgentLicenseNumber(rs.getString("agent_license_number"));
                    user = landlord;
                } else if ("CLIENT".equalsIgnoreCase(role)) {
                    Client client = new Client();
                    boolean receives = rs.getBoolean("receives_market_updates");
                    client.setReceivesMarketUpdates(!rs.wasNull() && receives);
                    user = client;
                } else {
                    user = new User();
                }

                user.setUserId(rs.getString("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setRole(role);
                user.setVerified(rs.getBoolean("is_verified"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));

                return Optional.of(user);
            }
        }
    }

    @Override
    public boolean updateUser(User user) throws SQLException {
        if (user == null || user.getUserId() == null) {
            return false;
        }

        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_USER_SQL)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getPhoneNumber());
            ps.setString(5, user.getRole());
            ps.setBoolean(6, user.isVerified());

            if (user instanceof Landlord) {
                ps.setString(7, ((Landlord) user).getAgentLicenseNumber());
                ps.setNull(8, Types.BOOLEAN);
            } else if (user instanceof Client) {
                ps.setNull(7, Types.VARCHAR);
                ps.setBoolean(8, ((Client) user).isReceivesMarketUpdates());
            } else {
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.BOOLEAN);
            }

            ps.setString(9, user.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteUser(String userId) throws SQLException {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_USER_SQL)) {

            ps.setString(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Landlord> getAllLandlords() throws SQLException {
        List<Landlord> landlords = new ArrayList<>();
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_LANDLORDS_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Landlord l = new Landlord();
                l.setUserId(rs.getString("user_id"));
                l.setName(rs.getString("name"));
                l.setEmail(rs.getString("email"));
                l.setPasswordHash(rs.getString("password_hash"));
                l.setPhoneNumber(rs.getString("phone_number"));
                l.setRole(rs.getString("role"));
                l.setVerified(rs.getBoolean("is_verified"));
                l.setAgentLicenseNumber(rs.getString("agent_license_number"));
                l.setCreatedAt(rs.getTimestamp("created_at"));
                l.setUpdatedAt(rs.getTimestamp("updated_at"));
                landlords.add(l);
            }
        }
        return landlords;
    }

    @Override
    public List<Client> getAllClients() throws SQLException {
        List<Client> clients = new ArrayList<>();
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_CLIENTS_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Client c = new Client();
                c.setUserId(rs.getString("user_id"));
                c.setName(rs.getString("name"));
                c.setEmail(rs.getString("email"));
                c.setPasswordHash(rs.getString("password_hash"));
                c.setPhoneNumber(rs.getString("phone_number"));
                c.setRole(rs.getString("role"));
                c.setVerified(rs.getBoolean("is_verified"));
                boolean receives = rs.getBoolean("receives_market_updates");
                c.setReceivesMarketUpdates(!rs.wasNull() && receives);
                c.setCreatedAt(rs.getTimestamp("created_at"));
                c.setUpdatedAt(rs.getTimestamp("updated_at"));
                clients.add(c);
            }
        }
        return clients;
    }
}
