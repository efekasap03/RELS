package Data.domain; // Ensure this matches the package of your Landlord class

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Objects;

class LandlordTest {

    // --- Tests for Constructors ---

    @Test
    void testDefaultConstructor() {
        // Arrange & Act
        Landlord landlord = new Landlord();

        // Assert
        assertNotNull(landlord, "Landlord object should not be null");
        // Check default state from User superclass (assuming null for fields)
        assertNull(landlord.getUserId(), "Default constructor should initialize userId to null");
        assertNull(landlord.getName(), "Default constructor should initialize name to null");
        assertNull(landlord.getEmail(), "Default constructor should initialize email to null");
        assertNull(landlord.getPasswordHash(), "Default constructor should initialize passwordHash to null");
        // Check state set by the Landlord default constructor
        assertEquals("LANDLORD", landlord.getRole(), "Default constructor should set role to 'LANDLORD'");
        assertNull(landlord.getAgentLicenseNumber(), "Default constructor should initialize agentLicenseNumber to null");
    }

    @Test
    void testParameterizedConstructor() {
        // Arrange
        String userId = "landlord123";
        String name = "Alice Landlord";
        String email = "alice@example.com";
        String passwordHash = "anotherhashedpassword";
        String license = "LN12345";

        // Act
        Landlord landlord = new Landlord(userId, name, email, passwordHash, license);

        // Assert
        assertNotNull(landlord, "Landlord object should not be null");
        // Check state passed to User superclass constructor
        assertEquals(userId, landlord.getUserId(), "Parameterized constructor should set userId");
        assertEquals(name, landlord.getName(), "Parameterized constructor should set name");
        assertEquals(email, landlord.getEmail(), "Parameterized constructor should set email");
        assertEquals(passwordHash, landlord.getPasswordHash(), "Parameterized constructor should set passwordHash");
        assertEquals("LANDLORD", landlord.getRole(), "Parameterized constructor should set role to 'LANDLORD'");
        // Check state specific to Landlord constructor
        assertEquals(license, landlord.getAgentLicenseNumber(), "Parameterized constructor should set agentLicenseNumber");
    }

    // --- Tests for Specific Getter/Setter ---

    @Test
    void testSetAndGetAgentLicenseNumber() {
        // Arrange
        Landlord landlord = new Landlord();
        String expectedLicense = "XYZ987";

        // Act
        landlord.setAgentLicenseNumber(expectedLicense);
        String actualLicense = landlord.getAgentLicenseNumber();

        // Assert
        assertEquals(expectedLicense, actualLicense, "Set and get agentLicenseNumber should retrieve the correct value");
    }

    // --- Tests for equals() ---

    @Test
    void testEquals_SameUserIdAndLicense() {
        // Arrange
        Landlord landlord1 = new Landlord("id456", "Name A", "a@example.com", "hashA", "LicenseA");
        Landlord landlord2 = new Landlord("id456", "Name B", "b@example.com", "hashB", "LicenseA"); // Different name/email/hash, same userId and license

        // Assert
        // Assuming User.equals() is based on userId, super.equals(o) will be true.
        // Landlord.equals() then checks agentLicenseNumber, which is also same.
        assertEquals(landlord1, landlord2, "Landlords with same userId and agentLicenseNumber should be equal");
    }

    @Test
    void testEquals_SameUserIdDifferentLicense() {
        // Arrange
        Landlord landlord1 = new Landlord("id456", "Name A", "a@example.com", "hashA", "LicenseA");
        Landlord landlord2 = new Landlord("id456", "Name A", "a@example.com", "hashA", "LicenseB"); // Same User fields, different license

        // Assert
        // super.equals(o) will be true. Landlord.equals() checks agentLicenseNumber, which are different.
        assertNotEquals(landlord1, landlord2, "Landlords with same userId but different agentLicenseNumber should not be equal");
    }

