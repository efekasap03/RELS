// src/main/java/UserOperations/AdminOperations.java
package UserOperations;

import com.rels.domain.Landlord;
import com.rels.domain.Property;
import com.rels.domain.Bid;
import com.rels.connector.DatabaseConnectorImpl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminOperations implements IAdminOperations {

    private final Connection conn;

    public AdminOperations(DatabaseConnectorImpl connector) {
        try {
            this.conn = connector.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean addLandlord(Landlord landlord) {
        String sql = """
            INSERT INTO users(user_id, name, email, password_hash, role, agent_license_number)
            VALUES (?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, landlord.getUserId());
            ps.setString(2, landlord.getName());
            ps.setString(3, landlord.getEmail());
            ps.setString(4, landlord.getPasswordHash());
            ps.setString(5, landlord.getRole());
            ps.setString(6, landlord.getAgentLicenseNumber());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Error adding landlord", ex);
        }
    }

    @Override
    public boolean editLandlord(Landlord landlord) {
        String sql = """
            UPDATE users
               SET name=?, email=?, password_hash=?, agent_license_number=?
             WHERE user_id=? AND role='LANDLORD'
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, landlord.getName());
            ps.setString(2, landlord.getEmail());
            ps.setString(3, landlord.getPasswordHash());
            ps.setString(4, landlord.getAgentLicenseNumber());
            ps.setString(5, landlord.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Error editing landlord", ex);
        }
    }

    @Override
    public List<Property> monitorProperties() {
        String sql = "SELECT * FROM properties WHERE is_active = TRUE";
        List<Property> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Property p = new Property();
                p.setPropertyId(rs.getString("property_id"));
                p.setLandlordId(rs.getString("landlord_id"));
                p.setAddress(rs.getString("address"));
                p.setCity(rs.getString("city"));
                p.setPrice(rs.getBigDecimal("price"));
                p.setSquareFootage(rs.getBigDecimal("square_footage"));
                p.setBedrooms(rs.getInt("bedrooms"));
                p.setBathrooms(rs.getInt("bathrooms"));
                p.setActive(rs.getBoolean("is_active"));
                list.add(p);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error fetching properties", ex);
        }
        return list;
    }

    @Override
    public List<Bid> monitorBids() {
        String sql = "SELECT * FROM bids";
        List<Bid> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Bid b = new Bid();
                b.setBidId(rs.getString("bid_id"));
                b.setPropertyId(rs.getString("property_id"));
                b.setClientId(rs.getString("client_id"));
                b.setAmount(rs.getBigDecimal("amount"));
                b.setStatus(rs.getString("status"));
                list.add(b);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error fetching bids", ex);
        }
        return list;
    }

    @Override
    public String generateReports() {
        // Şimdilik basit özet: kaç kayıt var
        int landlordCount = count("SELECT COUNT(*) FROM users WHERE role='LANDLORD'");
        int propCount     = count("SELECT COUNT(*) FROM properties");
        int bidCount      = count("SELECT COUNT(*) FROM bids");
        return String.format(
                "Report Summary:%n" +
                        "Landlords: %d%n" +
                        "Properties: %d%n" +
                        "Bids: %d",
                landlordCount, propCount, bidCount
        );
    }


    public List<Landlord> getAllLandlords() {
        return List.of();
    }

    private int count(String sql) {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Error counting records", ex);
        }
    }
}