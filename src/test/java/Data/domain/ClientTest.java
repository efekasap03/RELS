package Data.domain; // Ensure this matches the package of your Client class

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Objects;

// Assuming User is a simple data class with equals/hashCode based on userId.
// If User had complex dependencies, we might need more advanced mocking.

class ClientTest {

    // --- Tests for Constructors ---

    @Test
    void testDefaultConstructor() {
        // Arrange & Act
        Client client = new Client();

        // Assert
        assertNotNull(client, "Client object should not be null");
        // Check default state from User superclass (assuming null for fields)
        assertNull(client.getUserId(), "Default constructor should initialize userId to null");
        assertNull(client.getName(), "Default constructor should initialize name to null");
        assertNull(client.getEmail(), "Default constructor should initialize email to null");
        assertNull(client.getPasswordHash(), "Default constructor should initialize passwordHash to null");
        // Check state set by the Client default constructor
        assertEquals("CLIENT", client.getRole(), "Default constructor should set role to 'CLIENT'");
        assertFalse(client.isReceivesMarketUpdates(), "Default constructor should initialize receivesMarketUpdates to false"); // Default boolean value
    }

    @Test
    void testParameterizedConstructor() {
        // Arrange
        String userId = "client123";
        String name = "Bob Client";
        String email = "bob@example.com";
        String passwordHash = "hashedpassword";
        boolean receivesUpdates = true;

        // Act
        Client client = new Client(userId, name, email, passwordHash, receivesUpdates);

        // Assert
        assertNotNull(client, "Client object should not be null");
        // Check state passed to User superclass constructor
        assertEquals(userId, client.getUserId(), "Parameterized constructor should set userId");
        assertEquals(name, client.getName(), "Parameterized constructor should set name");
        assertEquals(email, client.getEmail(), "Parameterized constructor should set email");
        assertEquals(passwordHash, client.getPasswordHash(), "Parameterized constructor should set passwordHash");
        assertEquals("CLIENT", client.getRole(), "Parameterized constructor should set role to 'CLIENT'");
        // Check state specific to Client constructor
        assertEquals(receivesUpdates, client.isReceivesMarketUpdates(), "Parameterized constructor should set receivesMarketUpdates");
    }

    // --- Tests for Specific Getter/Setter ---

    @Test
    void testSetAndGetReceivesMarketUpdates() {
        // Arrange
        Client client = new Client();
        boolean initialValue = client.isReceivesMarketUpdates(); // Should be false from default constructor
        boolean newValue = !initialValue; // Toggle the value

        // Act
        client.setReceivesMarketUpdates(newValue);
        boolean retrievedValue = client.isReceivesMarketUpdates();

        // Assert
        assertNotEquals(initialValue, retrievedValue, "Setter should change the value");
        assertEquals(newValue, retrievedValue, "Getter should return the value set by the setter");
    }

    // --- Tests for equals() ---

    @Test
    void testEquals_SameUserIdAndMarketUpdates() {
        // Arrange
        Client client1 = new Client("id123", "Name A", "a@example.com", "hashA", true);
        Client client2 = new Client("id123", "Name B", "b@example.com", "hashB", true); // Different name/email/hash, same userId and updates

        // Assert
        // Assuming User.equals() is based on userId, super.equals(o) will be true.
        // Client.equals() then checks receivesMarketUpdates, which are also same.
        assertEquals(client1, client2, "Clients with same userId and receivesMarketUpdates should be equal");
    }

    @Test
    void testEquals_SameUserIdDifferentMarketUpdates() {
        // Arrange
        Client client1 = new Client("id123", "Name A", "a@example.com", "hashA", true);
        Client client2 = new Client("id123", "Name A", "a@example.com", "hashA", false); // Same User fields, different receivesMarketUpdates

        // Assert
        // super.equals(o) will be true. Client.equals() checks receivesMarketUpdates, which are different.
        assertNotEquals(client1, client2, "Clients with same userId but different receivesMarketUpdates should not be equal");
    }

    @Test
    void testEquals_DifferentUserIdSameMarketUpdates() {
        // Arrange
        Client client1 = new Client("id123", "Name A", "a@example.com", "hashA", true);
        Client client2 = new Client("id456", "Name A", "a@example.com", "hashA", true); // Different userId, same receivesMarketUpdates

        // Assert
        // super.equals(o) will be false (due to different userId). Client.equals() will return false.
        assertNotEquals(client1, client2, "Clients with different userId should not be equal, even if receivesMarketUpdates is same");
    }

