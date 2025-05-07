package Data.repository.impl;

import Data.connector.IDatabaseConnector;
import Data.domain.Filter;
import Data.domain.Property;
import Data.repository.interfaces.IPropertyRepository; // Assuming this interface exists

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*; // Import Mockito methods

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Comprehensive Unit tests for PropertyRepositoryImpl using Mockito to mock the database connector and JDBC objects.
class PropertyRepositoryImplTest {

    // Mock objects
    private IDatabaseConnector mockConnector;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    // The repository instance under test
    private IPropertyRepository propertyRepository;

    // Test data
    private static final String TEST_PROPERTY_ID = "prop123";
    private static final String TEST_LANDLORD_ID = "landlord456";
    private static final String TEST_ADDRESS = "123 Test St";
    private static final String TEST_CITY = "Testville";
    private static final String TEST_POSTAL_CODE = "T1E 1S1";
    private static final String TEST_PROPERTY_TYPE = "House";
    private static final String TEST_DESCRIPTION = "A lovely test house";
    private static final BigDecimal TEST_PRICE = new BigDecimal("500000.00");
    private static final BigDecimal TEST_SQUARE_FOOTAGE = new BigDecimal("1500.50");
    private static final Integer TEST_BEDROOMS = 3;
    private static final Integer TEST_BATHROOMS = 2;
    private static final boolean TEST_IS_ACTIVE = true;
    private static final LocalDateTime TEST_DATE_LISTED = LocalDateTime.of(2023, 5, 10, 9, 0);
    private static final LocalDateTime TEST_CREATED_AT = LocalDateTime.of(2023, 5, 9, 10, 0);
    private static final LocalDateTime TEST_UPDATED_AT = LocalDateTime.of(2023, 5, 9, 10, 0);


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

