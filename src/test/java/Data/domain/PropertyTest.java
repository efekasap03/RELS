package Data.domain; // Ensure this matches the package of your Property class

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

class PropertyTest {

    // --- Tests for Constructors ---

    @Test
    void testDefaultConstructorInitializesFieldsToDefaults() {
        // Arrange & Act
        Property property = new Property();

        // Assert
        assertNotNull(property, "Property object should not be null");
        // All reference type fields should be initialized to null by the default constructor
        assertNull(property.getPropertyId(), "Default constructor should initialize propertyId to null");
        assertNull(property.getLandlordId(), "Default constructor should initialize landlordId to null");
        assertNull(property.getAddress(), "Default constructor should initialize address to null");
        assertNull(property.getCity(), "Default constructor should initialize city to null");
        assertNull(property.getPostalCode(), "Default constructor should initialize postalCode to null");
        assertNull(property.getPropertyType(), "Default constructor should initialize propertyType to null");
        assertNull(property.getDescription(), "Default constructor should initialize description to null");
        assertNull(property.getPrice(), "Default constructor should initialize price to null");
        assertNull(property.getSquareFootage(), "Default constructor should initialize squareFootage to null");
        assertNull(property.getBedrooms(), "Default constructor should initialize bedrooms to null");
        assertNull(property.getBathrooms(), "Default constructor should initialize bathrooms to null");
        assertNull(property.getDateListed(), "Default constructor should initialize dateListed to null");
        assertNull(property.getCreatedAt(), "Default constructor should initialize createdAt to null");
        assertNull(property.getUpdatedAt(), "Default constructor should initialize updatedAt to null");
        // Primitive boolean should be initialized to false
        assertFalse(property.isActive(), "Default constructor should initialize isActive to false");
    }

    // --- Tests for java.sql.Timestamp Setters ---

    @Test
    void testSetDateListedFromSqlTimestamp_NonNull() {
        // Arrange
        Property property = new Property();
        LocalDateTime now = LocalDateTime.now();
        Timestamp sqlTimestamp = Timestamp.valueOf(now);

        // Act
        property.setDateListed(sqlTimestamp);

        // Assert
        assertNotNull(property.getDateListed(), "dateListed should not be null after setting a non-null SQL Timestamp");
        // Compare up to seconds to avoid nanosecond precision issues during conversion
        assertEquals(now.toLocalDate(), property.getDateListed().toLocalDate(), "Date part of dateListed should match");
        assertEquals(now.toLocalTime().toSecondOfDay(), property.getDateListed().toLocalTime().toSecondOfDay(), "Time part (up to seconds) of dateListed should match");
    }

    @Test
    void testSetDateListedFromSqlTimestamp_Null() {
        // Arrange
        Property property = new Property();
        Timestamp sqlTimestamp = null;

        // Act
        property.setDateListed(sqlTimestamp);

        // Assert
        assertNull(property.getDateListed(), "dateListed should be null after setting a null SQL Timestamp");
    }

    @Test
    void testSetCreatedAtFromSqlTimestamp_NonNull() {
        // Arrange
        Property property = new Property();
        LocalDateTime now = LocalDateTime.now();
        Timestamp sqlTimestamp = Timestamp.valueOf(now);

        // Act
        property.setCreatedAt(sqlTimestamp);

        // Assert
        assertNotNull(property.getCreatedAt(), "createdAt should not be null after setting a non-null SQL Timestamp");
        assertEquals(now.toLocalDate(), property.getCreatedAt().toLocalDate(), "Date part of createdAt should match");
        assertEquals(now.toLocalTime().toSecondOfDay(), property.getCreatedAt().toLocalTime().toSecondOfDay(), "Time part (up to seconds) of createdAt should match");
    }

    @Test
    void testSetCreatedAtFromSqlTimestamp_Null() {
        // Arrange
        Property property = new Property();
        Timestamp sqlTimestamp = null;

        // Act
        property.setCreatedAt(sqlTimestamp);

        // Assert
        assertNull(property.getCreatedAt(), "createdAt should be null after setting a null SQL Timestamp");
    }

    @Test
    void testSetUpdatedAtFromSqlTimestamp_NonNull() {
        // Arrange
        Property property = new Property();
        LocalDateTime now = LocalDateTime.now();
        Timestamp sqlTimestamp = Timestamp.valueOf(now);

        // Act
        property.setUpdatedAt(sqlTimestamp);

        // Assert
        assertNotNull(property.getUpdatedAt(), "updatedAt should not be null after setting a non-null SQL Timestamp");
        assertEquals(now.toLocalDate(), property.getUpdatedAt().toLocalDate(), "Date part of updatedAt should match");
        assertEquals(now.toLocalTime().toSecondOfDay(), property.getUpdatedAt().toLocalTime().toSecondOfDay(), "Time part (up to seconds) of updatedAt should match");
    }

    @Test
    void testSetUpdatedAtFromSqlTimestamp_Null() {
        // Arrange
        Property property = new Property();
        Timestamp sqlTimestamp = null;

        // Act
        property.setUpdatedAt(sqlTimestamp);

        // Assert
        assertNull(property.getUpdatedAt(), "updatedAt should be null after setting a null SQL Timestamp");
    }

    // --- Tests for Specific Field Types (Getters/Setters) ---

    @Test
    void testSetAndGetPrice() {
        // Arrange
        Property property = new Property();
        BigDecimal expectedPrice = new BigDecimal("550000.75");

        // Act
        property.setPrice(expectedPrice);
        BigDecimal actualPrice = property.getPrice();

        // Assert
        assertEquals(expectedPrice, actualPrice, "Set and get price should retrieve the correct value");
    }

