package Data.domain; // Ensure this matches the package of your User class

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

class UserTest {

    // --- Tests for Constructors ---

    @Test
    void testDefaultConstructorInitializesFieldsToDefaults() {
        // Arrange & Act
        User user = new User();

        // Assert
        assertNotNull(user, "User object should not be null");
        // All reference type fields should be initialized to null by the default constructor
        assertNull(user.getUserId(), "Default constructor should initialize userId to null");
        assertNull(user.getName(), "Default constructor should initialize name to null");
        assertNull(user.getEmail(), "Default constructor should initialize email to null");
        assertNull(user.getPasswordHash(), "Default constructor should initialize passwordHash to null");
        assertNull(user.getPhoneNumber(), "Default constructor should initialize phoneNumber to null");
        assertNull(user.getRole(), "Default constructor should initialize role to null");
        assertNull(user.getCreatedAt(), "Default constructor should initialize createdAt to null");
        assertNull(user.getUpdatedAt(), "Default constructor should initialize updatedAt to null");
        // Primitive boolean should be initialized to false
        assertFalse(user.isVerified(), "Default constructor should initialize isVerified to false");
    }

    @Test
    void testParameterizedConstructorInitializesFields() {
        // Arrange
        String userId = "u123";
        String name = "Test User";
        String email = "test@example.com";
        String passwordHash = "hashedpassword123";
        String role = "CLIENT"; // Or "LANDLORD", "ADMIN"

        // Act
        User user = new User(userId, name, email, passwordHash, role);

        // Assert
        assertNotNull(user, "User object should not be null");
        assertEquals(userId, user.getUserId(), "Parameterized constructor should set userId");
        assertEquals(name, user.getName(), "Parameterized constructor should set name");
        assertEquals(email, user.getEmail(), "Parameterized constructor should set email");
        assertEquals(passwordHash, user.getPasswordHash(), "Parameterized constructor should set passwordHash");
        assertEquals(role, user.getRole(), "Parameterized constructor should set role");

        // Other fields not in this constructor should have default values
        assertNull(user.getPhoneNumber(), "Parameterized constructor should initialize phoneNumber to null");
        assertFalse(user.isVerified(), "Parameterized constructor should initialize isVerified to false");
        assertNull(user.getCreatedAt(), "Parameterized constructor should initialize createdAt to null");
        assertNull(user.getUpdatedAt(), "Parameterized constructor should initialize updatedAt to null");
    }


    // --- Tests for java.sql.Timestamp Setters ---

    @Test
    void testSetCreatedAtFromSqlTimestamp_NonNull() {
        // Arrange
        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        Timestamp sqlTimestamp = Timestamp.valueOf(now);

        // Act
        user.setCreatedAt(sqlTimestamp);

        // Assert
        assertNotNull(user.getCreatedAt(), "createdAt should not be null after setting a non-null SQL Timestamp");
        // Compare up to seconds to avoid nanosecond precision issues during conversion
        assertEquals(now.toLocalDate(), user.getCreatedAt().toLocalDate(), "Date part of createdAt should match");
        assertEquals(now.toLocalTime().toSecondOfDay(), user.getCreatedAt().toLocalTime().toSecondOfDay(), "Time part (up to seconds) of createdAt should match");
        // Optional: If nanosecond precision is critical, compare getNano(), but be aware of potential rounding.
        // assertEquals(sqlTimestamp.getNanos(), user.getCreatedAt().getNano(), "Nanosecond part should match");
    }

    @Test
    void testSetCreatedAtFromSqlTimestamp_Null() {
        // Arrange
        User user = new User();
        Timestamp sqlTimestamp = null;

        // Act
        user.setCreatedAt(sqlTimestamp);

        // Assert
        assertNull(user.getCreatedAt(), "createdAt should be null after setting a null SQL Timestamp");
    }

    @Test
    void testSetUpdatedAtFromSqlTimestamp_NonNull() {
        // Arrange
        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        Timestamp sqlTimestamp = Timestamp.valueOf(now);

        // Act
        user.setUpdatedAt(sqlTimestamp);

        // Assert
        assertNotNull(user.getUpdatedAt(), "updatedAt should not be null after setting a non-null SQL Timestamp");
        assertEquals(now.toLocalDate(), user.getUpdatedAt().toLocalDate(), "Date part of updatedAt should match");
        assertEquals(now.toLocalTime().toSecondOfDay(), user.getUpdatedAt().toLocalTime().toSecondOfDay(), "Time part (up to seconds) of updatedAt should match");
    }

    @Test
    void testSetUpdatedAtFromSqlTimestamp_Null() {
        // Arrange
        User user = new User();
        Timestamp sqlTimestamp = null;

        // Act
        user.setUpdatedAt(sqlTimestamp);

        // Assert
        assertNull(user.getUpdatedAt(), "updatedAt should be null after setting a null SQL Timestamp");
    }

    // --- Tests for Specific Field Types (Getters/Setters) ---

    @Test
    void testSetAndGetUserId() {
        // Arrange
        User user = new User();
        String expectedUserId = "uniqueUser123";

        // Act
        user.setUserId(expectedUserId);
        String actualUserId = user.getUserId();

        // Assert
        assertEquals(expectedUserId, actualUserId, "Set and get userId should retrieve the correct value");
    }

