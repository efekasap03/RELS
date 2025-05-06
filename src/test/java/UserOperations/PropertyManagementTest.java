package UserOperations;

import Data.connector.DatabaseConnectorImpl;
import Data.domain.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PropertyManagementTest {

    @Mock
    private DatabaseConnectorImpl mockDbConnector;
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockResultSet;

    private PropertyManagement propertyManagement;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(mockDbConnector.getConnection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        // Default behavior for connection validity, can be overridden in specific tests
        when(mockConnection.isValid(anyInt())).thenReturn(true);


        propertyManagement = new PropertyManagement(mockDbConnector);
    }

    @Test
    void getActiveProperties_shouldReturnOnlyActiveProperties() throws SQLException {
        // Arrange
        // Mock ResultSet for two properties: one active, one inactive
        when(mockResultSet.next())
                .thenReturn(true) // First property
                .thenReturn(true) // Second property
                .thenReturn(false); // No more properties

        // First property (active)
        when(mockResultSet.getString("property_id")).thenReturn("p1", "p2");
        when(mockResultSet.getString("landlord_id")).thenReturn("l1", "l1");
        when(mockResultSet.getString("address")).thenReturn("123 Main St", "456 Oak St");
        when(mockResultSet.getString("city")).thenReturn("Anytown", "Anytown");
        when(mockResultSet.getString("postal_code")).thenReturn("12345", "54321");
        when(mockResultSet.getString("property_type")).thenReturn("House", "Apartment");
        when(mockResultSet.getString("description")).thenReturn("A nice house", "A cozy apartment");
        when(mockResultSet.getBigDecimal("price"))
                .thenReturn(new BigDecimal("250000.00"), new BigDecimal("150000.00"));
        when(mockResultSet.getBigDecimal("square_footage"))
                .thenReturn(new BigDecimal("1500.00"), new BigDecimal("800.00"));
        when(mockResultSet.getInt("bedrooms")).thenReturn(3, 1);
        when(mockResultSet.getInt("bathrooms")).thenReturn(2, 1);
        // This is the key for this test: is_active is TRUE for both, as the SQL query filters this.
        // The mapResultSetToProperty method will read whatever 'is_active' column returns.
        // The SQL "SELECT * FROM properties WHERE is_active = TRUE" handles the filtering.
        // So, the ResultSet mock should reflect what mapResultSetToProperty expects for active properties.
        when(mockResultSet.getBoolean("is_active")).thenReturn(true, true);

        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        when(mockResultSet.getTimestamp("date_listed")).thenReturn(nowTimestamp, nowTimestamp);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(nowTimestamp, nowTimestamp);
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(nowTimestamp, nowTimestamp);


        // Act
        List<Property> activeProperties = propertyManagement.getActiveProperties();

        // Assert
        assertNotNull(activeProperties);
        assertEquals(2, activeProperties.size(), "Should return two properties that are presumably active as per query");

        Property prop1 = activeProperties.get(0);
        assertEquals("p1", prop1.getPropertyId());
        assertTrue(prop1.isActive(), "Property 1 should be active based on mocked ResultSet");

        Property prop2 = activeProperties.get(1);
        assertEquals("p2", prop2.getPropertyId());
        assertTrue(prop2.isActive(), "Property 2 should be active based on mocked ResultSet");

        // Verify that the correct SQL was executed (optional, but good for sanity check)
        // The SQL in getActiveProperties is "SELECT * FROM properties WHERE is_active = TRUE"
        verify(mockStatement).executeQuery("SELECT * FROM properties WHERE is_active = TRUE");
        verify(mockConnection, times(1)).close(); // Assuming try-with-resources closes connection
        verify(mockStatement, times(1)).close(); // Assuming try-with-resources closes statement
        verify(mockResultSet, times(1)).close(); // Assuming try-with-resources closes resultset
    }

    @Test
    void getActiveProperties_whenNoPropertiesFound_shouldReturnEmptyList() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false); // No properties

        // Act
        List<Property> activeProperties = propertyManagement.getActiveProperties();

        // Assert
        assertNotNull(activeProperties);
        assertTrue(activeProperties.isEmpty(), "Should return an empty list when no properties are found");
        verify(mockStatement).executeQuery("SELECT * FROM properties WHERE is_active = TRUE");
        verify(mockConnection, times(1)).close();
        verify(mockStatement, times(1)).close();
        verify(mockResultSet, times(1)).close();
    }

    @Test
    void getActiveProperties_whenDbConnectionInvalid_shouldThrowRuntimeException() throws SQLException {
        // Arrange
        when(mockDbConnector.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(anyInt())).thenReturn(false); // Simulate invalid connection

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            propertyManagement.getActiveProperties();
        });

        assertTrue(exception.getMessage().contains("Failed to fetch active properties"), "Exception message should indicate failure to fetch");
        assertTrue(exception.getCause() instanceof SQLException, "Cause should be SQLException");
        assertEquals("Database connection is invalid", exception.getCause().getMessage(), "Cause message should be specific to invalid connection");

        // Verify that getConnection was called, but executeQuery might not be if isValid check is early
        verify(mockDbConnector).getConnection();
        verify(mockConnection).isValid(anyInt()); // Check that isValid was called
        // Statement and ResultSet might not be created or closed if connection is invalid early on
        // depending on where the isValid check is within the try-with-resources block of the actual method.
        // The provided PropertyManagement.getActiveProperties has the isValid check inside the try-with-resources.
        verify(mockConnection, times(1)).close(); // Connection is closed by try-with-resources
    }

    @Test
    void getActiveProperties_whenSqlExceptionOccursDuringQuery_shouldThrowRuntimeException() throws SQLException {
        // Arrange
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("Test SQL Error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            propertyManagement.getActiveProperties();
        });
        assertTrue(exception.getMessage().contains("Failed to fetch active properties"));
        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals("Test SQL Error", exception.getCause().getMessage());

        verify(mockConnection, times(1)).close();
        verify(mockStatement, times(1)).close();
        // mockResultSet.close() won't be called if executeQuery throws
    }
} 