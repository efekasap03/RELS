package Data.connector; // Ensure this matches the package of your DatabaseConnectorImpl class

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*; // Import Mockito methods like mock(), verify(), when()

import java.sql.Connection;
import java.sql.SQLException;

// Note: Testing DatabaseConnectorImpl is challenging because its core
// functionality (getConnection()) relies on the static DriverManager,
// which cannot be easily mocked with standard Mockito/JUnit.
// These tests focus on the constructor validation and the closeConnection logic,
// which *can* be tested in isolation using mocks for the Connection object.
// A test for the actual connection attempt would be an Integration Test, not a Unit Test.
class DatabaseConnectorImplTest {

    private static final String TEST_URL = "jdbc:test://localhost:1234/testdb";
    private static final String TEST_USER = "testuser";
    private static final String TEST_PASSWORD = "testpassword";

    // We don't need @BeforeEach unless we had common setup for all tests,
    // but it's good practice to include it if needed later.

    // --- Constructor Tests ---

    @Test
    void testConstructor_ValidArguments() {
        // Arrange, Act
        DatabaseConnectorImpl connector = new DatabaseConnectorImpl(TEST_URL, TEST_USER, TEST_PASSWORD);

        // Assert
        assertNotNull(connector, "Constructor should create a non-null instance");
        assertEquals(TEST_URL, connector.getDbUrl(), "Constructor should set the database URL");
        assertEquals(TEST_USER, connector.getDbUser(), "Constructor should set the database user");
        // Note: The password is not exposed by a getter, so we cannot assert it directly,
        // but we trust that private fields are set correctly if others are.
    }

    @Test
    void testConstructor_NullDbUrlThrowsException() {
        // Arrange, Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new DatabaseConnectorImpl(null, TEST_USER, TEST_PASSWORD);
        }, "Constructor should throw IllegalArgumentException for null URL");

        assertTrue(exception.getMessage().contains("Database URL, user, and password cannot be null."),
                "Exception message should indicate null arguments");
    }

    @Test
    void testConstructor_NullDbUserThrowsException() {
        // Arrange, Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new DatabaseConnectorImpl(TEST_URL, null, TEST_PASSWORD);
        }, "Constructor should throw IllegalArgumentException for null user");

        assertTrue(exception.getMessage().contains("Database URL, user, and password cannot be null."),
                "Exception message should indicate null arguments");
    }

    @Test
    void testConstructor_NullDbPasswordThrowsException() {
        // Arrange, Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new DatabaseConnectorImpl(TEST_URL, TEST_USER, null);
        }, "Constructor should throw IllegalArgumentException for null password");

        assertTrue(exception.getMessage().contains("Database URL, user, and password cannot be null."),
                "Exception message should indicate null arguments");
    }

    // --- Getter Tests (Partially covered by Constructor tests, but explicitly) ---

    @Test
    void testGetDbUrl() {
        // Arrange
        DatabaseConnectorImpl connector = new DatabaseConnectorImpl(TEST_URL, TEST_USER, TEST_PASSWORD);

        // Act
        String url = connector.getDbUrl();

        // Assert
        assertEquals(TEST_URL, url, "getDbUrl should return the URL passed to the constructor");
    }

    @Test
    void testGetDbUser() {
        // Arrange
        DatabaseConnectorImpl connector = new DatabaseConnectorImpl(TEST_URL, TEST_USER, TEST_PASSWORD);

        // Act
        String user = connector.getDbUser();

        // Assert
        assertEquals(TEST_USER, user, "getDbUser should return the user passed to the constructor");
    }

    // --- closeConnection Tests ---

    @Test
    void testCloseConnection_NonNullAndOpen() throws SQLException {
        // Arrange
        DatabaseConnectorImpl connector = new DatabaseConnectorImpl(TEST_URL, TEST_USER, TEST_PASSWORD);
        Connection mockConnection = mock(Connection.class); // Create a mock Connection
        when(mockConnection.isClosed()).thenReturn(false); // Tell the mock to return false for isClosed()

        // Act
        connector.closeConnection(mockConnection);

        // Assert
        // Verify that close() was called on the mock Connection exactly once
        verify(mockConnection, times(1)).close();
        // Verify that isClosed() was called before attempting to close
        verify(mockConnection, times(1)).isClosed();
    }

    @Test
    void testCloseConnection_NonNullAndAlreadyClosed() throws SQLException {
        // Arrange
        DatabaseConnectorImpl connector = new DatabaseConnectorImpl(TEST_URL, TEST_USER, TEST_PASSWORD);
        Connection mockConnection = mock(Connection.class); // Create a mock Connection
        when(mockConnection.isClosed()).thenReturn(true); // Tell the mock to return true for isClosed()

        // Act
        connector.closeConnection(mockConnection);

        // Assert
        // Verify that close() was *never* called on the mock Connection
        verify(mockConnection, never()).close();
        // Verify that isClosed() was called
        verify(mockConnection, times(1)).isClosed();
    }


    @Test
    void testCloseConnection_NullConnection() throws SQLException {
        // Arrange
        DatabaseConnectorImpl connector = new DatabaseConnectorImpl(TEST_URL, TEST_USER, TEST_PASSWORD);
        Connection nullConnection = null;

        // Act & Assert
        // Ensure that calling closeConnection with null does not throw an exception
        assertDoesNotThrow(() -> {
            connector.closeConnection(nullConnection);
        }, "closeConnection with null should not throw an exception");

        // Since no mock was involved, no verify calls are needed.
        // This implicitly confirms no interaction happened with a non-existent object.
    }

    @Test
    void testCloseConnection_ThrowsSQLException_WhenClosing() throws SQLException {
        // Arrange
        DatabaseConnectorImpl connector = new DatabaseConnectorImpl(TEST_URL, TEST_USER, TEST_PASSWORD);
        Connection mockConnection = mock(Connection.class); // Create a mock Connection
        when(mockConnection.isClosed()).thenReturn(false); // Mock it as open
        // Configure the mock's close() method to throw a SQLException
        SQLException expectedException = new SQLException("Simulated close error");
        doThrow(expectedException).when(mockConnection).close();

        // Act & Assert
        // Verify that closeConnection re-throws the SQLException
        SQLException thrownException = assertThrows(SQLException.class, () -> {
            connector.closeConnection(mockConnection);
        }, "closeConnection should re-throw SQLException from Connection.close()");

        assertEquals(expectedException, thrownException, "The re-thrown exception should be the original one");

        // Verify that isClosed() and close() were called
        verify(mockConnection, times(1)).isClosed();
        verify(mockConnection, times(1)).close();
    }


}