    @Test
    void testSetAndGetBedrooms_NonNull() {
        // Arrange
        Property property = new Property();
        Integer expectedBedrooms = 4;

        // Act
        property.setBedrooms(expectedBedrooms);
        Integer actualBedrooms = property.getBedrooms();

        // Assert
        assertEquals(expectedBedrooms, actualBedrooms, "Set and get bedrooms should retrieve the correct non-null value");
    }

    @Test
    void testSetAndGetBedrooms_Null() {
        // Arrange
        Property property = new Property();
        // Set to a value first to ensure null actually overwrites it
        property.setBedrooms(3);
        assertNotNull(property.getBedrooms(), "Bedrooms should be non-null initially for this test setup");

        Integer expectedBedrooms = null;

        // Act
        property.setBedrooms(expectedBedrooms);
        Integer actualBedrooms = property.getBedrooms();

        // Assert
        assertNull(actualBedrooms, "Set and get bedrooms should retrieve the null value");
    }

    @Test
    void testSetAndGetIsActive_True() {
        // Arrange
        Property property = new Property();
        boolean expectedValue = true;

        // Act
        property.setActive(expectedValue);
        boolean actualValue = property.isActive();

        // Assert
        assertEquals(expectedValue, actualValue, "Set and get isActive should retrieve the 'true' value");
    }

    @Test
    void testSetAndGetIsActive_False() {
        // Arrange
        Property property = new Property();
        // Set to true first to ensure false overwrites it
        property.setActive(true);
        assertTrue(property.isActive(), "IsActive should be true initially for this test setup");

        boolean expectedValue = false;

        // Act
        property.setActive(expectedValue);
        boolean actualValue = property.isActive();

        // Assert
        assertEquals(expectedValue, actualValue, "Set and get isActive should retrieve the 'false' value");
    }


    // --- Tests for equals() and hashCode() ---

    @Test
    void testEquals_SamePropertyId() {
        // Arrange
        Property prop1 = new Property();
        prop1.setPropertyId("propXYZ");
        prop1.setAddress("Address A"); // Different field value
        prop1.setPrice(new BigDecimal("100000")); // Different field value

        Property prop2 = new Property();
        prop2.setPropertyId("propXYZ");
        prop2.setAddress("Address B"); // Different field value
        prop2.setActive(true); // Different field value

        // Assert
        assertEquals(prop1, prop2, "Properties with the same propertyId should be equal");
    }

    @Test
    void testEquals_DifferentPropertyId() {
        // Arrange
        Property prop1 = new Property();
        prop1.setPropertyId("propXYZ");
        Property prop2 = new Property();
        prop2.setPropertyId("propABC");

        // Assert
        assertNotEquals(prop1, prop2, "Properties with different propertyId should not be equal");
    }

    @Test
    void testEquals_NullObject() {
        // Arrange
        Property prop1 = new Property();
        prop1.setPropertyId("propXYZ");
        Property prop2 = null;

        // Assert
        assertNotEquals(prop1, prop2, "Property should not be equal to null");
    }

    @Test
    void testEquals_DifferentClass() {
        // Arrange
        Property prop1 = new Property();
        prop1.setPropertyId("propXYZ");
        Object notAProperty = new Object();

        // Assert
        assertNotEquals(prop1, notAProperty, "Property should not be equal to an object of a different class");
    }

    @Test
    void testEquals_SameObjectInstance() {
        // Arrange
        Property prop1 = new Property();
        prop1.setPropertyId("propXYZ");
        Property prop2 = prop1; // Same instance

        // Assert
        assertEquals(prop1, prop2, "Property should be equal to itself");
    }


    @Test
    void testHashCode_ConsistentForEqualObjects() {
        // Arrange
        Property prop1 = new Property();
        prop1.setPropertyId("propXYZ");
        prop1.setAddress("Address A"); // Different field value, ignored by equals/hashCode
        Property prop2 = new Property();
        prop2.setPropertyId("propXYZ");
        prop2.setAddress("Address B"); // Different field value, ignored by equals/hashCode

        // Assert
        // If equals() returns true, hashCode() must return the same value
        assertEquals(prop1, prop2, "Properties should be equal for this test"); // Sanity check
        assertEquals(prop1.hashCode(), prop2.hashCode(), "Hash codes must be equal for equal objects");
    }

    @Test
    void testHashCode_ConsistentForSameObject() {
        // Arrange
        Property prop1 = new Property();
        prop1.setPropertyId("propXYZ");
        int hashCode1 = prop1.hashCode();

        // Act (get hash code multiple times)
        int hashCode2 = prop1.hashCode();
        int hashCode3 = prop1.hashCode();

        // Assert
        assertEquals(hashCode1, hashCode2, "Hash code must be consistent");
        assertEquals(hashCode1, hashCode3, "Hash code must be consistent");
    }


    // --- Testing toString() (Optional, often brittle) ---
    @Test
    void testToString() {
         // Arrange
        Property property = new Property();
        property.setPropertyId("p111");
        property.setLandlordId("l222");
        property.setAddress("123 Main St");
        property.setCity("Anytown");
        property.setPrice(new BigDecimal("300000.00"));
        property.setActive(true);

        String expectedToString = "Property:" + '\n' +
                "PropertyId = " + "p111" + '\n' +
                "LandlordId = " + "l222" + '\n' +
                "Address = " + "123 Main St" + '\n' +
                "City = " + "Anytown" + '\n' +
                "Price = " + "300000.00" +'\n' + // Note: BigDecimal.toString() might vary
                "isActive = " + "true" ;

        // Act
        String actualToString = property.toString();

        // Assert
        assertEquals(expectedToString, actualToString, "toString() output format should match expected");
        // Note: Testing toString() can be brittle if formatting changes or fields are added/removed from the output.
    }
}