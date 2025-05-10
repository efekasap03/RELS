package UserOperations;

import Data.connector.DatabaseConnectorImpl;
import Data.domain.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
    @Mock
    private PreparedStatement mockPreparedStatement;

    private PropertyManagement propertyManagement;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(mockDbConnector.getConnection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockConnection.isValid(anyInt())).thenReturn(true);

        propertyManagement = new PropertyManagement(mockDbConnector);
    }

    @Test
    void getActiveProperties_shouldReturnOnlyActiveProperties() throws SQLException {
        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

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
        when(mockResultSet.getBoolean("is_active")).thenReturn(true, true);

        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        when(mockResultSet.getTimestamp("date_listed")).thenReturn(nowTimestamp, nowTimestamp);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(nowTimestamp, nowTimestamp);
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(nowTimestamp, nowTimestamp);

        List<Property> activeProperties = propertyManagement.getActiveProperties();

        assertNotNull(activeProperties);
        assertEquals(2, activeProperties.size(), "Should return two properties that are presumably active as per query");

        Property prop1 = activeProperties.get(0);
        assertEquals("p1", prop1.getPropertyId());
        assertTrue(prop1.isActive(), "Property 1 should be active based on mocked ResultSet");

        Property prop2 = activeProperties.get(1);
        assertEquals("p2", prop2.getPropertyId());
        assertTrue(prop2.isActive(), "Property 2 should be active based on mocked ResultSet");

        verify(mockStatement).executeQuery("SELECT * FROM properties WHERE is_active = TRUE");
        verify(mockConnection, times(1)).close();
        verify(mockStatement, times(1)).close();
        verify(mockResultSet, times(1)).close();
    }

    @Test
    void getActiveProperties_whenNoPropertiesFound_shouldReturnEmptyList() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);

        List<Property> activeProperties = propertyManagement.getActiveProperties();

        assertNotNull(activeProperties);
        assertTrue(activeProperties.isEmpty(), "Should return an empty list when no properties are found");
        verify(mockStatement).executeQuery("SELECT * FROM properties WHERE is_active = TRUE");
        verify(mockConnection, times(1)).close();
        verify(mockStatement, times(1)).close();
        verify(mockResultSet, times(1)).close();
    }

    @Test
    void getActiveProperties_whenDbConnectionInvalid_shouldThrowRuntimeException() throws SQLException {
        when(mockDbConnector.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(anyInt())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            propertyManagement.getActiveProperties();
        });

        assertTrue(exception.getMessage().contains("Failed to fetch active properties"), "Exception message should indicate failure to fetch");
        assertTrue(exception.getCause() instanceof SQLException, "Cause should be SQLException");
        assertEquals("Database connection is invalid", exception.getCause().getMessage(), "Cause message should be specific to invalid connection");

        verify(mockDbConnector).getConnection();
        verify(mockConnection).isValid(anyInt());
        verify(mockConnection, times(1)).close();
    }

    @Test
    void getActiveProperties_whenSqlExceptionOccursDuringQuery_shouldThrowRuntimeException() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("Test SQL Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            propertyManagement.getActiveProperties();
        });
        assertTrue(exception.getMessage().contains("Failed to fetch active properties"));
        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals("Test SQL Error", exception.getCause().getMessage());

        verify(mockConnection, times(1)).close();
        verify(mockStatement, times(1)).close();
    }

    @Test
    void addProperty_shouldSuccessfullyAddProperty() throws SQLException {
        Property property = new Property();
        property.setPropertyId("p1");
        property.setLandlordId("l1");
        property.setAddress("1 Test St");
        property.setCity("Testville");
        property.setPostalCode("12345");
        property.setPropertyType("House");
        property.setDescription("A test property");
        property.setPrice(new BigDecimal("100000"));
        property.setSquareFootage(new BigDecimal("1200"));
        property.setBedrooms(3);
        property.setBathrooms(2);
        property.setActive(true);

        propertyManagement.addProperty(property);

        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setString(1, "p1");
        verify(mockPreparedStatement).setString(2, "l1");
        verify(mockPreparedStatement).setString(3, "1 Test St");
        verify(mockPreparedStatement).setString(4, "Testville");
        verify(mockPreparedStatement).setString(5, "12345");
        verify(mockPreparedStatement).setString(6, "House");
        verify(mockPreparedStatement).setString(7, "A test property");
        verify(mockPreparedStatement).setBigDecimal(8, new BigDecimal("100000"));
        verify(mockPreparedStatement).setBigDecimal(9, new BigDecimal("1200"));
        verify(mockPreparedStatement).setInt(10, 3);
        verify(mockPreparedStatement).setInt(11, 2);
        verify(mockPreparedStatement).setBoolean(12, true);
        verify(mockPreparedStatement).executeUpdate();
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
    }

    @Test
    void addProperty_whenSqlExceptionOccurs_shouldThrowRuntimeException() throws SQLException {
        // Arrange
        Property property = new Property(); // Fully initialize to avoid NPE
        property.setPropertyId("p-sql-ex");
        property.setLandlordId("l-sql-ex");
        property.setAddress("1 Error St");
        property.setCity("ExceptionVille");
        property.setPostalCode("00000");
        property.setPropertyType("HauntedHouse");
        property.setDescription("A property that causes SQL errors");
        property.setPrice(new BigDecimal("666"));
        property.setSquareFootage(new BigDecimal("666"));
        property.setBedrooms(0);
        property.setBathrooms(0);
        property.setActive(false);

        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("SQL error on add"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            propertyManagement.addProperty(property);
        });
        assertTrue(exception.getMessage().contains("Failed to add property"));
        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals("SQL error on add", exception.getCause().getMessage());

        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
    }

    @Test
    void editProperty_shouldSuccessfullyUpdateProperty() throws SQLException {
        // Arrange
        Property property = new Property();
        property.setPropertyId("p1");
        property.setLandlordId("l1"); // Assuming landlordId is also part of the property object internally or needed
        property.setAddress("2 Updated St");
        property.setCity("UpdatedCity");
        property.setPostalCode("54321");
        property.setPropertyType("Apartment");
        property.setDescription("An updated property");
        property.setPrice(new BigDecimal("150000.00"));
        property.setSquareFootage(new BigDecimal("900.00"));
        property.setBedrooms(2);
        property.setBathrooms(1);
        property.setActive(true);
        // Ensure dateListed, createdAt, updatedAt are handled if mapResultSetToProperty or other logic uses them
        // For edit, updated_at is set by SQL, date_listed and created_at would be from existing record typically.

        String landlordId = "l1"; // This is the landlordId used for the WHERE clause condition

        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Simulate 1 row affected

        // Act
        propertyManagement.editProperty(property, landlordId);

        // Assert
        verify(mockConnection).prepareStatement(argThat(sql -> sql.startsWith("UPDATE properties SET")));
        verify(mockPreparedStatement).setString(1, property.getAddress());
        verify(mockPreparedStatement).setString(2, property.getCity());
        verify(mockPreparedStatement).setString(3, property.getPostalCode());
        verify(mockPreparedStatement).setString(4, property.getPropertyType());
        verify(mockPreparedStatement).setString(5, property.getDescription());
        verify(mockPreparedStatement).setBigDecimal(6, property.getPrice());
        verify(mockPreparedStatement).setBigDecimal(7, property.getSquareFootage());
        verify(mockPreparedStatement).setInt(8, property.getBedrooms());
        verify(mockPreparedStatement).setInt(9, property.getBathrooms());
        verify(mockPreparedStatement).setBoolean(10, property.isActive());
        verify(mockPreparedStatement).setString(11, property.getPropertyId());
        verify(mockPreparedStatement).setString(12, landlordId);
        verify(mockPreparedStatement).executeUpdate();
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
    }

    @Test
    void editProperty_whenPropertyNotFoundOrNotOwned_shouldThrowRuntimeException() throws SQLException {
        // Arrange
        Property property = new Property(); // Must be fully initialized to avoid NPE before intended exception
        property.setPropertyId("p-nonexistent");
        property.setAddress("1 Main St"); // Dummy value
        property.setCity("Anytown");       // Dummy value
        property.setPostalCode("12345");  // Dummy value
        property.setPropertyType("House");   // Dummy value
        property.setDescription("Desc");    // Dummy value
        property.setPrice(BigDecimal.ONE); // Dummy value
        property.setSquareFootage(BigDecimal.TEN); // Dummy value
        property.setBedrooms(1);           // Dummy value
        property.setBathrooms(1);          // Dummy value
        property.setActive(true);          // Dummy value

        String landlordId = "l1";
        when(mockPreparedStatement.executeUpdate()).thenReturn(0); // Simulate 0 rows affected

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            propertyManagement.editProperty(property, landlordId);
        });
        assertEquals("Property not Edited", exception.getMessage());

        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
    }

    @Test
    void editProperty_whenSqlExceptionOccurs_shouldThrowRuntimeException() throws SQLException {
        // Arrange
        Property property = new Property(); // Fully initialize to avoid NPE before intended SQL exception
        property.setPropertyId("p1");
        property.setAddress("1 Main St");
        property.setCity("Anytown");
        property.setPostalCode("12345");
        property.setPropertyType("House");
        property.setDescription("A property");
        property.setPrice(new BigDecimal("100"));
        property.setSquareFootage(new BigDecimal("100"));
        property.setBedrooms(1);
        property.setBathrooms(1);
        property.setActive(true);

        String landlordId = "l1";
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("SQL error on edit"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            propertyManagement.editProperty(property, landlordId);
        });
        assertTrue(exception.getMessage().contains("Failed to update property"));
        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals("SQL error on edit", exception.getCause().getMessage());
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
    }

    @Test
    void deactivateProperty_shouldSuccessfullyDeactivate() throws SQLException {
        String propertyId = "p1";
        String landlordId = "l1";
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        propertyManagement.deactivateProperty(propertyId, landlordId);

        verify(mockConnection).prepareStatement(argThat(sql -> sql.startsWith("UPDATE properties SET is_active = FALSE")));
        verify(mockPreparedStatement).setString(1, propertyId);
        verify(mockPreparedStatement).setString(2, landlordId);
        verify(mockPreparedStatement).executeUpdate();
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
    }

    @Test
    void deactivateProperty_whenPropertyNotFoundOrNotOwned_shouldThrowRuntimeException() throws SQLException {
        String propertyId = "p-nonexistent";
        String landlordId = "l-other";
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Simulated error for not found/owned"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            propertyManagement.deactivateProperty(propertyId, landlordId);
        });
        assertEquals("Failed to deactivate property as it was not found or does not belong to you", exception.getMessage());
        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals("Simulated error for not found/owned", exception.getCause().getMessage());
        
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
    }

    @Test
    void getProperties_shouldReturnProperties() throws SQLException {
        when(mockResultSet.next())
                .thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockResultSet.getString("property_id")).thenReturn("p1", "p2");
        when(mockResultSet.getBoolean("is_active")).thenReturn(true, true);
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        when(mockResultSet.getTimestamp("date_listed")).thenReturn(nowTimestamp, nowTimestamp);

        List<Property> properties = propertyManagement.getProperties();

        assertNotNull(properties);
        assertEquals(2, properties.size());
        verify(mockStatement).executeQuery("SELECT * FROM properties WHERE is_active = TRUE");
        verify(mockConnection, times(1)).close();
        verify(mockStatement, times(1)).close();
        verify(mockResultSet, times(1)).close();
    }

    @Test
    void getProperties_whenNoPropertiesFound_shouldReturnEmptyList() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);

        List<Property> properties = propertyManagement.getProperties();

        assertNotNull(properties);
        assertTrue(properties.isEmpty());
        verify(mockStatement).executeQuery("SELECT * FROM properties WHERE is_active = TRUE");
        verify(mockConnection, times(1)).close();
        verify(mockStatement, times(1)).close();
        verify(mockResultSet, times(1)).close();
    }

    @Test
    void getProperties_whenSqlExceptionOccurs_shouldThrowRuntimeException() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("SQL error on getProperties"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            propertyManagement.getProperties();
        });
        assertTrue(exception.getMessage().contains("Failed to fetch properties"));
        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals("SQL error on getProperties", exception.getCause().getMessage());
        verify(mockConnection, times(1)).close();
        verify(mockStatement, times(1)).close();
    }

    @Test
    void getPropertiesByLandlord_shouldReturnPropertiesForLandlord() throws SQLException {
        String landlordId = "l1";
        when(mockResultSet.next())
                .thenReturn(true).thenReturn(false);
        when(mockResultSet.getString("property_id")).thenReturn("p1");
        when(mockResultSet.getString("landlord_id")).thenReturn(landlordId);
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        when(mockResultSet.getTimestamp("date_listed")).thenReturn(nowTimestamp);
        when(mockConnection.prepareStatement(argThat(sql -> sql.contains("WHERE landlord_id = ?")))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<Property> properties = propertyManagement.getPropertiesByLandlord(landlordId);

        assertNotNull(properties);
        assertEquals(1, properties.size());
        assertEquals("p1", properties.get(0).getPropertyId());
        assertEquals(landlordId, properties.get(0).getLandlordId());
        verify(mockPreparedStatement).setString(1, landlordId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockResultSet, times(1)).close();
    }

    @Test
    void getPropertiesByLandlord_whenNoPropertiesFound_shouldReturnEmptyList() throws SQLException {
        String landlordId = "l-nonexistent";
        when(mockResultSet.next()).thenReturn(false);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<Property> properties = propertyManagement.getPropertiesByLandlord(landlordId);

        assertNotNull(properties);
        assertTrue(properties.isEmpty());
        verify(mockPreparedStatement).setString(1, landlordId);
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockResultSet, times(1)).close();
    }

    @Test
    void getPropertiesByLandlord_whenSqlExceptionOccurs_shouldThrowRuntimeException() throws SQLException {
        String landlordId = "l1";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("SQL error on getByLandlord"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            propertyManagement.getPropertiesByLandlord(landlordId);
        });
        assertTrue(exception.getMessage().contains("Failed to fetch properties by landlord"));
        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals("SQL error on getByLandlord", exception.getCause().getMessage());
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
    }

    @Test
    void searchProperties_withAllParameters_shouldReturnMatchingProperties() throws SQLException {
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSet.getString("property_id")).thenReturn("search-p1");
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        when(mockResultSet.getTimestamp("date_listed")).thenReturn(nowTimestamp);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<Property> properties = propertyManagement.searchProperties("House", 100000.0, 200000.0, "TestCity");

        assertNotNull(properties);
        assertEquals(1, properties.size());
        assertEquals("search-p1", properties.get(0).getPropertyId());
        verify(mockPreparedStatement).setString(1, "House");
        verify(mockPreparedStatement).setDouble(2, 100000.0);
        verify(mockPreparedStatement).setDouble(3, 200000.0);
        verify(mockPreparedStatement).setString(4, "%TestCity%");
        verify(mockPreparedStatement).setString(5, "%TestCity%");
        verify(mockPreparedStatement).executeQuery();
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockResultSet, times(1)).close();
    }

    @Test
    void searchProperties_withNoParameters_shouldReturnAllActiveProperties() throws SQLException {
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockResultSet.getString("property_id")).thenReturn("p1", "p2");
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        when(mockResultSet.getTimestamp("date_listed")).thenReturn(nowTimestamp, nowTimestamp);

        when(mockConnection.prepareStatement(argThat(sql -> sql.endsWith("WHERE is_active = TRUE")))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<Property> properties = propertyManagement.searchProperties(null, null, null, null);

        assertNotNull(properties);
        assertEquals(2, properties.size());
        verify(mockPreparedStatement, never()).setString(anyInt(), anyString());
        verify(mockPreparedStatement, never()).setDouble(anyInt(), anyDouble());
        verify(mockPreparedStatement).executeQuery();
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockResultSet, times(1)).close();
    }
    
    @Test
    void searchProperties_whenNoResultsMatch_shouldReturnEmptyList() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<Property> properties = propertyManagement.searchProperties("Condo", 500000.0, 600000.0, "NoWhereVille");

        assertNotNull(properties);
        assertTrue(properties.isEmpty());
        verify(mockPreparedStatement).executeQuery();
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockResultSet, times(1)).close();
    }

    @Test
    void searchProperties_whenSqlExceptionOccurs_shouldThrowRuntimeException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("SQL error on search"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            propertyManagement.searchProperties("House", null, null, "Anytown");
        });
        assertTrue(exception.getMessage().contains("Failed to search properties"));
        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals("SQL error on search", exception.getCause().getMessage());
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
    }
} 