    @Test
    void testSetAndGetIsVerified_True() {
        // Arrange
        User user = new User();
        boolean expectedValue = true;

        // Act
        user.setVerified(expectedValue);
        boolean actualValue = user.isVerified();

        // Assert
        assertEquals(expectedValue, actualValue, "Set and get isVerified should retrieve the 'true' value");
    }

    @Test
    void testSetAndGetIsVerified_False() {
        // Arrange
        User user = new User();
        // Set to true first to ensure false overwrites it
        user.setVerified(true);
        assertTrue(user.isVerified(), "IsVerified should be true initially for this test setup");

        boolean expectedValue = false;

        // Act
        user.setVerified(expectedValue);
        boolean actualValue = user.isVerified();

        // Assert
        assertEquals(expectedValue, actualValue, "Set and get isVerified should retrieve the 'false' value");
    }


    // Note: Tests for simple getters/setters like getName, getEmail, etc.,
    // are omitted as they are boilerplate and do not contain custom logic.

    // --- Tests for equals() and hashCode() ---

    @Test
    void testEquals_SameUserId() {
        // Arrange
        User user1 = new User();
        user1.setUserId("userABC");
        user1.setName("Name A"); // Different field value
        user1.setEmail("a@test.com"); // Different field value

        User user2 = new User();
        user2.setUserId("userABC");
        user2.setName("Name B"); // Different field value
        user2.setPhoneNumber("555-1234"); // Different field value

        // Assert
        assertEquals(user1, user2, "Users with the same userId should be equal");
    }

    @Test
    void testEquals_DifferentUserId() {
        // Arrange
        User user1 = new User();
        user1.setUserId("userABC");
        User user2 = new User();
        user2.setUserId("userXYZ");

        // Assert
        assertNotEquals(user1, user2, "Users with different userId should not be equal");
    }

    @Test
    void testEquals_NullObject() {
        // Arrange
        User user1 = new User();
        user1.setUserId("userABC");
        User user2 = null;

        // Assert
        assertNotEquals(user1, user2, "User should not be equal to null");
    }

    @Test
    void testEquals_DifferentClass() {
        // Arrange
        User user1 = new User();
        user1.setUserId("userABC");
        Object notAUser = new Object();

        // Assert
        assertNotEquals(user1, notAUser, "User should not be equal to an object of a different class");
    }

    @Test
    void testEquals_SameObjectInstance() {
        // Arrange
        User user1 = new User();
        user1.setUserId("userABC");
        User user2 = user1; // Same instance

        // Assert
        assertEquals(user1, user2, "User should be equal to itself");
    }


    @Test
    void testHashCode_ConsistentForEqualObjects() {
        // Arrange
        User user1 = new User();
        user1.setUserId("userABC");
        user1.setName("Name A"); // Different field value, ignored by equals/hashCode
        User user2 = new User();
        user2.setUserId("userABC");
        user2.setName("Name B"); // Different field value, ignored by equals/hashCode

        // Assert
        // If equals() returns true, hashCode() must return the same value
        assertEquals(user1, user2, "Users should be equal for this test"); // Sanity check
        assertEquals(user1.hashCode(), user2.hashCode(), "Hash codes must be equal for equal objects");
    }

    @Test
    void testHashCode_ConsistentForSameObject() {
        // Arrange
        User user1 = new User();
        user1.setUserId("userABC");
        int hashCode1 = user1.hashCode();

        // Act (get hash code multiple times)
        int hashCode2 = user1.hashCode();
        int hashCode3 = user1.hashCode();

        // Assert
        assertEquals(hashCode1, hashCode2, "Hash code must be consistent");
        assertEquals(hashCode1, hashCode3, "Hash code must be consistent");
    }

    // --- Testing toString() ---
    @Test
    void testToString() {
        // Arrange
        User user = new User(); // Use the default constructor
        user.setUserId("u456");
        user.setName("Sam");
        user.setEmail("sam@test.com");
        user.setPasswordHash("hashed");
        user.setPhoneNumber("555-5678");
        user.setRole("ADMIN");
        user.setVerified(true);
        LocalDateTime createdAt = LocalDateTime.of(2023, 10, 20, 9, 0);
        user.setCreatedAt(createdAt);
        LocalDateTime updatedAt = LocalDateTime.of(2023, 10, 20, 10, 0);
        user.setUpdatedAt(updatedAt);


        String expectedToString = "User{" +
                "userId='" + "u456" + '\'' +
                ", name='" + "Sam" + '\'' +
                ", email='" + "sam@test.com" + '\'' +
                ", phoneNumber='" + "555-5678" + '\'' +
                ", role='" + "ADMIN" + '\'' +
                ", isVerified=" + "true" + // boolean.toString() is "true" or "false"
                ", createdAt=" + user.getCreatedAt() + // LocalDateTime.toString() format
                ", updatedAt=" + user.getUpdatedAt() + // LocalDateTime.toString() format
                '}';

        // Act
        String actualToString = user.toString();

        // Assert
        assertEquals(expectedToString, actualToString, "toString() output format should match expected");
        // Note: Testing toString() can be brittle if formatting changes or fields are added/removed from the output.
    }
}