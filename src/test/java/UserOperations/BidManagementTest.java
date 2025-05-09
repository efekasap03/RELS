package UserOperations;

import Data.connector.DatabaseConnectorImpl;
import Data.domain.Bid;
import UserOperations.BidManagement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BidManagementTest {

    private DatabaseConnectorImpl dbConnector;
    private BidManagement bidManagement;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private Statement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    public void setup() throws Exception {
        dbConnector = mock(DatabaseConnectorImpl.class);
        bidManagement = new BidManagement(dbConnector);
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockStatement = mock(Statement.class);
        mockResultSet = mock(ResultSet.class);

        when(dbConnector.getConnection()).thenReturn(mockConnection);
    }

    @Test
    public void testCreateBid() throws Exception {
        // Set up for getNextBidId() (returns "bid11" after bid10)
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery("SELECT MAX(CAST(SUBSTRING(bid_id, 4) AS UNSIGNED)) FROM bids")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(10);  // Max bid number is 10

        // Set up for INSERT
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        doNothing().when(mockPreparedStatement).setBigDecimal(anyInt(), any(BigDecimal.class));
        doNothing().when(mockPreparedStatement).setTimestamp(anyInt(), any(Timestamp.class));
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        String bidId = bidManagement.createBid("prop1", "client6", 450000.00);

        assertEquals("bid11", bidId);
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testUpdateBidSuccess() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = bidManagement.updateBid("bid1", 460000.00);

        assertTrue(result);
        verify(mockPreparedStatement).setBigDecimal(1, BigDecimal.valueOf(460000.00));
        verify(mockPreparedStatement).setString(2, "bid1");
    }

    @Test
    public void testGetBidStatusFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("status")).thenReturn("PENDING");

        String status = bidManagement.getBidStatus("bid1");

        assertEquals("PENDING", status);
    }

    @Test
    public void testGetBidStatusNotFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        String status = bidManagement.getBidStatus("nonexistent");

        assertEquals("NOT FOUND", status);
    }

    @Test
    public void testListBidsByProperty() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, true, false); // Two rows
        when(mockResultSet.getString("bid_id")).thenReturn("bid1", "bid2");
        when(mockResultSet.getString("address")).thenReturn("123 Oak St", "123 Oak St");
        when(mockResultSet.getBigDecimal("amount")).thenReturn(new BigDecimal("440000.00"), new BigDecimal("445000.00"));
        when(mockResultSet.getString("status")).thenReturn("PENDING", "PENDING");
        when(mockResultSet.getTimestamp("bid_timestamp")).thenReturn(Timestamp.valueOf("2023-01-20 10:30:00"), Timestamp.valueOf("2023-01-21 11:15:00"));

        List<String> bids = bidManagement.listBidsByProperty("prop1");

        assertEquals(2, bids.size());
        assertTrue(bids.get(0).contains("bid1") || bids.get(1).contains("bid1"));
    }

    @Test
    public void testUpdateBidStatusSuccess() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = bidManagement.updateBidStatus("bid1", "ACCEPTED", "land1");

        assertTrue(result);
    }

    @Test
    public void testUpdateBidStatusFailsIfNoRowAffected() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bidManagement.updateBidStatus("bidX", "ACCEPTED", "landX"));

        assertTrue(ex.getMessage().contains("Bid not found"));
    }
}
