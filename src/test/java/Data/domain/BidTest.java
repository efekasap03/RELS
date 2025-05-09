package Data.domain; // Ensure this matches the package of your Bid class

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

class BidTest {

    // --- Tests for java.sql.Timestamp Setters ---

    @Test
    void testSetBidTimestampFromSqlTimestamp_NonNull() {
        // Arrange
        Bid bid = new Bid();
        LocalDateTime now = LocalDateTime.now();
        Timestamp sqlTimestamp = Timestamp.valueOf(now);

        // Act
        bid.setBidTimestamp(sqlTimestamp);

        // Assert
        // Comparing LocalDateTime requires some care with nanoseconds
        // We check if the LocalDates and LocalTimes (up to seconds) match.
        // More precise checks might be needed depending on requirements, but this is usually sufficient.
        assertNotNull(bid.getBidTimestamp(), "bidTimestamp should not be null after setting a non-null SQL Timestamp");
        assertEquals(now.toLocalDate(), bid.getBidTimestamp().toLocalDate(), "Date part of bidTimestamp should match");
        assertEquals(now.toLocalTime().toSecondOfDay(), bid.getBidTimestamp().toLocalTime().toSecondOfDay(), "Time part (up to seconds) of bidTimestamp should match");
        // Optional: Check nanoseconds if conversion accuracy is critical, but involves potential rounding differences
        // assertEquals(sqlTimestamp.getNanos(), bid.getBidTimestamp().getNano(), "Nanosecond part should match");
    }

    @Test
    void testSetBidTimestampFromSqlTimestamp_Null() {
        // Arrange
        Bid bid = new Bid();
        Timestamp sqlTimestamp = null;

        // Act
        bid.setBidTimestamp(sqlTimestamp);

        // Assert
        assertNull(bid.getBidTimestamp(), "bidTimestamp should be null after setting a null SQL Timestamp");
    }

    @Test
    void testSetCreatedAtFromSqlTimestamp_NonNull() {
        // Arrange
        Bid bid = new Bid();
        LocalDateTime now = LocalDateTime.now();
        Timestamp sqlTimestamp = Timestamp.valueOf(now);

        // Act
        bid.setCreatedAt(sqlTimestamp);

        // Assert
        assertNotNull(bid.getCreatedAt(), "createdAt should not be null after setting a non-null SQL Timestamp");
        assertEquals(now.toLocalDate(), bid.getCreatedAt().toLocalDate(), "Date part of createdAt should match");
        assertEquals(now.toLocalTime().toSecondOfDay(), bid.getCreatedAt().toLocalTime().toSecondOfDay(), "Time part (up to seconds) of createdAt should match");
    }

    @Test
    void testSetCreatedAtFromSqlTimestamp_Null() {
        // Arrange
        Bid bid = new Bid();
        Timestamp sqlTimestamp = null;

        // Act
        bid.setCreatedAt(sqlTimestamp);

        // Assert
        assertNull(bid.getCreatedAt(), "createdAt should be null after setting a null SQL Timestamp");
    }

    @Test
    void testSetUpdatedAtFromSqlTimestamp_NonNull() {
        // Arrange
        Bid bid = new Bid();
        LocalDateTime now = LocalDateTime.now();
        Timestamp sqlTimestamp = Timestamp.valueOf(now);

        // Act
        bid.setUpdatedAt(sqlTimestamp);

        // Assert
        assertNotNull(bid.getUpdatedAt(), "updatedAt should not be null after setting a non-null SQL Timestamp");
        assertEquals(now.toLocalDate(), bid.getUpdatedAt().toLocalDate(), "Date part of updatedAt should match");
        assertEquals(now.toLocalTime().toSecondOfDay(), bid.getUpdatedAt().toLocalTime().toSecondOfDay(), "Time part (up to seconds) of updatedAt should match");
    }

    @Test
    void testSetUpdatedAtFromSqlTimestamp_Null() {
        // Arrange
        Bid bid = new Bid();
        Timestamp sqlTimestamp = null;

        // Act
        bid.setUpdatedAt(sqlTimestamp);

        // Assert
        assertNull(bid.getUpdatedAt(), "updatedAt should be null after setting a null SQL Timestamp");
    }

    // --- Tests for equals() and hashCode() ---

    @Test
    void testEquals_SameBidId() {
        // Arrange
        Bid bid1 = new Bid();
        bid1.setBidId("bid123");
        bid1.setAmount(BigDecimal.valueOf(1000)); // Different field value
        Bid bid2 = new Bid();
        bid2.setBidId("bid123");
        bid2.setStatus("PENDING"); // Different field value

        // Assert
        assertEquals(bid1, bid2, "Bids with the same bidId should be equal");
    }

