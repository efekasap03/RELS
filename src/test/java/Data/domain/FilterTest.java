package Data.domain; // Ensure this matches the package of your Filter class

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

// Unit tests for the Filter data class.
// Focuses on constructor initialization and specific field get/set,
// particularly for wrapper types that allow null.
// Does NOT test equals/hashCode as they are not overridden and use default Object behavior.
// toString test is commented out as it's often brittle.
class FilterTest {

    // --- Tests for Constructor ---

    @Test
    void testDefaultConstructorInitializesFieldsToNull() {
        // Arrange & Act
        Filter filter = new Filter();

        // Assert
        assertNotNull(filter, "Filter object should not be null");
        // All reference type fields should be initialized to null by the default constructor
        assertNull(filter.getLocation(), "Default constructor should initialize location to null");
        assertNull(filter.getPropertyType(), "Default constructor should initialize propertyType to null");
        assertNull(filter.getMinPrice(), "Default constructor should initialize minPrice to null");
        assertNull(filter.getMaxPrice(), "Default constructor should initialize maxPrice to null");
        assertNull(filter.getMinBedrooms(), "Default constructor should initialize minBedrooms to null");
        assertNull(filter.getMinBathrooms(), "Default constructor should initialize minBathrooms to null");
        assertNull(filter.getKeywords(), "Default constructor should initialize keywords to null");
        assertNull(filter.getMustBeActive(), "Default constructor should initialize mustBeActive to null");
    }

    // --- Tests for Getters and Setters (Demonstration / Key fields) ---

    @Test
    void testSetAndGetLocation() {
        // Arrange
        Filter filter = new Filter();
        String expectedLocation = "New York";

        // Act
        filter.setLocation(expectedLocation);
        String actualLocation = filter.getLocation();

        // Assert
        assertEquals(expectedLocation, actualLocation, "Set and get location should retrieve the correct value");
    }

    @Test
    void testSetAndGetMinPrice() {
        // Arrange
        Filter filter = new Filter();
        BigDecimal expectedPrice = new BigDecimal("150000.00");

        // Act
        filter.setMinPrice(expectedPrice);
        BigDecimal actualPrice = filter.getMinPrice();

        // Assert
        assertEquals(expectedPrice, actualPrice, "Set and get minPrice should retrieve the correct value");
    }

    @Test
    void testSetAndGetMinBedrooms() {
        // Arrange
        Filter filter = new Filter();
        Integer expectedBedrooms = 3;

        // Act
        filter.setMinBedrooms(expectedBedrooms);
        Integer actualBedrooms = filter.getMinBedrooms();

        // Assert
        assertEquals(expectedBedrooms, actualBedrooms, "Set and get minBedrooms should retrieve the correct value");
    }


    @Test
    void testSetAndGetMustBeActive_True() {
        // Arrange
        Filter filter = new Filter();
        Boolean expectedValue = true;

        // Act
        filter.setMustBeActive(expectedValue);
        Boolean actualValue = filter.getMustBeActive();

        // Assert
        assertEquals(expectedValue, actualValue, "Set and get mustBeActive should retrieve the 'true' value");
    }

    @Test
    void testSetAndGetMustBeActive_False() {
        // Arrange
        Filter filter = new Filter();
        Boolean expectedValue = false;

        // Act
        filter.setMustBeActive(expectedValue);
        Boolean actualValue = filter.getMustBeActive();

        // Assert
        assertEquals(expectedValue, actualValue, "Set and get mustBeActive should retrieve the 'false' value");
    }

    @Test
    void testSetAndGetMustBeActive_Null() {
        // Arrange
        Filter filter = new Filter();
        // Ensure it's initially not null if it was set before
        filter.setMustBeActive(true);
        assertNotNull(filter.getMustBeActive(), "MustBeActive should not be null initially for this test setup");

        Boolean expectedValue = null;

        // Act
        filter.setMustBeActive(expectedValue);
        Boolean actualValue = filter.getMustBeActive();

        // Assert
        assertNull(actualValue, "Set and get mustBeActive should retrieve the 'null' value");
    }


    // --- Testing toString() (Optional, often brittle) ---
    @Test
    void testToString() {
        // Arrange
        Filter filter = new Filter();
        filter.setLocation("London");
        filter.setPropertyType("House");
        filter.setMinPrice(new BigDecimal("200000.00"));
        filter.setMustBeActive(true);

        // Construct the expected string - be careful with formatting details!
        String expectedToString = "Filter{" +
                "location='London'" +
                ", propertyType='House'" +
                ", minPrice=200000.00" +
                ", maxPrice=null" +
                ", minBedrooms=null" +
                ", minBathrooms=null" +
                ", keywords='null'" +
                ", mustBeActive=true" +
                '}'; // Note: Keywords is a String, its toString is "null" if the field is null

        // Act
        String actualToString = filter.toString();

        // Assert
        assertEquals(expectedToString, actualToString, "toString() output format should match expected");
        // Note: Testing toString() can be brittle. If the format changes,
        // this test will fail even if the data in the object is correct.
    }
}