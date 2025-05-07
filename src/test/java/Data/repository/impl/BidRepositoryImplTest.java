package Data.repository.impl;

import Data.connector.IDatabaseConnector;
import Data.domain.Bid;
import Data.repository.interfaces.IBidRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Unit tests for BidRepositoryImpl using Mockito to mock the database connector and JDBC objects.
class BidRepositoryImplTest {

    // Mock objects
    private IDatabaseConnector mockConnector;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    // The repository instance under test
    private IBidRepository bidRepository;

    // Test data
    private static final String TEST_BID_ID = "bid123";
    private static final String TEST_PROPERTY_ID = "prop456";
    private static final String TEST_CLIENT_ID = "client789";
    private static final BigDecimal TEST_AMOUNT = new BigDecimal("1000.50");
    private static final String TEST_STATUS = "PENDING";
    private static final LocalDateTime TEST_BID_TIMESTAMP = LocalDateTime.of(2023, 1, 1, 10, 0);
    private static final LocalDateTime TEST_CREATED_AT = LocalDateTime.of(2023, 1, 1, 9, 0);
    private static final LocalDateTime TEST_UPDATED_AT = LocalDateTime.of(2023, 1, 1, 9, 0);


    @BeforeEach
    void setUp() throws SQLException {
        // Create fresh mocks before each test
        mockConnector = mock(IDatabaseConnector.class);
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Configure the mock connector to return the mock connection
        when(mockConnector.getConnection()).thenReturn(mockConnection);

        // Configure the mock connection to return the mock prepared statement for *any* SQL string
        // We use anyString() here because the SQL constants in the SUT are private and not accessible for verification.
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Configure the mock prepared statement to return the mock result set for queries
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Configure the mock prepared statement to return a non-zero value for updates by default
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);


