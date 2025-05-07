package UserOperations;

import Data.connector.DatabaseConnectorImpl;
import Data.domain.Landlord;
import Data.domain.Property;
import Data.domain.Bid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminOperationsTest {

    @Mock
    private DatabaseConnectorImpl mockConnector;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    private AdminOperations adminOperations;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        
        when(mockConnector.getConnection()).thenReturn(mockConnection);
        
        adminOperations = new AdminOperations(mockConnector);
        
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
    }

    @Test
    void testAddLandlord_Success() throws SQLException {
        Landlord landlord = new Landlord();
        landlord.setUserId("testlandlord");
        landlord.setName("Test Landlord");
        landlord.setEmail("test@example.com");
        landlord.setPasswordHash("password123");
        landlord.setAgentLicenseNumber("AGENT007");

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = adminOperations.addLandlord(landlord);

        assertTrue(result);

        String expectedSql = "INSERT INTO users(user_id, name, email, password_hash, role, agent_license_number) " +
                             "VALUES (?, ?, ?, ?, 'LANDLORD', ?)";
        verify(mockConnection).prepareStatement(expectedSql);
        verify(mockPreparedStatement).setString(1, "testlandlord");
        verify(mockPreparedStatement).setString(2, "Test Landlord");
        verify(mockPreparedStatement).setString(3, "test@example.com");
        verify(mockPreparedStatement).setString(4, "password123");
        verify(mockPreparedStatement).setString(5, "AGENT007");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testAddLandlord_Failure_ThrowsException() throws SQLException {
        Landlord landlord = new Landlord();
        landlord.setUserId("fail_landlord");
        // Set other properties as needed, though they might not be reached if executeUpdate fails early

        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            adminOperations.addLandlord(landlord);
        });

        assertEquals("Failed to add landlord", exception.getMessage());
        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals("Database error", exception.getCause().getMessage());

        // Verify prepareStatement was called, but setters might not be if exception occurs during prepare
        String expectedSql = "INSERT INTO users(user_id, name, email, password_hash, role, agent_license_number) " +
                             "VALUES (?, ?, ?, ?, 'LANDLORD', ?)";
        verify(mockConnection).prepareStatement(expectedSql);
        // Depending on when the SQLException is thrown by a real PreparedStatement,
        // setters might or might not have been called.
        // For this mock, executeUpdate is the one throwing, so setters would have been called.
        verify(mockPreparedStatement).setString(1, "fail_landlord");
        // ... verify other setters if necessary for this test case
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testEditLandlord_Success() throws SQLException {
        Landlord landlord = new Landlord();
        landlord.setUserId("existingLandlord");
        landlord.setName("Updated Name");
        landlord.setEmail("updated@example.com");
        landlord.setPasswordHash("newPassword");
        landlord.setAgentLicenseNumber("NEWAGENT123");

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = adminOperations.editLandlord(landlord);

        assertTrue(result);

        String expectedSql = "UPDATE users SET name = ?, email = ?, password_hash = ?, agent_license_number = ? " +
                             "WHERE user_id = ? AND role = 'LANDLORD'";
        verify(mockConnection).prepareStatement(expectedSql);
        verify(mockPreparedStatement).setString(1, "Updated Name");
        verify(mockPreparedStatement).setString(2, "updated@example.com");
        verify(mockPreparedStatement).setString(3, "newPassword");
        verify(mockPreparedStatement).setString(4, "NEWAGENT123");
        verify(mockPreparedStatement).setString(5, "existingLandlord");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testEditLandlord_NoRowsAffected() throws SQLException {
        Landlord landlord = new Landlord();
        landlord.setUserId("nonExistentLandlord");
        landlord.setName("No Update Name");
        // Set other fields

        when(mockPreparedStatement.executeUpdate()).thenReturn(0); // Simulate no rows affected

        boolean result = adminOperations.editLandlord(landlord);

        assertFalse(result);
        // Verify SQL and parameters are still set as expected
        String expectedSql = "UPDATE users SET name = ?, email = ?, password_hash = ?, agent_license_number = ? " +
                             "WHERE user_id = ? AND role = 'LANDLORD'";
        verify(mockConnection).prepareStatement(expectedSql);
        verify(mockPreparedStatement).setString(5, "nonExistentLandlord"); // Check at least the ID
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testEditLandlord_Failure_ThrowsException() throws SQLException {
        Landlord landlord = new Landlord();
        landlord.setUserId("fail_edit_landlord");
        // Set other properties

        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Database error during edit"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            adminOperations.editLandlord(landlord);
        });

        assertEquals("Failed to edit landlord", exception.getMessage());
        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals("Database error during edit", exception.getCause().getMessage());

        String expectedSql = "UPDATE users SET name = ?, email = ?, password_hash = ?, agent_license_number = ? " +
                             "WHERE user_id = ? AND role = 'LANDLORD'";
        verify(mockConnection).prepareStatement(expectedSql);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testMonitorProperties_Success_ReturnsProperties() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, true, false); // Simulate two properties
        when(mockResultSet.getString("property_id")).thenReturn("P1", "P2");
        when(mockResultSet.getString("landlord_id")).thenReturn("L1", "L2");
        when(mockResultSet.getString("address")).thenReturn("123 Main St", "456 Oak Ave");
        when(mockResultSet.getString("city")).thenReturn("CityA", "CityB");
        when(mockResultSet.getString("postal_code")).thenReturn("12345", "67890");
        when(mockResultSet.getString("property_type")).thenReturn("HOUSE", "CONDO");
        when(mockResultSet.getString("description")).thenReturn("Nice house", "Great condo");
        when(mockResultSet.getBigDecimal("price")).thenReturn(new BigDecimal("250000"), new BigDecimal("150000"));
        when(mockResultSet.getBigDecimal("square_footage")).thenReturn(new BigDecimal("1500.00"), new BigDecimal("900.50"));
        when(mockResultSet.getInt("bedrooms")).thenReturn(3, 2);
        when(mockResultSet.getInt("bathrooms")).thenReturn(2, 1);
        when(mockResultSet.getBoolean("is_active")).thenReturn(true, false);

        List<Property> properties = adminOperations.monitorProperties();

        assertNotNull(properties);
        assertEquals(2, properties.size());

        Property p1 = properties.get(0);
        assertEquals("P1", p1.getPropertyId());
        assertEquals("L1", p1.getLandlordId());
        assertEquals("123 Main St", p1.getAddress());
        assertTrue(p1.isActive());

        Property p2 = properties.get(1);
        assertEquals("P2", p2.getPropertyId());
        assertEquals("CityB", p2.getCity());
        assertFalse(p2.isActive());

        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery("SELECT * FROM properties");
    }

    @Test
    void testMonitorProperties_Success_NoProperties() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // No properties

        List<Property> properties = adminOperations.monitorProperties();

        assertNotNull(properties);
        assertTrue(properties.isEmpty());

        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery("SELECT * FROM properties");
    }

    @Test
    void testMonitorProperties_Failure_ThrowsException() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("DB error fetching properties"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            adminOperations.monitorProperties();
        });

        assertEquals("Failed to fetch properties", exception.getMessage());
        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals("DB error fetching properties", exception.getCause().getMessage());

        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery("SELECT * FROM properties");
    }

    @Test
    void testMonitorBids_Success_ReturnsBids() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        Timestamp bidTime1 = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp bidTime2 = Timestamp.valueOf(LocalDateTime.now());

        when(mockResultSet.next()).thenReturn(true, true, false); // Simulate two bids
        when(mockResultSet.getString("bid_id")).thenReturn("B1", "B2");
        when(mockResultSet.getString("property_id")).thenReturn("P1", "P1");
        when(mockResultSet.getString("client_id")).thenReturn("C1", "C2");
        when(mockResultSet.getBigDecimal("amount")).thenReturn(new BigDecimal("1000"), new BigDecimal("1200"));
        when(mockResultSet.getString("status")).thenReturn("PENDING", "ACCEPTED");
        when(mockResultSet.getTimestamp("bid_timestamp")).thenReturn(bidTime1, bidTime2);

        List<Bid> bids = adminOperations.monitorBids();

        assertNotNull(bids);
        assertEquals(2, bids.size());

        Bid b1 = bids.get(0);
        assertEquals("B1", b1.getBidId());
        assertEquals("P1", b1.getPropertyId());
        assertEquals("C1", b1.getClientId());
        assertEquals(new BigDecimal("1000"), b1.getAmount());
        assertEquals("PENDING", b1.getStatus());
        assertEquals(bidTime1.toLocalDateTime(), b1.getBidTimestamp());

        Bid b2 = bids.get(1);
        assertEquals("B2", b2.getBidId());
        assertEquals("ACCEPTED", b2.getStatus());

        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery("SELECT * FROM bids");
    }

    @Test
    void testMonitorBids_Success_NoBids() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // No bids

        List<Bid> bids = adminOperations.monitorBids();

        assertNotNull(bids);
        assertTrue(bids.isEmpty());

        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery("SELECT * FROM bids");
    }

    @Test
    void testMonitorBids_Failure_ThrowsException() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("DB error fetching bids"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            adminOperations.monitorBids();
        });

        assertEquals("Failed to fetch bids", exception.getMessage());
        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals("DB error fetching bids", exception.getCause().getMessage());

        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery("SELECT * FROM bids");
    }

    @Test
    void testGenerateReport_Success() throws SQLException {
        // Mocking the ResultSet for multiple calls to countRecords
        // Order of calls in generateReports():
        // 1. Landlords
        // 2. Active Properties
        // 3. Inactive Properties
        // 4. Pending Bids
        // 5. Accepted Bids
        // 6. Rejected Bids

        when(mockStatement.executeQuery(contains("COUNT(*)"))).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, true, true, true, false); // For each countRecords call
        when(mockResultSet.getInt(1))
            .thenReturn(5)  // Landlords
            .thenReturn(10) // Active Properties
            .thenReturn(2)  // Inactive Properties
            .thenReturn(3)  // Pending Bids
            .thenReturn(7)  // Accepted Bids
            .thenReturn(1); // Rejected Bids

        String report = adminOperations.generateReports();

        String expectedReport = "Total Landlords: 5\n" +
                                "Active Properties: 10\n" +
                                "Inactive Properties: 2\n" +
                                "Pending Bids: 3\n" +
                                "Accepted Bids: 7\n" +
                                "Rejected Bids: 1\n";
        assertEquals(expectedReport, report);

        // Verify that createStatement was called (it's used by countRecords multiple times)
        // The number of times executeQuery is called for COUNT depends on implementation detail of generateReports
        // Here we expect 6 calls to countRecords
        verify(mockConnection, times(6)).createStatement();
        verify(mockStatement, times(1)).executeQuery("SELECT COUNT(*) FROM users WHERE role = 'LANDLORD'");
        verify(mockStatement, times(1)).executeQuery("SELECT COUNT(*) FROM properties WHERE is_active = TRUE");
        verify(mockStatement, times(1)).executeQuery("SELECT COUNT(*) FROM properties WHERE is_active = FALSE");
        verify(mockStatement, times(1)).executeQuery("SELECT COUNT(*) FROM bids WHERE status = 'PENDING'");
        verify(mockStatement, times(1)).executeQuery("SELECT COUNT(*) FROM bids WHERE status = 'ACCEPTED'");
        verify(mockStatement, times(1)).executeQuery("SELECT COUNT(*) FROM bids WHERE status = 'REJECTED'");
    }

    @Test
    void testGenerateReport_CountRecordsFailure() throws SQLException {
        // Let the first call to countRecords (for landlords) succeed, then the second one fail.
        when(mockStatement.executeQuery(contains("COUNT(*)"))).thenReturn(mockResultSet);
        when(mockResultSet.next())
            .thenReturn(true) // For landlord count: rs.next() is true
            .thenReturn(true); // For active properties count: rs.next() is true, but executeQuery will throw
        when(mockResultSet.getInt(1)).thenReturn(5); // Landlord count

        // Make the second executeQuery (for active properties) throw an exception
        when(mockStatement.executeQuery("SELECT COUNT(*) FROM users WHERE role = 'LANDLORD'")).thenReturn(mockResultSet);
        when(mockStatement.executeQuery("SELECT COUNT(*) FROM properties WHERE is_active = TRUE"))
            .thenThrow(new SQLException("DB error during count"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            adminOperations.generateReports();
        });

        assertEquals("Failed to count records", exception.getMessage());
        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals("DB error during count", exception.getCause().getMessage());

        // Verify createStatement was called for the first successful count and the failing one
        verify(mockConnection, times(2)).createStatement();
        verify(mockStatement, times(1)).executeQuery("SELECT COUNT(*) FROM users WHERE role = 'LANDLORD'");
        verify(mockStatement, times(1)).executeQuery("SELECT COUNT(*) FROM properties WHERE is_active = TRUE");
    }

    @Test
    void testGetAllLandlords_Success_ReturnsLandlords() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, true, false); // Simulate two landlords
        when(mockResultSet.getString("user_id")).thenReturn("L100", "L200");
        when(mockResultSet.getString("name")).thenReturn("Landlord One", "Landlord Two");
        when(mockResultSet.getString("email")).thenReturn("l1@example.com", "l2@example.com");
        when(mockResultSet.getString("password_hash")).thenReturn("pass1", "pass2");
        when(mockResultSet.getString("agent_license_number")).thenReturn("LIC1", "LIC2");

        List<Landlord> landlords = adminOperations.getAllLandlords();

        assertNotNull(landlords);
        assertEquals(2, landlords.size());

        Landlord l1 = landlords.get(0);
        assertEquals("L100", l1.getUserId());
        assertEquals("Landlord One", l1.getName());
        assertEquals("l1@example.com", l1.getEmail());
        assertEquals("pass1", l1.getPasswordHash());
        assertEquals("LIC1", l1.getAgentLicenseNumber());

        Landlord l2 = landlords.get(1);
        assertEquals("L200", l2.getUserId());
        assertEquals("Landlord Two", l2.getName());

        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery("SELECT * FROM users WHERE role = 'LANDLORD'");
    }

    @Test
    void testGetAllLandlords_Success_NoLandlords() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // No landlords

        List<Landlord> landlords = adminOperations.getAllLandlords();

        assertNotNull(landlords);
        assertTrue(landlords.isEmpty());

        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery("SELECT * FROM users WHERE role = 'LANDLORD'");
    }

    @Test
    void testGetAllLandlords_Failure_ThrowsException() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("DB error fetching landlords"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            adminOperations.getAllLandlords();
        });

        assertEquals("Failed to fetch landlords", exception.getMessage());
        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals("DB error fetching landlords", exception.getCause().getMessage());

        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery("SELECT * FROM users WHERE role = 'LANDLORD'");
    }
} 