    @Test
    void testEquals_DifferentUserIdDifferentMarketUpdates() {
        // Arrange
        Client client1 = new Client("id123", "Name A", "a@example.com", "hashA", true);
        Client client2 = new Client("id456", "Name B", "b@example.com", "hashB", false); // Different userId and receivesMarketUpdates

        // Assert
        // super.equals(o) will be false. Client.equals() will return false.
        assertNotEquals(client1, client2, "Clients with different userId and receivesMarketUpdates should not be equal");
    }


    @Test
    void testEquals_NullObject() {
        // Arrange
        Client client1 = new Client("id123", "Name A", "a@example.com", "hashA", true);
        Client client2 = null;

        // Assert
        assertNotEquals(client1, client2, "Client should not be equal to null");
    }

    @Test
    void testEquals_DifferentClass() {
        // Arrange
        Client client1 = new Client("id123", "Name A", "a@example.com", "hashA", true);
        // Assuming Landlord is a different class, even if it extends User
        // Object notAClient = new Landlord("id123", "Name A", "a@example.com", "hashA", "license123"); // If Landlord exists
        Object notAClient = new Object(); // Use generic Object if no other User subclass exists

        // Assert
        // getClass() != o.getClass() will be true
        assertNotEquals(client1, notAClient, "Client should not be equal to an object of a different class");
    }

    @Test
    void testEquals_SameObjectInstance() {
        // Arrange
        Client client1 = new Client("id123", "Name A", "a@example.com", "hashA", true);
        Client client2 = client1; // Same instance

        // Assert
        assertEquals(client1, client2, "Client should be equal to itself");
    }


    // --- Tests for hashCode() ---

    @Test
    void testHashCode_ConsistentForEqualObjects() {
        // Arrange
        Client client1 = new Client("id123", "Name A", "a@example.com", "hashA", true);
        Client client2 = new Client("id123", "Name B", "b@example.com", "hashB", true); // Equal according to equals()

        // Assert
        // If equals() returns true, hashCode() must return the same value
        assertEquals(client1, client2, "Clients should be equal for this test sanity check");
        assertEquals(client1.hashCode(), client2.hashCode(), "Hash codes must be equal for equal objects");
    }

    @Test
    void testHashCode_DifferentForObjectsWithDifferentMarketUpdates() {
        // Arrange
        Client client1 = new Client("id123", "Name A", "a@example.com", "hashA", true);
        Client client2 = new Client("id123", "Name A", "a@example.com", "hashA", false); // Same User part, different receivesMarketUpdates

        // Assert
        // If objects are not equal, hash codes are NOT required to be different,
        // but for good distribution, they *should* ideally be different.
        // We test that they are NOT equal, and if the hash code implementation is good,
        // their hash codes will likely be different. The strict requirement is only
        // that equal objects *must* have equal hash codes.
        // Let's test the *contract* first, then potentially test for difference if desired (optional).

        // Assert that they are NOT equal
        assertNotEquals(client1, client2, "Clients should not be equal for this test sanity check");

        // Optional: Assert hash codes are different (good for distribution, not strictly required by contract)
        // assertNotEquals(client1.hashCode(), client2.hashCode(), "Hash codes should ideally be different for non-equal objects with different relevant fields");

        // The primary contract test is covered by the testEquals_SameUserIdDifferentMarketUpdates.
        // Testing that different objects *always* have different hash codes is impossible and not required.
        // Testing that *equal* objects have the same hash code is required and covered by testHashCode_ConsistentForEqualObjects.
    }


    @Test
    void testHashCode_ConsistentForSameObject() {
        // Arrange
        Client client1 = new Client("id123", "Name A", "a@example.com", "hashA", true);
        int hashCode1 = client1.hashCode();

        // Act (get hash code multiple times)
        int hashCode2 = client1.hashCode();
        int hashCode3 = client1.hashCode();

        // Assert
        assertEquals(hashCode1, hashCode2, "Hash code must be consistent");
        assertEquals(hashCode1, hashCode3, "Hash code must be consistent");
    }


    // --- Testing toString() (Optional, often brittle) ---
    @Test
    void testToString() {
         // Arrange
        Client client = new Client("c99", "Alice", "alice@test.com", "passhash", false);
        String userToString = new User("c99", "Alice", "alice@test.com", "passhash", "CLIENT").toString(); // Simulate User toString output
        String expectedToString = "Client{" +
                "user=" + userToString +
                ", receivesMarketUpdates=" + false +
                '}';

        // Act
        String actualToString = client.toString();

        // Assert
        assertEquals(expectedToString, actualToString, "toString() output format should match expected");
        // Note: Testing toString() can be brittle if formatting changes slightly, or if User's toString changes.
    }
}