        // Instantiate the repository with the mock connector
        bidRepository = new BidRepositoryImpl(mockConnector);
    }

    // --- Constructor Tests ---

    @Test
    void testConstructor_ValidConnector() {
        // Arrange, Act (done in @BeforeEach)
        // Assert
        assertNotNull(bidRepository, "Repository instance should be created");
        // We can't directly assert the private 'connector' field, but successful creation implies it was assigned.
    }

    @Test
    void testConstructor_NullConnectorThrowsException() {
        // Arrange, Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new BidRepositoryImpl(null);
        }, "Constructor should throw IllegalArgumentException for null connector");

        assertTrue(exception.getMessage().contains("Database connector cannot be null."),
                "Exception message should indicate null connector");
    }

    // --- addBid Tests ---

    @Test
    void testAddBid_Success() throws SQLException {
        // Arrange
        Bid bid = new Bid();
        bid.setBidId(TEST_BID_ID);
        bid.setPropertyId(TEST_PROPERTY_ID);
        bid.setClientId(TEST_CLIENT_ID);
        bid.setAmount(TEST_AMOUNT);
        bid.setStatus(TEST_STATUS);
        bid.setBidTimestamp(TEST_BID_TIMESTAMP); // Non-null timestamp

        // When executeUpdate() is called, return 1 (indicating one row affected)
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = bidRepository.addBid(bid);

        // Assert
        assertTrue(result, "addBid should return true on successful insertion");

        // Verify interactions with mocks
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString()); // Verify preparesStatement was called once with any string
        verify(mockPreparedStatement, times(1)).setString(1, TEST_BID_ID);
        verify(mockPreparedStatement, times(1)).setString(2, TEST_PROPERTY_ID);
        verify(mockPreparedStatement, times(1)).setString(3, TEST_CLIENT_ID);
        verify(mockPreparedStatement, times(1)).setBigDecimal(4, TEST_AMOUNT);
        verify(mockPreparedStatement, times(1)).setString(5, TEST_STATUS);
        verify(mockPreparedStatement, times(1)).setTimestamp(eq(6), any(Timestamp.class)); // Verify setTimestamp is called for non-null
        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify resources are closed (try-with-resources)
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }

    @Test
    void testAddBid_Success_NullBidTimestamp() throws SQLException {
        // Arrange
        Bid bid = new Bid();
        bid.setBidId(TEST_BID_ID);
        bid.setPropertyId(TEST_PROPERTY_ID);
        bid.setClientId(TEST_CLIENT_ID);
        bid.setAmount(TEST_AMOUNT);
        bid.setStatus(TEST_STATUS);
        bid.setBidTimestamp((LocalDateTime) null); // Null timestamp

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = bidRepository.addBid(bid);

        // Assert
        assertTrue(result, "addBid should return true on successful insertion with null timestamp");

        // Verify setNull is called for the null timestamp
        verify(mockPreparedStatement, times(1)).setNull(6, Types.TIMESTAMP);
        // Verify other parameters were set correctly (rest of the calls similar to the success case)
        verify(mockPreparedStatement, times(1)).setString(1, TEST_BID_ID);
        verify(mockPreparedStatement, times(1)).setString(2, TEST_PROPERTY_ID);
        verify(mockPreparedStatement, times(1)).setString(3, TEST_CLIENT_ID);
        verify(mockPreparedStatement, times(1)).setBigDecimal(4, TEST_AMOUNT);
        verify(mockPreparedStatement, times(1)).setString(5, TEST_STATUS);
        verify(mockPreparedStatement, times(1)).executeUpdate();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }


    @Test
    void testAddBid_DatabaseError() throws SQLException {
        // Arrange
        Bid bid = new Bid();
        bid.setBidId(TEST_BID_ID);
        bid.setPropertyId(TEST_PROPERTY_ID);
        bid.setClientId(TEST_CLIENT_ID);
        bid.setAmount(TEST_AMOUNT);
        bid.setStatus(TEST_STATUS);
        bid.setBidTimestamp(TEST_BID_TIMESTAMP);

        // Configure mock PreparedStatement to throw SQLException
        SQLException expectedException = new SQLException("Simulated DB error");
        when(mockPreparedStatement.executeUpdate()).thenThrow(expectedException);

        // Act
        boolean result = bidRepository.addBid(bid);

        // Assert
        assertFalse(result, "addBid should return false on database error");

        // Verify interactions (parameters were set, executeUpdate was attempted)
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, TEST_BID_ID);
        verify(mockPreparedStatement, times(1)).setString(2, TEST_PROPERTY_ID);
        verify(mockPreparedStatement, times(1)).setString(3, TEST_CLIENT_ID);
        verify(mockPreparedStatement, times(1)).setBigDecimal(4, TEST_AMOUNT);
        verify(mockPreparedStatement, times(1)).setString(5, TEST_STATUS);
        verify(mockPreparedStatement, times(1)).setTimestamp(eq(6), any(Timestamp.class));
        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify resources are closed despite error (try-with-resources)
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }

    @Test
    void testAddBid_NullBidObject() throws SQLException {
        // Arrange
        Bid nullBid = null;

        // Act
        boolean result = bidRepository.addBid(nullBid);

        // Assert
        assertFalse(result, "addBid should return false for null bid object");
        // Verify no interaction with the database connector
        verify(mockConnector, never()).getConnection();
    }

    @Test
    void testAddBid_BidWithNullRequiredFields() throws SQLException {
        // Arrange
        Bid bidWithNulls = new Bid();
        bidWithNulls.setBidId(null); // Make BidId null
        bidWithNulls.setPropertyId(TEST_PROPERTY_ID);
        bidWithNulls.setClientId(TEST_CLIENT_ID);
        bidWithNulls.setStatus(TEST_STATUS);

        // Act
        boolean result = bidRepository.addBid(bidWithNulls);

        // Assert
        assertFalse(result, "addBid should return false for bid with null required fields");
        // Verify no interaction with the database connector
        verify(mockConnector, never()).getConnection();
    }


    // --- getBidById Tests ---

    @Test
    void testGetBidById_Found() throws SQLException {
        // Arrange
        // Configure mock ResultSet to have one row and return test data
        // CORRECTED: expect next() to be called only once for single result query
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("bid_id")).thenReturn(TEST_BID_ID);
        when(mockResultSet.getString("property_id")).thenReturn(TEST_PROPERTY_ID);
        when(mockResultSet.getString("client_id")).thenReturn(TEST_CLIENT_ID);
        when(mockResultSet.getBigDecimal("amount")).thenReturn(TEST_AMOUNT);
        when(mockResultSet.getString("status")).thenReturn(TEST_STATUS);
        // Mocking getTimestamp requires careful handling for nullability if needed
        when(mockResultSet.getTimestamp("bid_timestamp")).thenReturn(Timestamp.valueOf(TEST_BID_TIMESTAMP));
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(TEST_CREATED_AT));
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(TEST_UPDATED_AT));


        // Act
        Bid foundBid = bidRepository.getBidById(TEST_BID_ID);

        // Assert
        assertNotNull(foundBid, "getBidById should return a Bid object when found");
        assertEquals(TEST_BID_ID, foundBid.getBidId());
        assertEquals(TEST_PROPERTY_ID, foundBid.getPropertyId());
        assertEquals(TEST_CLIENT_ID, foundBid.getClientId());
        assertEquals(TEST_AMOUNT, foundBid.getAmount());
        assertEquals(TEST_STATUS, foundBid.getStatus());
        assertEquals(TEST_BID_TIMESTAMP, foundBid.getBidTimestamp());
        // We test mapping for created_at/updated_at via their presence,
        // assuming their mapping logic is covered by Bid's own tests.
        assertNotNull(foundBid.getCreatedAt());
        assertNotNull(foundBid.getUpdatedAt());


        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString()); // Verify preparesStatement was called once with any string
        verify(mockPreparedStatement, times(1)).setString(1, TEST_BID_ID);
        verify(mockPreparedStatement, times(1)).executeQuery();
        // CORRECTED: Verify next() was called only once
        verify(mockResultSet, times(1)).next();
        // Verify data reading happened for the single row
        verify(mockResultSet, times(1)).getString("bid_id");
        verify(mockResultSet, times(1)).getString("property_id");
        verify(mockResultSet, times(1)).getString("client_id");
        verify(mockResultSet, times(1)).getBigDecimal("amount");
        verify(mockResultSet, times(1)).getString("status");
        verify(mockResultSet, times(1)).getTimestamp("bid_timestamp");
        verify(mockResultSet, times(1)).getTimestamp("created_at");
        verify(mockResultSet, times(1)).getTimestamp("updated_at");


        // Verify resources closed
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }

    @Test
    void testGetBidById_NotFound() throws SQLException {
        // Arrange
        // Configure mock ResultSet to have no rows
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Bid foundBid = bidRepository.getBidById("nonexistentId");

        // Assert
        assertNull(foundBid, "getBidById should return null when bid is not found");

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, "nonexistentId");
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next(); // Called once to check if row exists

        // Verify resources closed
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }


    @Test
    void testGetBidById_DatabaseError() throws SQLException {
        // Arrange
        // Configure mock PreparedStatement to throw SQLException on executeQuery
        SQLException expectedException = new SQLException("Simulated query error");
        when(mockPreparedStatement.executeQuery()).thenThrow(expectedException);

        // Act
        Bid foundBid = bidRepository.getBidById(TEST_BID_ID);

        // Assert
        assertNull(foundBid, "getBidById should return null on database error");

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, TEST_BID_ID);
        verify(mockPreparedStatement, times(1)).executeQuery(); // Execution attempted

        // Verify resources closed despite error
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
        // ResultSet close is not verified as executeQuery() threw exception before resultSet was returned
    }


    @Test
    void testGetBidById_NullOrEmptyId() throws SQLException {
        // Arrange
        String nullId = null;
        String emptyId = "";
        String blankId = "   ";

        // Act
        Bid bid1 = bidRepository.getBidById(nullId);
        Bid bid2 = bidRepository.getBidById(emptyId);
        Bid bid3 = bidRepository.getBidById(blankId);


        // Assert
        assertNull(bid1, "getBidById should return null for null ID");
        assertNull(bid2, "getBidById should return null for empty ID");
        assertNull(bid3, "getBidById should return null for blank ID");


        // Verify no interaction with the database connector
        verify(mockConnector, never()).getConnection();
    }


    // --- updateBid Tests ---

    @Test
    void testUpdateBid_Success() throws SQLException {
        // Arrange
        Bid bidToUpdate = new Bid();
        bidToUpdate.setBidId(TEST_BID_ID);
        bidToUpdate.setPropertyId("newPropId"); // Simulate updated fields
        bidToUpdate.setClientId("newClientId");
        bidToUpdate.setAmount(new BigDecimal("2500.00"));
        bidToUpdate.setStatus("ACCEPTED");
        bidToUpdate.setBidTimestamp(LocalDateTime.now()); // Simulate updated timestamp

        // Configure mock PreparedStatement to indicate success (1 row affected)
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = bidRepository.updateBid(bidToUpdate);

        // Assert
        assertTrue(result, "updateBid should return true on successful update");

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, "newPropId");
        verify(mockPreparedStatement, times(1)).setString(2, "newClientId");
        verify(mockPreparedStatement, times(1)).setBigDecimal(3, new BigDecimal("2500.00"));
        verify(mockPreparedStatement, times(1)).setString(4, "ACCEPTED");
        verify(mockPreparedStatement, times(1)).setTimestamp(eq(5), any(Timestamp.class)); // Verify setTimestamp is called for non-null
        verify(mockPreparedStatement, times(1)).setString(6, TEST_BID_ID); // Verify WHERE clause parameter
        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify resources closed
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }

    @Test
    void testUpdateBid_NoRowsAffected() throws SQLException {
        // Arrange
        Bid bidToUpdate = new Bid();
        bidToUpdate.setBidId("nonexistentId"); // Simulate updating a non-existent bid
        bidToUpdate.setPropertyId("newPropId");
        bidToUpdate.setClientId("newClientId");
        bidToUpdate.setAmount(new BigDecimal("2500.00"));
        bidToUpdate.setStatus("ACCEPTED");
        bidToUpdate.setBidTimestamp(LocalDateTime.now());

        // Configure mock PreparedStatement to indicate no rows affected
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = bidRepository.updateBid(bidToUpdate);

        // Assert
        assertFalse(result, "updateBid should return false if no rows were affected (bid not found)");

        // Verify interactions (parameters were set, executeUpdate was attempted)
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, "newPropId");
        verify(mockPreparedStatement, times(1)).setString(2, "newClientId");
        verify(mockPreparedStatement, times(1)).setBigDecimal(3, new BigDecimal("2500.00"));
        verify(mockPreparedStatement, times(1)).setString(4, "ACCEPTED");
        verify(mockPreparedStatement, times(1)).setTimestamp(eq(5), any(Timestamp.class));
        verify(mockPreparedStatement, times(1)).setString(6, "nonexistentId"); // Check the WHERE clause ID
        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify resources closed
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }

    @Test
    void testUpdateBid_DatabaseError() throws SQLException {
        // Arrange
        Bid bidToUpdate = new Bid();
        bidToUpdate.setBidId(TEST_BID_ID);
        bidToUpdate.setPropertyId("newPropId");
        bidToUpdate.setClientId("newClientId");
        bidToUpdate.setAmount(new BigDecimal("2500.00"));
        bidToUpdate.setStatus("ACCEPTED");
        bidToUpdate.setBidTimestamp(LocalDateTime.now());

        // Configure mock PreparedStatement to throw SQLException
        SQLException expectedException = new SQLException("Simulated DB error");
        when(mockPreparedStatement.executeUpdate()).thenThrow(expectedException);

        // Act
        boolean result = bidRepository.updateBid(bidToUpdate);

        // Assert
        assertFalse(result, "updateBid should return false on database error");

        // Verify interactions (parameters were set, executeUpdate was attempted)
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, "newPropId");
        verify(mockPreparedStatement, times(1)).setString(2, "newClientId");
        verify(mockPreparedStatement, times(1)).setBigDecimal(3, new BigDecimal("2500.00"));
        verify(mockPreparedStatement, times(1)).setString(4, "ACCEPTED");
        verify(mockPreparedStatement, times(1)).setTimestamp(eq(5), any(Timestamp.class));
        verify(mockPreparedStatement, times(1)).setString(6, TEST_BID_ID); // Check WHERE clause ID
        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify resources closed despite error
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }

    @Test
    void testUpdateBid_NullBidObject() throws SQLException {
        // Arrange
        Bid nullBid = null;

        // Act
        boolean result = bidRepository.updateBid(nullBid);

        // Assert
        assertFalse(result, "updateBid should return false for null bid object");
        // Verify no interaction with the database connector
        verify(mockConnector, never()).getConnection();
    }

    @Test
    void testUpdateBid_BidWithNullBidId() throws SQLException {
        // Arrange
        Bid bidWithNullId = new Bid();
        bidWithNullId.setBidId(null); // Make BidId null
        bidWithNullId.setPropertyId(TEST_PROPERTY_ID);
        // ... other fields ...

        // Act
        boolean result = bidRepository.updateBid(bidWithNullId);

        // Assert
        assertFalse(result, "updateBid should return false for bid with null bidId");
        // Verify no interaction with the database connector
        verify(mockConnector, never()).getConnection();
    }


    // --- getBidsByPropertyId Tests ---

    @Test
    void testGetBidsByPropertyId_Found() throws SQLException {
        // Arrange
        // Configure mock ResultSet to return two rows and then stop
        when(mockResultSet.next()).thenReturn(true, true, false); // First call true, second true, third false

        // Mock data for the first row
        when(mockResultSet.getString("bid_id")).thenReturn("bidA", "bidB");
        when(mockResultSet.getString("property_id")).thenReturn(TEST_PROPERTY_ID, TEST_PROPERTY_ID);
        when(mockResultSet.getString("client_id")).thenReturn("client1", "client2");
        when(mockResultSet.getBigDecimal("amount")).thenReturn(new BigDecimal("500"), new BigDecimal("1500"));
        when(mockResultSet.getString("status")).thenReturn("PENDING", "ACCEPTED");
        when(mockResultSet.getTimestamp("bid_timestamp")).thenReturn(Timestamp.valueOf(TEST_BID_TIMESTAMP.plusHours(1)), Timestamp.valueOf(TEST_BID_TIMESTAMP));
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(TEST_CREATED_AT), Timestamp.valueOf(TEST_CREATED_AT));
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(TEST_UPDATED_AT), Timestamp.valueOf(TEST_UPDATED_AT));


        // Act
        List<Bid> bids = bidRepository.getBidsByPropertyId(TEST_PROPERTY_ID);

        // Assert
        assertNotNull(bids, "getBidsByPropertyId should return a non-null list");
        assertEquals(2, bids.size(), "Should return a list with 2 bids");

        // Basic checks on the returned Bid objects
        assertEquals("bidA", bids.get(0).getBidId());
        assertEquals("bidB", bids.get(1).getBidId());
        assertEquals(TEST_PROPERTY_ID, bids.get(0).getPropertyId());
        assertEquals(TEST_PROPERTY_ID, bids.get(1).getPropertyId());
        assertEquals(new BigDecimal("500"), bids.get(0).getAmount());
        assertEquals(new BigDecimal("1500"), bids.get(1).getAmount());
        assertEquals(TEST_BID_TIMESTAMP.plusHours(1), bids.get(0).getBidTimestamp());
        assertEquals(TEST_BID_TIMESTAMP, bids.get(1).getBidTimestamp());


        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, TEST_PROPERTY_ID);
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(3)).next(); // Called twice to get rows, once to terminate

        // Verify data reading happened for each row (called twice per field for 2 rows)
        verify(mockResultSet, times(2)).getString("bid_id");
        verify(mockResultSet, times(2)).getString("property_id");
        verify(mockResultSet, times(2)).getString("client_id");
        verify(mockResultSet, times(2)).getBigDecimal("amount");
        verify(mockResultSet, times(2)).getString("status");
        verify(mockResultSet, times(2)).getTimestamp("bid_timestamp");
        verify(mockResultSet, times(2)).getTimestamp("created_at");
        verify(mockResultSet, times(2)).getTimestamp("updated_at");


        // Verify resources closed
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }


    @Test
    void testGetBidsByPropertyId_NotFound() throws SQLException {
        // Arrange
        // Configure mock ResultSet to have no rows
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Bid> bids = bidRepository.getBidsByPropertyId("nonexistentPropId");

        // Assert
        assertNotNull(bids, "getBidsByPropertyId should return a non-null list even if empty");
        assertTrue(bids.isEmpty(), "getBidsByPropertyId should return an empty list when no bids found");

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, "nonexistentPropId");
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next(); // Only called once to check for first row

        // Verify resources closed
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }


    @Test
    void testGetBidsByPropertyId_DatabaseError() throws SQLException {
        // Arrange
        // Configure mock PreparedStatement to throw SQLException on executeQuery
        SQLException expectedException = new SQLException("Simulated query error");
        when(mockPreparedStatement.executeQuery()).thenThrow(expectedException);

        // Act
        List<Bid> bids = bidRepository.getBidsByPropertyId(TEST_PROPERTY_ID);

        // Assert
        assertNotNull(bids, "getBidsByPropertyId should return a non-null list on database error");
        // The current implementation returns an empty list on error *before* iterating results
        assertTrue(bids.isEmpty(), "getBidsByPropertyId should return an empty list on database error");


        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, TEST_PROPERTY_ID);
        verify(mockPreparedStatement, times(1)).executeQuery(); // Execution attempted

        // Verify resources closed despite error
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
        // ResultSet close is not verified as executeQuery() threw exception
    }


    @Test
    void testGetBidsByPropertyId_NullOrEmptyId() throws SQLException {
        // Arrange
        String nullId = null;
        String emptyId = "";
        String blankId = "   ";

        // Act
        List<Bid> bids1 = bidRepository.getBidsByPropertyId(nullId);
        List<Bid> bids2 = bidRepository.getBidsByPropertyId(emptyId);
        List<Bid> bids3 = bidRepository.getBidsByPropertyId(blankId);


        // Assert
        assertNotNull(bids1, "getBidsByPropertyId should return non-null list for null ID");
        assertTrue(bids1.isEmpty(), "getBidsByPropertyId should return empty list for null ID");
        assertNotNull(bids2, "getBidsByPropertyId should return non-null list for empty ID");
        assertTrue(bids2.isEmpty(), "getBidsByPropertyId should return empty list for empty ID");
        assertNotNull(bids3, "getBidsByPropertyId should return non-null list for blank ID");
        assertTrue(bids3.isEmpty(), "getBidsByPropertyId should return empty list for blank ID");


        // Verify no interaction with the database connector
        verify(mockConnector, never()).getConnection();
    }


    // --- getBidsByUserId Tests (Similar to getBidsByPropertyId, test basics) ---

    @Test
    void testGetBidsByUserId_Found() throws SQLException {
        // Arrange
        // Configure mock ResultSet to return two rows and then stop
        when(mockResultSet.next()).thenReturn(true, true, false);

        // Mock data for the first row
        when(mockResultSet.getString("bid_id")).thenReturn("bidC", "bidD");
        when(mockResultSet.getString("property_id")).thenReturn("propX", "propY");
        when(mockResultSet.getString("client_id")).thenReturn(TEST_CLIENT_ID, TEST_CLIENT_ID);
        when(mockResultSet.getBigDecimal("amount")).thenReturn(new BigDecimal("750"), new BigDecimal("1200"));
        when(mockResultSet.getString("status")).thenReturn("PENDING", "REJECTED");
        when(mockResultSet.getTimestamp("bid_timestamp")).thenReturn(Timestamp.valueOf(TEST_BID_TIMESTAMP.plusHours(2)), Timestamp.valueOf(TEST_BID_TIMESTAMP.plusHours(3)));
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(TEST_CREATED_AT), Timestamp.valueOf(TEST_CREATED_AT));
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(TEST_UPDATED_AT), Timestamp.valueOf(TEST_UPDATED_AT));


        // Act
        List<Bid> bids = bidRepository.getBidsByUserId(TEST_CLIENT_ID);

        // Assert
        assertNotNull(bids, "getBidsByUserId should return a non-null list");
        assertEquals(2, bids.size(), "Should return a list with 2 bids for user");
        assertEquals("bidC", bids.get(0).getBidId());
        assertEquals("bidD", bids.get(1).getBidId());
        assertEquals(TEST_CLIENT_ID, bids.get(0).getClientId());
        assertEquals(TEST_CLIENT_ID, bids.get(1).getClientId());


        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, TEST_CLIENT_ID);
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(3)).next();

        // Verify data reading happened for each row (called twice per field for 2 rows)
        verify(mockResultSet, times(2)).getString("bid_id");
        verify(mockResultSet, times(2)).getString("property_id");
        verify(mockResultSet, times(2)).getString("client_id");
        verify(mockResultSet, times(2)).getBigDecimal("amount");
        verify(mockResultSet, times(2)).getString("status");
        verify(mockResultSet, times(2)).getTimestamp("bid_timestamp");
        verify(mockResultSet, times(2)).getTimestamp("created_at");
        verify(mockResultSet, times(2)).getTimestamp("updated_at");


        // Verify resources closed
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }


    @Test
    void testGetBidsByUserId_NotFound() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Bid> bids = bidRepository.getBidsByUserId("nonexistentUserId");

        // Assert
        assertNotNull(bids, "getBidsByUserId should return a non-null list even if empty");
        assertTrue(bids.isEmpty(), "getBidsByUserId should return an empty list when no bids found for user");

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, "nonexistentUserId");
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();

        // Verify resources closed
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }

    @Test
    void testGetBidsByUserId_DatabaseError() throws SQLException {
        // Arrange
        SQLException expectedException = new SQLException("Simulated query error");
        when(mockPreparedStatement.executeQuery()).thenThrow(expectedException);

        // Act
        List<Bid> bids = bidRepository.getBidsByUserId(TEST_CLIENT_ID);

        // Assert
        assertNotNull(bids, "getBidsByUserId should return a non-null list on database error");
        assertTrue(bids.isEmpty(), "getBidsByUserId should return an empty list on database error");

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, TEST_CLIENT_ID);
        verify(mockPreparedStatement, times(1)).executeQuery();

        // Verify resources closed despite error
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
        // ResultSet close is not verified as executeQuery() threw exception
    }


    @Test
    void testGetBidsByUserId_NullOrEmptyId() throws SQLException {
        // Arrange
        String nullId = null;
        String emptyId = "";
        String blankId = "   ";

        // Act
        List<Bid> bids1 = bidRepository.getBidsByUserId(nullId);
        List<Bid> bids2 = bidRepository.getBidsByUserId(emptyId);
        List<Bid> bids3 = bidRepository.getBidsByUserId(blankId);


        // Assert
        assertNotNull(bids1, "getBidsByUserId should return non-null list for null ID");
        assertTrue(bids1.isEmpty(), "getBidsByUserId should return empty list for null ID");
        assertNotNull(bids2, "getBidsByUserId should return non-null list for empty ID");
        assertTrue(bids2.isEmpty(), "getBidsByUserId should return empty list for empty ID");
        assertNotNull(bids3, "getBidsByUserId should return non-null list for blank ID");
        assertTrue(bids3.isEmpty(), "getBidsByUserId should return empty list for blank ID");


        // Verify no interaction with the database connector
        verify(mockConnector, never()).getConnection();
    }

}