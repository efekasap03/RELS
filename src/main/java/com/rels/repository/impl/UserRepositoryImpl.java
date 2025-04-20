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
    private static final String INSERT_USER_SQL = "INSERT INTO users (user_id, name, email, password_hash, phone_number, role, is_verified, agent_license_number, receives_market_updates, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
    private static final String SELECT_USER_BASE_SQL = "SELECT user_id, name, email, password_hash, phone_number, role, is_verified, created_at, updated_at, agent_license_number, receives_market_updates FROM users "; // Note space
    private static final String SELECT_USER_BY_ID_SQL = SELECT_USER_BASE_SQL + "WHERE user_id = ?";
    private static final String SELECT_USER_BY_EMAIL_SQL = SELECT_USER_BASE_SQL + "WHERE email = ?";
    private static final String SELECT_LANDLORDS_SQL = SELECT_USER_BASE_SQL + "WHERE role = 'LANDLORD'";
    private static final String SELECT_CLIENTS_SQL = SELECT_USER_BASE_SQL + "WHERE role = 'CLIENT'";
    private static final String UPDATE_USER_SQL = "UPDATE users SET name = ?, email = ?, password_hash = ?, phone_number = ?, role = ?, is_verified = ?, agent_license_number = ?, receives_market_updates = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
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
            // Basic validation, more could be added
            return false;
        }

        // Use try-with-resources for automatic resource management
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_USER_SQL)) {

            ps.setString(1, user.getUserId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getPhoneNumber()); // Can be null
            ps.setString(6, user.getRole());
            ps.setBoolean(7, user.isVerified());

            // Handle role-specific fields
            if (user instanceof Landlord) {
                ps.setString(8, ((Landlord) user).getAgentLicenseNumber());
                ps.setNull(9, Types.BOOLEAN); // Client field is null
            } else if (user instanceof Client) {
                ps.setNull(8, Types.VARCHAR); // Landlord field is null
                // Use setBoolean - DB column default applies if we insert null here
                ps.setBoolean(9, ((Client) user).isReceivesMarketUpdates());
            } else {
                // Handle generic User or ADMIN role, setting both role fields to null
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.BOOLEAN);
            }

            // Let DB handle defaults for timestamps (created_at, updated_at)

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0; // More robust check than == 1
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

    // Helper method for common getUser logic, returning Optional
    private Optional<User> getUserBy(String sql, String parameter) throws SQLException {
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, parameter);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { rs) throws SQLException {
                    String role = rs.getString("role");
                    User user;

                    // Instantiate the correct subclass based on the role
                    if ("LANDLORD".equalsIgnoreCase(role)) {
                        Landlord landlord = new Landlord();
                        // Set landlord-specific field
                        landlord.setAgentLicenseNumber(rs.getString("agent_license_number"));
                        user = landlord; // Assign to the base User variable
                    } else if ("CLIENT".equalsIgnoreCase(role)) {
                        Client client = new Client();
                        // Set client-specific field
                        // Use getBoolean, handling potential NULL if default wasn't applied or column allows null
                        boolean receivesUpdates = rs.getBoolean("receives_market_updates");
                        client.setReceivesMarketUpdates(!rs.wasNull() && receivesUpdates); // Set false if NULL
                        user = client; // Assign to the base User variable
                    } else {
                        // Could be ADMIN or just a generic User if role isn't strictly LANDLORD/CLIENT
                        // Log if role is unexpected based on application design
                        if (role != null && !role.equalsIgnoreCase("ADMIN")) { // Example check
                            System.err.println("Warning: Mapping user with unexpected role: " + role + " for ID: " + rs.getString("user_id"));
                        }
                        user = new User();
                    }

                    // Set common fields inherited from User using data from the current ResultSet row
                    user.setUserId(rs.getString("user_id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash")); // Be careful exposing this!
                    user.setPhoneNumber(rs.getString("phone_number"));
                    user.setRole(role); // Set the actual role read from DB
                    user.setVerified(rs.getBoolean("is_verified"));
                    user.setCreatedAt(rs.getTimestamp("created_at")); // Use setter overload
                    user.setUpdatedAt(rs.getTimestamp("updated_at")); // Use setter overload

                    return user;
                }
                }