    @Test
    void testEquals_DifferentUserIdSameLicense() {
        // Arrange
        Landlord landlord1 = new Landlord("id456", "Name A", "a@example.com", "hashA", "LicenseA");
        Landlord landlord2 = new Landlord("id789", "Name A", "a@example.com", "hashA", "LicenseA"); // Different userId, same license

        // Assert
        // super.equals(o) will be false (due to different userId). Landlord.equals() will return false.
        assertNotEquals(landlord1, landlord2, "Landlords with different userId should not be equal, even if agentLicenseNumber is same");
    }

    @Test
    void testEquals_DifferentUserIdDifferentLicense() {
        // Arrange
        Landlord landlord1 = new Landlord("id456", "Name A", "a@example.com", "hashA", "LicenseA");
        Landlord landlord2 = new Landlord("id789", "Name B", "b@example.com", "hashB", "LicenseB"); // Different userId and license

        // Assert
        // super.equals(o) will be false. Landlord.equals() will return false.
        assertNotEquals(landlord1, landlord2, "Landlords with different userId and agentLicenseNumber should not be equal");
    }


    @Test
    void testEquals_NullObject() {
        // Arrange
        Landlord landlord1 = new Landlord("id456", "Name A", "a@example.com", "hashA", "LicenseA");
        Landlord landlord2 = null;

        // Assert
        assertNotEquals(landlord1, landlord2, "Landlord should not be equal to null");
    }

    @Test
    void testEquals_DifferentClass() {
        // Arrange
        Landlord landlord1 = new Landlord("id456", "Name A", "a@example.com", "hashA", "LicenseA");
        // Assuming Client is a different class that extends User
        // Object notALandlord = new Client("id456", "Name A", "a@example.com", "hashA", true); // If Client exists
        Object notALandlord = new Object(); // Use generic Object if no other User subclass exists

        // Assert
        // getClass() != o.getClass() will be true
        assertNotEquals(landlord1, notALandlord, "Landlord should not be equal to an object of a different class");
    }

    @Test
    void testEquals_SameObjectInstance() {
        // Arrange
        Landlord landlord1 = new Landlord("id456", "Name A", "a@example.com", "hashA", "LicenseA");
        Landlord landlord2 = landlord1; // Same instance

        // Assert
        assertEquals(landlord1, landlord2, "Landlord should be equal to itself");
    }


    // --- Tests for hashCode() ---

    @Test
    void testHashCode_ConsistentForEqualObjects() {
        // Arrange
        Landlord landlord1 = new Landlord("id456", "Name A", "a@example.com", "hashA", "LicenseA");
        Landlord landlord2 = new Landlord("id456", "Name B", "b@example.com", "hashB", "LicenseA"); // Equal according to equals()

        // Assert
        // If equals() returns true, hashCode() must return the same value
        assertEquals(landlord1, landlord2, "Landlords should be equal for this test sanity check");
        assertEquals(landlord1.hashCode(), landlord2.hashCode(), "Hash codes must be equal for equal objects");
    }

    @Test
    void testHashCode_ConsistentForSameObject() {
        // Arrange
        Landlord landlord1 = new Landlord("id456", "Name A", "a@example.com", "hashA", "LicenseA");
        int hashCode1 = landlord1.hashCode();

        // Act (get hash code multiple times)
        int hashCode2 = landlord1.hashCode();
        int hashCode3 = landlord1.hashCode();

        // Assert
        assertEquals(hashCode1, hashCode2, "Hash code must be consistent");
        assertEquals(hashCode1, hashCode3, "Hash code must be consistent");
    }

    // --- Testing toString() (Optional, often brittle) ---
    @Test
    void testToString() {
         // Arrange
        Landlord landlord = new Landlord("l77", "Bob", "bob@test.com", "pw", "L8888");
        String userToString = new User("l77", "Bob", "bob@test.com", "pw", "LANDLORD").toString(); // Simulate User toString output
        String expectedToString = "Landlord{" +
                "user=" + userToString +
                ", agentLicenseNumber='" + "L8888" + '\'' +
                '}';

        // Act
        String actualToString = landlord.toString();

        // Assert
        assertEquals(expectedToString, actualToString, "toString() output format should match expected");
        // Note: Testing toString() can be brittle if formatting changes slightly, or if User's toString changes.
    }
}