        // Configure the mock prepared statement to return the mock result set for queries by default
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Configure the mock prepared statement to return a non-zero value for updates by default
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);


        // Instantiate the repository with the mock connector
        propertyRepository = new PropertyRepositoryImpl(mockConnector);
    }

    // Helper method to create a test Property object
    private Property createTestProperty() {
        Property property = new Property();
        property.setPropertyId(TEST_PROPERTY_ID);
        property.setLandlordId(TEST_LANDLORD_ID);
        property.setAddress(TEST_ADDRESS);
        property.setCity(TEST_CITY);
        property.setPostalCode(TEST_POSTAL_CODE);
        property.setPropertyType(TEST_PROPERTY_TYPE);
        property.setDescription(TEST_DESCRIPTION);
        property.setPrice(TEST_PRICE);
        property.setSquareFootage(TEST_SQUARE_FOOTAGE);
        property.setBedrooms(TEST_BEDROOMS);
        property.setBathrooms(TEST_BATHROOMS);
        property.setActive(TEST_IS_ACTIVE);
        property.setDateListed(TEST_DATE_LISTED);
        // createdAt and updatedAt are typically set by the DB, but we include them for mapping tests
        property.setCreatedAt(TEST_CREATED_AT);
        property.setUpdatedAt(TEST_UPDATED_AT);
        return property;
    }

    // Helper method to configure the mock ResultSet to return a specific property row
    private void configureMockResultSetForSingleProperty(Property p) throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false); // One row, then done
        when(mockResultSet.getString("property_id")).thenReturn(p.getPropertyId());
        when(mockResultSet.getString("landlord_id")).thenReturn(p.getLandlordId());
        when(mockResultSet.getString("address")).thenReturn(p.getAddress());
        when(mockResultSet.getString("city")).thenReturn(p.getCity());
        when(mockResultSet.getString("postal_code")).thenReturn(p.getPostalCode());
        when(mockResultSet.getString("property_type")).thenReturn(p.getPropertyType());
        when(mockResultSet.getString("description")).thenReturn(p.getDescription());

        // Use getBigDecimal for price
        when(mockResultSet.getBigDecimal("price")).thenReturn(p.getPrice());

        // Handle nullable BigDecimal (Square Footage) - mock value and wasNull status
        when(mockResultSet.getBigDecimal("square_footage")).thenReturn(p.getSquareFootage());
        // The SUT's mapRowToProperty does NOT use a helper that calls wasNull() after getBigDecimal directly.
        // If you add a getNullableBigDecimal helper for *getting* that calls wasNull,
        // you would need to add a verify(mockResultSet, times(1)).wasNull() here IF the value could be null.
        // Based on current SUT, wasNull is not called after getBigDecimal in mapping.


        // Handle nullable Integer (Bedrooms) - mock value and wasNull status
        // getInt returns 0 for null, so we return 0 if value is null
        when(mockResultSet.getInt("bedrooms")).thenReturn(p.getBedrooms() != null ? p.getBedrooms() : 0);
        // wasNull is called after getInt in the SUT's getNullableInteger helper - 1st call to wasNull() per row
        when(mockResultSet.wasNull()).thenReturn(p.getBedrooms() == null);


        // Handle nullable Integer (Bathrooms) - mock value and wasNull status
        // getInt returns 0 for null, so we return 0 if value is null
        when(mockResultSet.getInt("bathrooms")).thenReturn(p.getBathrooms() != null ? p.getBathrooms() : 0);
        // wasNull is called after getInt in the SUT's getNullableInteger helper - 2nd call to wasNull() per row
        when(mockResultSet.wasNull()).thenReturn(p.getBathrooms() == null);


        when(mockResultSet.getBoolean("is_active")).thenReturn(p.isActive());

        // Handle nullable Timestamp (Date Listed) - mock value
        when(mockResultSet.getTimestamp("date_listed")).thenReturn(p.getDateListed() != null ? Timestamp.valueOf(p.getDateListed()) : null);
        // Note: SUT's getNullableTimestamp does NOT call wasNull(), so no need to mock wasNull after getTimestamp


        // Handle non-nullable Timestamps - must return non-null
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(p.getCreatedAt()));
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(p.getUpdatedAt()));

    }


    // --- Constructor Tests ---

    @Test
    void testConstructor_ValidConnector() {
        // Arrange, Act (done in @BeforeEach)
        // Assert
        assertNotNull(propertyRepository, "Repository instance should be created");
        // We can't directly assert the private 'connector' field, but successful creation implies it was assigned.
    }

    @Test
    void testConstructor_NullConnectorThrowsException() {
        // Arrange, Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PropertyRepositoryImpl(null);
        }, "Constructor should throw IllegalArgumentException for null connector");

        assertTrue(exception.getMessage().contains("Database connector cannot be null."),
                "Exception message should indicate null connector");
    }

    // --- addProperty Tests ---

    @Test
    void testAddProperty_Success() throws SQLException {
        // Arrange
        Property property = createTestProperty();
        // Ensure nullable fields are non-null for this specific test
        property.setSquareFootage(TEST_SQUARE_FOOTAGE);
        property.setBedrooms(TEST_BEDROOMS);
        property.setBathrooms(TEST_BATHROOMS);
        property.setDateListed(TEST_DATE_LISTED);


        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = propertyRepository.addProperty(property);

        // Assert
        assertTrue(result, "addProperty should return true on successful insertion");

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString()); // Verify prepareStatement with any string
        verify(mockPreparedStatement, times(1)).setString(1, property.getPropertyId());
        verify(mockPreparedStatement, times(1)).setString(2, property.getLandlordId());
        verify(mockPreparedStatement, times(1)).setString(3, property.getAddress());
        verify(mockPreparedStatement, times(1)).setString(4, property.getCity());
        verify(mockPreparedStatement, times(1)).setString(5, property.getPostalCode());
        verify(mockPreparedStatement, times(1)).setString(6, property.getPropertyType());
        verify(mockPreparedStatement, times(1)).setString(7, property.getDescription());
        verify(mockPreparedStatement, times(1)).setBigDecimal(8, property.getPrice());
        verify(mockPreparedStatement, times(1)).setBigDecimal(9, property.getSquareFootage()); // Non-null nullable
        verify(mockPreparedStatement, times(1)).setInt(10, property.getBedrooms()); // Non-null nullable
        verify(mockPreparedStatement, times(1)).setInt(11, property.getBathrooms()); // Non-null nullable
        verify(mockPreparedStatement, times(1)).setBoolean(12, property.isActive());
        verify(mockPreparedStatement, times(1)).setTimestamp(eq(13), any(Timestamp.class)); // Non-null nullable

        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify resources closed
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet); // Not used in executeUpdate paths
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    void testAddProperty_Success_WithNullNullableFields() throws SQLException {
        // Arrange
        Property property = createTestProperty();
        // Explicitly set nullable fields to null
        property.setSquareFootage(null);
        property.setBedrooms(null);
        property.setBathrooms(null);

        // Fix #1: Cast null to LocalDateTime to resolve ambiguity
        property.setDateListed((LocalDateTime) null);


        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = propertyRepository.addProperty(property);

        // Assert
        assertTrue(result, "addProperty should return true on successful insertion with null nullable fields");

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, property.getPropertyId());
        verify(mockPreparedStatement, times(1)).setString(2, property.getLandlordId());
        verify(mockPreparedStatement, times(1)).setString(3, property.getAddress());
        verify(mockPreparedStatement, times(1)).setString(4, property.getCity());
        verify(mockPreparedStatement, times(1)).setString(5, property.getPostalCode());
        verify(mockPreparedStatement, times(1)).setString(6, property.getPropertyType());
        verify(mockPreparedStatement, times(1)).setString(7, property.getDescription());
        verify(mockPreparedStatement, times(1)).setBigDecimal(8, property.getPrice());
        verify(mockPreparedStatement, times(1)).setNull(9, Types.DECIMAL); // Nullable is null
        verify(mockPreparedStatement, times(1)).setNull(10, Types.INTEGER); // Nullable is null
        verify(mockPreparedStatement, times(1)).setNull(11, Types.INTEGER); // Nullable is null
        verify(mockPreparedStatement, times(1)).setBoolean(12, property.isActive());
        verify(mockPreparedStatement, times(1)).setNull(13, Types.TIMESTAMP); // Nullable is null

        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify resources closed
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet); // Not used
        verifyNoMoreInteractions(mockConnection);
    }


    @Test
    void testAddProperty_DatabaseError() throws SQLException {
        // Arrange
        Property property = createTestProperty();
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Simulated DB error"));

        // Act
        boolean result = propertyRepository.addProperty(property);

        // Assert
        assertFalse(result, "addProperty should return false on database error");

        // Verify interactions up to the point of error
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        // Verify all parameters were attempted to be set before executeUpdate failed
        verify(mockPreparedStatement, times(1)).setString(1, property.getPropertyId());
        verify(mockPreparedStatement, times(1)).setString(2, property.getLandlordId());
        verify(mockPreparedStatement, times(1)).setString(3, property.getAddress());
        verify(mockPreparedStatement, times(1)).setString(4, property.getCity());
        verify(mockPreparedStatement, times(1)).setString(5, property.getPostalCode());
        verify(mockPreparedStatement, times(1)).setString(6, property.getPropertyType());
        verify(mockPreparedStatement, times(1)).setString(7, property.getDescription());
        verify(mockPreparedStatement, times(1)).setBigDecimal(8, property.getPrice());
        verify(mockPreparedStatement, times(1)).setBigDecimal(9, property.getSquareFootage());
        verify(mockPreparedStatement, times(1)).setInt(10, property.getBedrooms());
        verify(mockPreparedStatement, times(1)).setInt(11, property.getBathrooms());
        verify(mockPreparedStatement, times(1)).setBoolean(12, property.isActive());
        verify(mockPreparedStatement, times(1)).setTimestamp(eq(13), any(Timestamp.class));

        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify resources closed despite error (try-with-resources)
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
        // ResultSet close is not verified in executeUpdate paths

        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet); // Not used
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    void testAddProperty_NullPropertyObject() throws SQLException {
        // Arrange
        Property nullProperty = null;

        // Act
        boolean result = propertyRepository.addProperty(nullProperty);

        // Assert
        assertFalse(result, "addProperty should return false for null property object");
        verify(mockConnector, never()).getConnection(); // Verify no interaction with DB

        verifyNoMoreInteractions(mockPreparedStatement); // Never used
        verifyNoMoreInteractions(mockResultSet); // Never used
        verifyNoMoreInteractions(mockConnection); // Never used
    }

    @Test
    void testAddProperty_PropertyWithNullRequiredFields() throws SQLException {
        // Arrange
        Property propertyWithNulls = new Property();
        propertyWithNulls.setPropertyId(null); // Make required field null
        propertyWithNulls.setLandlordId(TEST_LANDLORD_ID);

        // Act
        boolean result = propertyRepository.addProperty(propertyWithNulls);

        // Assert
        assertFalse(result, "addProperty should return false for property with null required fields");
        verify(mockConnector, never()).getConnection(); // Verify no interaction with DB

        verifyNoMoreInteractions(mockPreparedStatement); // Never used
        verifyNoMoreInteractions(mockResultSet); // Never used
        verifyNoMoreInteractions(mockConnection); // Never used
    }


    // --- getPropertyById Tests ---

    @Test
    void testGetPropertyById_Found() throws SQLException {
        // Arrange
        Property expectedProperty = createTestProperty();
        configureMockResultSetForSingleProperty(expectedProperty); // Helper mocks ResultSet.next() and get* calls


        // Act
        Property foundProperty = propertyRepository.getPropertyById(TEST_PROPERTY_ID);

        // Assert
        assertNotNull(foundProperty, "getPropertyById should return a Property object when found");
        // Verify mapping worked by checking fields (using .equals on Property if implemented, or field-by-field)
        assertEquals(expectedProperty.getPropertyId(), foundProperty.getPropertyId());
        assertEquals(expectedProperty.getLandlordId(), foundProperty.getLandlordId()); // Added verification
        assertEquals(expectedProperty.getAddress(), foundProperty.getAddress());
        assertEquals(expectedProperty.getCity(), foundProperty.getCity()); // Added verification
        assertEquals(expectedProperty.getPostalCode(), foundProperty.getPostalCode()); // Added verification
        assertEquals(expectedProperty.getPropertyType(), foundProperty.getPropertyType()); // Added verification
        assertEquals(expectedProperty.getDescription(), foundProperty.getDescription()); // Added verification
        assertEquals(expectedProperty.getPrice(), foundProperty.getPrice());
        assertEquals(expectedProperty.getSquareFootage(), foundProperty.getSquareFootage()); // Check nullable bigdecimal mapping
        assertEquals(expectedProperty.getBedrooms(), foundProperty.getBedrooms()); // Check nullable integer mapping
        assertEquals(expectedProperty.getBathrooms(), foundProperty.getBathrooms()); // Check nullable integer mapping
        assertEquals(expectedProperty.isActive(), foundProperty.isActive());
        assertEquals(expectedProperty.getDateListed(), foundProperty.getDateListed()); // Check nullable timestamp mapping
        assertEquals(expectedProperty.getCreatedAt(), foundProperty.getCreatedAt()); // Non-null timestamp mapping
        assertEquals(expectedProperty.getUpdatedAt(), foundProperty.getUpdatedAt()); // Non-null timestamp mapping


        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString()); // Verify preparesStatement
        verify(mockPreparedStatement, times(1)).setString(1, TEST_PROPERTY_ID);
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next(); // Only called once for single result

        // Verify data was read from ResultSet for mapping (called once per field for the single row)
        verify(mockResultSet, times(1)).getString("property_id");
        verify(mockResultSet, times(1)).getString("landlord_id");
        verify(mockResultSet, times(1)).getString("address");
        verify(mockResultSet, times(1)).getString("city");
        verify(mockResultSet, times(1)).getString("postal_code");
        verify(mockResultSet, times(1)).getString("property_type");
        verify(mockResultSet, times(1)).getString("description");
        verify(mockResultSet, times(1)).getBigDecimal("price");
        verify(mockResultSet, times(1)).getBigDecimal("square_footage");
        verify(mockResultSet, times(1)).getInt("bedrooms");
        verify(mockResultSet, times(1)).getInt("bathrooms");
        // Fix #4: Verify wasNull() is called 2 times per row mapped (after getInt for bedrooms and bathrooms)
        verify(mockResultSet, times(2)).wasNull();
        verify(mockResultSet, times(1)).getBoolean("is_active");
        verify(mockResultSet, times(1)).getTimestamp("date_listed");
        verify(mockResultSet, times(1)).getTimestamp("created_at");
        verify(mockResultSet, times(1)).getTimestamp("updated_at");


        // Verify resources closed
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        // Fix #4: Move verifyNoMoreInteractions to the end
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet);
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    void testGetPropertyById_NotFound() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false); // No rows

        // Act
        Property foundProperty = propertyRepository.getPropertyById("nonexistentId");

        // Assert
        assertNull(foundProperty, "getPropertyById should return null when not found");

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, "nonexistentId");
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next(); // Only called once

        // Verify resources closed
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        // Fix #4: Move verifyNoMoreInteractions to the end
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet);
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    void testGetPropertyById_DatabaseError() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Simulated DB error"));

        // Act
        Property foundProperty = propertyRepository.getPropertyById(TEST_PROPERTY_ID);

        // Assert
        assertNull(foundProperty, "getPropertyById should return null on database error");

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, TEST_PROPERTY_ID);
        verify(mockPreparedStatement, times(1)).executeQuery();

        // Verify resources closed despite error
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
        // ResultSet close is not verified as executeQuery() threw before it was returned

        // Fix #4: Move verifyNoMoreInteractions to the end
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet); // Never used
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    void testGetPropertyById_NullOrEmptyId() throws SQLException {
        // Arrange
        String nullId = null;
        String emptyId = "";
        String blankId = "   ";

        // Act
        Property prop1 = propertyRepository.getPropertyById(nullId);
        Property prop2 = propertyRepository.getPropertyById(emptyId);
        Property prop3 = propertyRepository.getPropertyById(blankId);

        // Assert
        assertNull(prop1, "getPropertyById should return null for null ID");
        assertNull(prop2, "getPropertyById should return null for empty ID");
        assertNull(prop3, "getPropertyById should return null for blank ID");

        verify(mockConnector, never()).getConnection(); // Verify no interaction with DB

        // Fix #4: Move verifyNoMoreInteractions to the end
        verifyNoMoreInteractions(mockPreparedStatement); // Never used
        verifyNoMoreInteractions(mockResultSet); // Never used
        verifyNoMoreInteractions(mockConnection); // Never used
    }


    // --- updateProperty Tests ---

    @Test
    void testUpdateProperty_Success() throws SQLException {
        // Arrange
        Property propertyToUpdate = createTestProperty();
        propertyToUpdate.setAddress("Updated Address"); // Simulate changes
        propertyToUpdate.setPrice(new BigDecimal("600000.00"));
        propertyToUpdate.setActive(false);
        propertyToUpdate.setBedrooms(null); // Simulate setting nullable to null


        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = propertyRepository.updateProperty(propertyToUpdate);

        // Assert
        assertTrue(result, "updateProperty should return true on successful update");

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString()); // Verify prepareStatement
        verify(mockPreparedStatement, times(1)).setString(1, propertyToUpdate.getLandlordId());
        verify(mockPreparedStatement, times(1)).setString(2, "Updated Address");
        verify(mockPreparedStatement, times(1)).setString(3, propertyToUpdate.getCity());
        verify(mockPreparedStatement, times(1)).setString(4, propertyToUpdate.getPostalCode());
        verify(mockPreparedStatement, times(1)).setString(5, propertyToUpdate.getPropertyType());
        verify(mockPreparedStatement, times(1)).setString(6, propertyToUpdate.getDescription());
        verify(mockPreparedStatement, times(1)).setBigDecimal(7, new BigDecimal("600000.00"));
        verify(mockPreparedStatement, times(1)).setBigDecimal(8, propertyToUpdate.getSquareFootage()); // Non-null nullable
        verify(mockPreparedStatement, times(1)).setNull(9, Types.INTEGER); // Nullable is null
        verify(mockPreparedStatement, times(1)).setInt(10, propertyToUpdate.getBathrooms()); // Non-null nullable
        verify(mockPreparedStatement, times(1)).setBoolean(11, false);
        verify(mockPreparedStatement, times(1)).setTimestamp(eq(12), any(Timestamp.class)); // Non-null nullable
        verify(mockPreparedStatement, times(1)).setString(13, propertyToUpdate.getPropertyId()); // WHERE clause ID

        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify resources closed
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        // Fix #4: Move verifyNoMoreInteractions to the end
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet); // Never used in executeUpdate paths
        verifyNoMoreInteractions(mockConnection);
    }


    @Test
    void testUpdateProperty_NoRowsAffected() throws SQLException {
        // Arrange
        Property propertyToUpdate = createTestProperty();
        propertyToUpdate.setPropertyId("nonexistentId"); // Simulate updating non-existent


        when(mockPreparedStatement.executeUpdate()).thenReturn(0); // 0 rows affected

        // Act
        boolean result = propertyRepository.updateProperty(propertyToUpdate);

        // Assert
        assertFalse(result, "updateProperty should return false if no rows were affected");

        // Verify interactions up to executeUpdate
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        // Verify parameters were set
        verify(mockPreparedStatement, times(1)).setString(1, propertyToUpdate.getLandlordId());
        verify(mockPreparedStatement, times(1)).setString(2, propertyToUpdate.getAddress());
        verify(mockPreparedStatement, times(1)).setString(3, propertyToUpdate.getCity());
        verify(mockPreparedStatement, times(1)).setString(4, propertyToUpdate.getPostalCode());
        verify(mockPreparedStatement, times(1)).setString(5, propertyToUpdate.getPropertyType());
        verify(mockPreparedStatement, times(1)).setString(6, propertyToUpdate.getDescription());
        verify(mockPreparedStatement, times(1)).setBigDecimal(7, propertyToUpdate.getPrice());
        verify(mockPreparedStatement, times(1)).setBigDecimal(8, propertyToUpdate.getSquareFootage());
        verify(mockPreparedStatement, times(1)).setInt(9, propertyToUpdate.getBedrooms());
        verify(mockPreparedStatement, times(1)).setInt(10, propertyToUpdate.getBathrooms());
        verify(mockPreparedStatement, times(1)).setBoolean(11, propertyToUpdate.isActive());
        verify(mockPreparedStatement, times(1)).setTimestamp(eq(12), any(Timestamp.class));
        verify(mockPreparedStatement, times(1)).setString(13, "nonexistentId"); // WHERE clause ID
        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify resources closed
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        // Fix #4: Move verifyNoMoreInteractions to the end
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet); // Never used
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    void testUpdateProperty_DatabaseError() throws SQLException {
        // Arrange
        Property propertyToUpdate = createTestProperty();
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Simulated DB error"));

        // Act
        boolean result = propertyRepository.updateProperty(propertyToUpdate);

        // Assert
        assertFalse(result, "updateProperty should return false on database error");

        // Verify interactions up to executeUpdate
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        // Verify parameters were set
        verify(mockPreparedStatement, times(1)).setString(1, propertyToUpdate.getLandlordId());
        verify(mockPreparedStatement, times(1)).setString(2, propertyToUpdate.getAddress());
        verify(mockPreparedStatement, times(1)).setString(3, propertyToUpdate.getCity());
        verify(mockPreparedStatement, times(1)).setString(4, propertyToUpdate.getPostalCode());
        verify(mockPreparedStatement, times(1)).setString(5, propertyToUpdate.getPropertyType());
        verify(mockPreparedStatement, times(1)).setString(6, propertyToUpdate.getDescription());
        verify(mockPreparedStatement, times(1)).setBigDecimal(7, propertyToUpdate.getPrice());
        verify(mockPreparedStatement, times(1)).setBigDecimal(8, propertyToUpdate.getSquareFootage());
        verify(mockPreparedStatement, times(1)).setInt(9, propertyToUpdate.getBedrooms());
        verify(mockPreparedStatement, times(1)).setInt(10, propertyToUpdate.getBathrooms());
        verify(mockPreparedStatement, times(1)).setBoolean(11, propertyToUpdate.isActive());
        verify(mockPreparedStatement, times(1)).setTimestamp(eq(12), any(Timestamp.class));
        verify(mockPreparedStatement, times(1)).setString(13, propertyToUpdate.getPropertyId());
        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify resources closed despite error
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
        // ResultSet close is not verified in executeUpdate paths

        // Fix #4: Move verifyNoMoreInteractions to the end
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet); // Never used
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    void testUpdateProperty_NullPropertyObject() throws SQLException {
        // Arrange
        Property nullProperty = null;

        // Act
        boolean result = propertyRepository.updateProperty(nullProperty);

        // Assert
        assertFalse(result, "updateProperty should return false for null property object");
        verify(mockConnector, never()).getConnection(); // Verify no interaction with DB

        // Fix #4: Move verifyNoMoreInteractions to the end
        verifyNoMoreInteractions(mockPreparedStatement); // Never used
        verifyNoMoreInteractions(mockResultSet); // Never used
        verifyNoMoreInteractions(mockConnection); // Never used
    }

    @Test
    void testUpdateProperty_PropertyWithNullId() throws SQLException {
        // Arrange
        Property propertyWithNullId = createTestProperty();
        propertyWithNullId.setPropertyId(null); // Make ID null

        // Act
        boolean result = propertyRepository.updateProperty(propertyWithNullId);

        // Assert
        assertFalse(result, "updateProperty should return false for property with null ID");
        verify(mockConnector, never()).getConnection(); // Verify no interaction with DB

        // Fix #4: Move verifyNoMoreInteractions to the end
        verifyNoMoreInteractions(mockPreparedStatement); // Never used
        verifyNoMoreInteractions(mockResultSet); // Never used
        verifyNoMoreInteractions(mockConnection); // Never used
    }


    // --- deactivateProperty Tests ---

    @Test
    void testDeactivateProperty_Success() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 row affected

        // Act
        boolean result = propertyRepository.deactivateProperty(TEST_PROPERTY_ID);

        // Assert
        assertTrue(result, "deactivateProperty should return true on successful deactivation");

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString()); // Verify prepareStatement
        verify(mockPreparedStatement, times(1)).setString(1, TEST_PROPERTY_ID); // Verify parameter
        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify resources closed
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        // Fix #4: Move verifyNoMoreInteractions to the end
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet); // Never used
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    void testDeactivateProperty_NoRowsAffected() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(0); // 0 rows affected

        // Act
        boolean result = propertyRepository.deactivateProperty("nonexistentId");

        // Assert
        assertFalse(result, "deactivateProperty should return false if no rows were affected");

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, "nonexistentId");
        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify resources closed
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        // Fix #4: Move verifyNoMoreInteractions to the end
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet); // Never used
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    void testDeactivateProperty_DatabaseError() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Simulated DB error"));

        // Act
        boolean result = propertyRepository.deactivateProperty(TEST_PROPERTY_ID);

        // Assert
        assertFalse(result, "deactivateProperty should return false on database error");

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(1)).setString(1, TEST_PROPERTY_ID);
        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify resources closed despite error
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
        // ResultSet close is not verified in executeUpdate paths

        // Fix #4: Move verifyNoMoreInteractions to the end
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet); // Never used
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    void testDeactivateProperty_NullOrEmptyId() throws SQLException {
        // Arrange
        String nullId = null;
        String emptyId = "";
        String blankId = "   ";

        // Act
        boolean result1 = propertyRepository.deactivateProperty(nullId);
        boolean result2 = propertyRepository.deactivateProperty(emptyId);
        boolean result3 = propertyRepository.deactivateProperty(blankId);

        // Assert
        assertFalse(result1, "deactivateProperty should return false for null ID");
        assertFalse(result2, "deactivateProperty should return false for empty ID");
        assertFalse(result3, "deactivateProperty should return false for blank ID");

        verify(mockConnector, never()).getConnection(); // Verify no interaction with DB

        // Fix #4: Move verifyNoMoreInteractions to the end
        verifyNoMoreInteractions(mockPreparedStatement); // Never used
        verifyNoMoreInteractions(mockResultSet); // Never used
        verifyNoMoreInteractions(mockConnection); // Never used
    }

    // --- findProperties Tests (Comprehensive dynamic SQL testing) ---

    @Test
    void testFindProperties_NullFilter() throws SQLException {
        // Arrange
        Filter filter = null;
        // Mock ResultSet to return no rows
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Property> properties = propertyRepository.findProperties(filter);

        // Assert
        assertNotNull(properties, "findProperties should return a non-null list");
        assertTrue(properties.isEmpty(), "findProperties should return an empty list for null filter and no results");

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        // Verify prepareStatement was called with some string (representing the base SQL + order by)
        verify(mockConnection, times(1)).prepareStatement(anyString());
        // Verify no parameters were set
        verify(mockPreparedStatement, never()).setObject(anyInt(), any());
        verify(mockPreparedStatement, never()).setString(anyInt(), anyString());
        verify(mockPreparedStatement, never()).setBigDecimal(anyInt(), any());
        verify(mockPreparedStatement, never()).setInt(anyInt(), anyInt());
        verify(mockPreparedStatement, never()).setBoolean(anyInt(), anyBoolean());
        verify(mockPreparedStatement, never()).setTimestamp(anyInt(), any());

        // Verify execution calls
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next(); // Called once to check for first row

        // Verify resources closed
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        // Fix #4: Move verifyNoMoreInteractions
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet);
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    void testFindProperties_EmptyFilter() throws SQLException {
        // Arrange
        Filter filter = new Filter(); // All fields are null by default
        // Mock ResultSet to return no rows
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Property> properties = propertyRepository.findProperties(filter);

        // Assert
        assertNotNull(properties, "findProperties should return a non-null list");
        assertTrue(properties.isEmpty(), "findProperties should return an empty list for empty filter and no results");

        // Verify interactions (Should be same as null filter)
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockPreparedStatement, never()).setObject(anyInt(), any());
        // ... verify no other set methods were called ...

        // Verify execution calls
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();

        // Verify resources closed
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        // Fix #4: Move verifyNoMoreInteractions
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet);
        verifyNoMoreInteractions(mockConnection);
    }


    @Test
    void testFindProperties_FilterByLocation() throws SQLException {
        // Arrange
        Filter filter = new Filter();
        String location = "Downtown";
        filter.setLocation(location); // Fix #3: Changed setCity to setLocation

        // Mock ResultSet to return some rows
        when(mockResultSet.next()).thenReturn(true, true, false); // Mock 2 rows

        // Create dummy properties to configure mock ResultSet for mapping
        Property propA = createTestProperty(); // Use TEST_... data for propA
        Property propB = createTestProperty();
        propB.setPropertyId("propB"); // Give it a different ID
        propB.setCity("Another City"); // Give it different data if needed


        // Configure mock ResultSet to return data for propA on first .next()
        when(mockResultSet.getString("property_id")).thenReturn(propA.getPropertyId(), propB.getPropertyId()); // Return for both rows
        when(mockResultSet.getString("landlord_id")).thenReturn(propA.getLandlordId(), propB.getLandlordId());
        when(mockResultSet.getString("address")).thenReturn(propA.getAddress(), propB.getAddress());
        when(mockResultSet.getString("city")).thenReturn("Downtown City", "Downtown Area"); // Match filter if desired
        when(mockResultSet.getString("postal_code")).thenReturn("DT1 1DT", "DT2 2DT");
        when(mockResultSet.getString("property_type")).thenReturn(propA.getPropertyType(), propB.getPropertyType());
        when(mockResultSet.getString("description")).thenReturn(propA.getDescription(), propB.getDescription());
        when(mockResultSet.getBigDecimal("price")).thenReturn(propA.getPrice(), propB.getPrice());
        when(mockResultSet.getBigDecimal("square_footage")).thenReturn(propA.getSquareFootage(), propB.getSquareFootage());

        // Mock getInt and wasNull for nullable Integers (Bedrooms, Bathrooms) - called for each row (2 rows * 2 fields * 1 wasNull/field = 4 wasNull calls)
        when(mockResultSet.getInt("bedrooms")).thenReturn(propA.getBedrooms(), propB.getBedrooms());
        when(mockResultSet.getInt("bathrooms")).thenReturn(propA.getBathrooms(), propB.getBathrooms());
        // Fix #4: Mock wasNull() 4 times (2 calls for bedrooms, 2 for bathrooms across 2 rows)
        when(mockResultSet.wasNull()).thenReturn(propA.getBedrooms() == null, propA.getBathrooms() == null,
                propB.getBedrooms() == null, propB.getBathrooms() == null);


        when(mockResultSet.getBoolean("is_active")).thenReturn(propA.isActive(), propB.isActive());

        // Mock non-nullable Timestamps - must return non-null for each row
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(propA.getCreatedAt()), Timestamp.valueOf(propB.getCreatedAt()));
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(propA.getUpdatedAt()), Timestamp.valueOf(propB.getUpdatedAt()));
        // Mock nullable Timestamp (Date Listed) - can return null or non-null
        when(mockResultSet.getTimestamp("date_listed")).thenReturn(Timestamp.valueOf(propA.getDateListed()), null); // Example: first non-null, second null


        // Act
        List<Property> properties = propertyRepository.findProperties(filter);

        // Assert
        assertNotNull(properties, "findProperties should return a non-null list");
        assertEquals(2, properties.size(), "Should return the mocked number of properties");

        // Verify the content of mapped properties if desired (basic check)
        assertEquals(propA.getPropertyId(), properties.get(0).getPropertyId());
        assertEquals(propB.getPropertyId(), properties.get(1).getPropertyId());


        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString()); // Verify prepareStatement
        // Verify parameters: Location is set twice for city/postal_code LIKE clauses
        // We use setObject because that is what the SUT uses inside findProperties for parameters
        verify(mockPreparedStatement, times(2)).setObject(anyInt(), eq("%" + location + "%"));

        // Verify execution calls
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(3)).next(); // Called to get rows and terminate

        // Verify data was read from ResultSet for mapping (called twice per field for 2 rows)
        verify(mockResultSet, times(2)).getString("property_id");
        verify(mockResultSet, times(2)).getString("landlord_id");
        verify(mockResultSet, times(2)).getString("address");
        verify(mockResultSet, times(2)).getString("city");
        verify(mockResultSet, times(2)).getString("postal_code");
        verify(mockResultSet, times(2)).getString("property_type");
        verify(mockResultSet, times(2)).getString("description");
        verify(mockResultSet, times(2)).getBigDecimal("price");
        verify(mockResultSet, times(2)).getBigDecimal("square_footage");
        verify(mockResultSet, times(2)).getInt("bedrooms");
        verify(mockResultSet, times(2)).getInt("bathrooms");
        // Fix #4: Verify wasNull() is called 4 times (after getInt for bedrooms and bathrooms, 2 rows)
        verify(mockResultSet, times(4)).wasNull();
        verify(mockResultSet, times(2)).getBoolean("is_active");
        verify(mockResultSet, times(2)).getTimestamp("date_listed");
        verify(mockResultSet, times(2)).getTimestamp("created_at");
        verify(mockResultSet, times(2)).getTimestamp("updated_at");


        // Verify resource closure
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        // Fix #4: Move verifyNoMoreInteractions to the end
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet);
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    void testFindProperties_FilterByTypeAndMinPrice() throws SQLException {
        // Arrange
        Filter filter = new Filter();
        String type = "Condo";
        BigDecimal minPrice = new BigDecimal("250000");
        filter.setPropertyType(type);
        filter.setMinPrice(minPrice);

        // Mock ResultSet to return one row and then stop
        when(mockResultSet.next()).thenReturn(true, false); // Mock 1 row

        // Create a test property for mapping verification
        Property mappedProperty = createTestProperty();
        // Configure mock ResultSet for this specific property, including nullable/non-nullable values
        configureMockResultSetForSingleProperty(mappedProperty); // This helper sets up the required mocks for 1 row


        // Act
        List<Property> properties = propertyRepository.findProperties(filter);

        // Assert
        assertNotNull(properties, "findProperties should return a non-null list");
        assertEquals(1, properties.size(), "Should return a list with 1 property");
        // Assert that the mapped property matches the expected one (implicitly testing mapRowToProperty)
        assertEquals(mappedProperty.getPropertyId(), properties.get(0).getPropertyId());
        assertEquals(mappedProperty.getCity(), properties.get(0).getCity());
        assertEquals(mappedProperty.getPrice(), properties.get(0).getPrice());
        assertEquals(mappedProperty.getBedrooms(), properties.get(0).getBedrooms());
        assertEquals(mappedProperty.getSquareFootage(), properties.get(0).getSquareFootage());
        assertEquals(mappedProperty.getDateListed(), properties.get(0).getDateListed());


        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString()); // Verify prepareStatement
        // Verify parameters: Type is first (index 1), MinPrice is second (index 2)
        // Using setObject as the SUT does
        verify(mockPreparedStatement, times(1)).setObject(1, type);
        verify(mockPreparedStatement, times(1)).setObject(2, minPrice);

        // Verify execution calls
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(2)).next(); // Called once to get row, once to check for next (which is false)

        // Verify data was read from ResultSet for mapping (called once per field for the single row)
        verify(mockResultSet, times(1)).getString("property_id");
        verify(mockResultSet, times(1)).getString("landlord_id");
        verify(mockResultSet, times(1)).getString("address");
        verify(mockResultSet, times(1)).getString("city");
        verify(mockResultSet, times(1)).getString("postal_code");
        verify(mockResultSet, times(1)).getString("property_type");
        verify(mockResultSet, times(1)).getString("description");
        verify(mockResultSet, times(1)).getBigDecimal("price");
        verify(mockResultSet, times(1)).getBigDecimal("square_footage");
        verify(mockResultSet, times(1)).getInt("bedrooms");
        verify(mockResultSet, times(1)).getInt("bathrooms");
        // Fix #4: Verify wasNull() is called 2 times per row mapped (after getInt for bedrooms and bathrooms)
        verify(mockResultSet, times(2)).wasNull();
        verify(mockResultSet, times(1)).getBoolean("is_active");
        verify(mockResultSet, times(1)).getTimestamp("date_listed");
        verify(mockResultSet, times(1)).getTimestamp("created_at");
        verify(mockResultSet, times(1)).getTimestamp("updated_at");


        // Verify resource closure
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        // Fix #4: Move verifyNoMoreInteractions
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet);
        verifyNoMoreInteractions(mockConnection);
    }


    @Test
    void testFindProperties_FilterByBedroomsGreaterThanZero() throws SQLException {
        // Arrange
        Filter filter = new Filter();
        Integer minBedrooms = 1; // > 0
        filter.setMinBedrooms(minBedrooms);

        when(mockResultSet.next()).thenReturn(false); // No results

        // Act
        List<Property> properties = propertyRepository.findProperties(filter);

        // Assert
        assertNotNull(properties);
        assertTrue(properties.isEmpty());

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString()); // Verify prepareStatement
        // Verify parameters: Bedrooms is the only parameter set
        verify(mockPreparedStatement, times(1)).setObject(1, minBedrooms); // SUT uses setObject

        // Verify execution calls
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();

        // Verify resource closure
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        // Fix #4: Move verifyNoMoreInteractions
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet);
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    void testFindProperties_FilterByBedroomsZero() throws SQLException {
        // Arrange
        Filter filter = new Filter();
        Integer minBedrooms = 0; // Should be ignored by the SUT's logic
        filter.setMinBedrooms(minBedrooms);

        when(mockResultSet.next()).thenReturn(false); // No results

        // Act
        List<Property> properties = propertyRepository.findProperties(filter);

        // Assert
        assertNotNull(properties);
        assertTrue(properties.isEmpty());

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString()); // Verify prepareStatement
        // Verify NO parameters were set because the 0 value is ignored
        verify(mockPreparedStatement, never()).setObject(anyInt(), any());
        verify(mockPreparedStatement, never()).setInt(anyInt(), anyInt()); // Just to be sure
        // ... verify no other set methods were called ...

        // Verify execution calls
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();

        // Verify resources closed
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        // Fix #4: Move verifyNoMoreInteractions
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet);
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    void testFindProperties_FilterByMustBeActiveTrue() throws SQLException {
        // Arrange
        Filter filter = new Filter();
        filter.setMustBeActive(true);

        when(mockResultSet.next()).thenReturn(false); // No results

        // Act
        List<Property> properties = propertyRepository.findProperties(filter);

        // Assert
        assertNotNull(properties);
        assertTrue(properties.isEmpty());

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        // Verify parameter: is_active = ?
        verify(mockPreparedStatement, times(1)).setObject(1, true); // SUT uses setObject

        // Verify execution calls
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();

        // Verify resources closed
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        // Fix #4: Move verifyNoMoreInteractions
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet);
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    void testFindProperties_FilterByMustBeActiveFalse() throws SQLException {
        // Arrange
        Filter filter = new Filter();
        filter.setMustBeActive(false);

        when(mockResultSet.next()).thenReturn(false); // No results

        // Act
        List<Property> properties = propertyRepository.findProperties(filter);

        // Assert
        assertNotNull(properties);
        assertTrue(properties.isEmpty());

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        // Verify parameter: is_active = ?
        verify(mockPreparedStatement, times(1)).setObject(1, false); // SUT uses setObject

        // Verify execution calls
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();

        // Verify resources closed
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        // Fix #4: Move verifyNoMoreInteractions
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet);
        verifyNoMoreInteractions(mockConnection);
    }

    @Test
    void testFindProperties_FilterByMustBeActiveNull() throws SQLException {
        // Arrange
        Filter filter = new Filter();
        filter.setMustBeActive(null); // Should be ignored

        when(mockResultSet.next()).thenReturn(false); // No results

        // Act
        List<Property> properties = propertyRepository.findProperties(filter);

        // Assert
        assertNotNull(properties);
        assertTrue(properties.isEmpty());

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        // Verify NO parameters were set for active status (or any other filter)
        verify(mockPreparedStatement, never()).setObject(anyInt(), any());
        verify(mockPreparedStatement, never()).setBoolean(anyInt(), anyBoolean()); // Just to be sure
        // ... verify no other set methods were called ...

        // Verify execution calls
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();

        // Verify resources closed
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        // Fix #4: Move verifyNoMoreInteractions
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet);
        verifyNoMoreInteractions(mockConnection);
    }


    @Test
    void testFindProperties_MultipleFiltersCombination() throws SQLException {
        // Arrange
        Filter filter = new Filter();

        filter.setLocation("Metropolis"); // Fix #3: Use setLocation

        filter.setMinPrice(new BigDecimal("100000"));
        filter.setKeywords("pool");
        filter.setMustBeActive(true);


        when(mockResultSet.next()).thenReturn(false); // No results

        // Act
        List<Property> properties = propertyRepository.findProperties(filter);

        // Assert
        assertNotNull(properties);
        assertTrue(properties.isEmpty());

        // Verify interactions
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString()); // Verify prepareStatement

        // Verify parameters: Location (2 params), MinPrice (1 param), Keywords (1 param), MustBeActive (1 param)
        // Total parameters: 2 + 1 + 1 + 1 = 5
        // Order matters based on the if conditions in the SUT
        // Let's trace the parameter indices:
        // Location: 1, 2 (city, postal_code LIKE)
        // MinPrice: 3 (price >= ?)
        // MaxPrice: (skipped)
        // MinBedrooms: (skipped)
        // MinBathrooms: (skipped)
        // Keywords: 4 (description LIKE ?)
        // MustBeActive: 5 (is_active = ?)

        // Verify using setObject as the SUT does
        verify(mockPreparedStatement, times(1)).setObject(1, "%Metropolis%");
        verify(mockPreparedStatement, times(1)).setObject(2, "%Metropolis%");
        verify(mockPreparedStatement, times(1)).setObject(3, new BigDecimal("100000"));
        verify(mockPreparedStatement, times(1)).setObject(4, "%pool%");
        verify(mockPreparedStatement, times(1)).setObject(5, true);

        // Verify execution calls
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();

        // Verify resource closure
        verify(mockResultSet, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();

        // Fix #4: Move verifyNoMoreInteractions
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet);
        verifyNoMoreInteractions(mockConnection);
    }


    @Test
    void testFindProperties_DatabaseError() throws SQLException {
        // Arrange
        Filter filter = new Filter();
        filter.setLocation("Errorville"); // Fix #3: Use setLocation

        // Configure mock PreparedStatement to throw SQLException on executeQuery
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Simulated query error"));

        // Act
        List<Property> properties = propertyRepository.findProperties(filter);

        // Assert
        assertNotNull(properties, "findProperties should return a non-null list on database error");
        assertTrue(properties.isEmpty(), "findProperties should return an empty list on database error");

        // Verify interactions up to executeQuery
        verify(mockConnector, times(1)).getConnection();
        verify(mockConnection, times(1)).prepareStatement(anyString());
        // Verify parameters were set (Location filter adds 2 String parameters)
        verify(mockPreparedStatement, times(2)).setObject(anyInt(), anyString());

        // Verify execution calls
        verify(mockPreparedStatement, times(1)).executeQuery();

        // Verify resources closed despite error
        verify(mockPreparedStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
        // ResultSet close is not verified in executeQuery paths

        // Fix #4: Move verifyNoMoreInteractions
        verifyNoMoreInteractions(mockPreparedStatement);
        verifyNoMoreInteractions(mockResultSet); // Never used
        verifyNoMoreInteractions(mockConnection);
    }

    // --- Helper Methods Tests ---
    // Private helper methods (mapRowToProperty, setNullable*, getNullable*)
    // are tested indirectly by the public methods that use them.
    // The tests above verify that:
    // - addProperty/updateProperty call the correct setNullable* methods (by verifying setNull or set* on PS).
    // - getPropertyById/findProperties correctly map data from a mock ResultSet to a Property object (by asserting on the returned Property's fields).
    // - getPropertyById/findProperties correctly handle nullable columns from the ResultSet (by mocking rs.wasNull() and checking the returned Property's fields).

    // --- Testing Resource Closure ---
    // In all tests where getConnection() is called, we verify the close() methods
    // on mockConnection, mockPreparedStatement, and mockResultSet (for queries)
    // to ensure the try-with-resources blocks or explicit close calls
    // in the original code are being executed correctly even under test scenarios.
}