    @Test
    void testEquals_DifferentBidId() {
        // Arrange
        Bid bid1 = new Bid();
        bid1.setBidId("bid123");
        Bid bid2 = new Bid();
        bid2.setBidId("bid456");

        // Assert
        assertNotEquals(bid1, bid2, "Bids with different bidId should not be equal");
    }

    @Test
    void testEquals_NullObject() {
        // Arrange
        Bid bid1 = new Bid();
        bid1.setBidId("bid123");
        Bid bid2 = null;

        // Assert
        assertNotEquals(bid1, bid2, "Bid should not be equal to null");
    }

    @Test
    void testEquals_DifferentClass() {
        // Arrange
        Bid bid1 = new Bid();
        bid1.setBidId("bid123");
        Object notABid = new Object();

        // Assert
        assertNotEquals(bid1, notABid, "Bid should not be equal to an object of a different class");
    }

    @Test
    void testEquals_SameObjectInstance() {
        // Arrange
        Bid bid1 = new Bid();
        bid1.setBidId("bid123");
        Bid bid2 = bid1; // Same instance

        // Assert
        assertEquals(bid1, bid2, "Bid should be equal to itself");
    }


    @Test
    void testHashCode_ConsistentForEqualObjects() {
        // Arrange
        Bid bid1 = new Bid();
        bid1.setBidId("bid123");
        bid1.setAmount(BigDecimal.valueOf(1000)); // Different field value, but should be ignored by equals/hashCode
        Bid bid2 = new Bid();
        bid2.setBidId("bid123");
        bid2.setStatus("ACCEPTED"); // Different field value, but should be ignored by equals/hashCode

        // Assert
        // If equals() returns true, hashCode() must return the same value
        assertEquals(bid1, bid2, "Bids should be equal for this test"); // Sanity check
        assertEquals(bid1.hashCode(), bid2.hashCode(), "Hash codes must be equal for equal objects");
    }

    @Test
    void testHashCode_ConsistentForSameObject() {
        // Arrange
        Bid bid1 = new Bid();
        bid1.setBidId("bid123");
        int hashCode1 = bid1.hashCode();

        // Act (get hash code multiple times)
        int hashCode2 = bid1.hashCode();
        int hashCode3 = bid1.hashCode();

        // Assert
        assertEquals(hashCode1, hashCode2, "Hash code must be consistent");
        assertEquals(hashCode1, hashCode3, "Hash code must be consistent");
    }

    // --- Optional: Basic Getter/Setter Test (for structure demonstration, low value) ---

    @Test
    void testSetAndGetAmount() {
        // Arrange
        Bid bid = new Bid();
        BigDecimal expectedAmount = new BigDecimal("1234.56");

        // Act
        bid.setAmount(expectedAmount);
        BigDecimal actualAmount = bid.getAmount();

        // Assert
        assertEquals(expectedAmount, actualAmount, "Set and get amount should retrieve the correct value");
        // Note: Testing all getters/setters like this is usually low priority.
    }

    @Test
    void testDefaultConstructorInitializesFieldsToDefaults() {
        // Arrange & Act
        Bid bid = new Bid();

        // Assert
        assertNull(bid.getBidId(), "Default constructor should initialize bidId to null");
        assertNull(bid.getPropertyId(), "Default constructor should initialize propertyId to null");
        assertNull(bid.getClientId(), "Default constructor should initialize clientId to null");
        assertNull(bid.getAmount(), "Default constructor should initialize amount to null");
        assertNull(bid.getStatus(), "Default constructor should initialize status to null");
        assertNull(bid.getBidTimestamp(), "Default constructor should initialize bidTimestamp to null");
        assertNull(bid.getCreatedAt(), "Default constructor should initialize createdAt to null");
        assertNull(bid.getUpdatedAt(), "Default constructor should initialize updatedAt to null");
    }


    @Test
    void testToString() {
         // Arrange
        Bid bid = new Bid();
        bid.setBidId("b456");
        bid.setPropertyId("p789");
        bid.setClientId("c101");
        bid.setAmount(new BigDecimal("500.00"));
        bid.setStatus("ACCEPTED");
        LocalDateTime timestamp = LocalDateTime.of(2023, 1, 15, 10, 30);
        bid.setBidTimestamp(timestamp);

        String expectedToString = "Bid{" +
                "bidId='b456'" +
                ", propertyId='p789'" +
                ", clientId='c101'" +
                ", amount=500.00" + // Note: BigDecimal.toString() might vary slightly in precision representation
                ", status='ACCEPTED'" +
                ", bidTimestamp=" + timestamp + // LocalDateTime.toString() format
                '}';

        // Act
        String actualToString = bid.toString();

        // Assert
        assertEquals(expectedToString, actualToString, "toString() output format should match expected");
    }
}