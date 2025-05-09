package Data.repository.impl;

import Data.connector.IDatabaseConnector;
import Data.domain.Client;
import Data.domain.Landlord;
import Data.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock
    private IDatabaseConnector mockConnector;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    @InjectMocks
    private UserRepositoryImpl userRepository;

    private User genericUser;
    private Landlord landlord;
    private Client client;
    private Timestamp fixedTimestamp;

    @BeforeEach
    void setUp() throws SQLException {
        // Common setup for mock interactions, can be overridden in specific tests
        lenient().when(mockConnector.getConnection()).thenReturn(mockConnection);
        lenient().when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        fixedTimestamp = new Timestamp(System.currentTimeMillis());

        genericUser = new User();
        genericUser.setUserId(UUID.randomUUID().toString());
        genericUser.setName("Generic User");
        genericUser.setEmail("generic@example.com");
        genericUser.setPasswordHash("genericHash");
        genericUser.setPhoneNumber("1234567890");
        genericUser.setRole("USER");
        genericUser.setVerified(true);
        genericUser.setCreatedAt(fixedTimestamp);
        genericUser.setUpdatedAt(fixedTimestamp);

        landlord = new Landlord();
        landlord.setUserId(UUID.randomUUID().toString());
        landlord.setName("Landlord User");
        landlord.setEmail("landlord@example.com");
        landlord.setPasswordHash("landlordHash");
        landlord.setPhoneNumber("0987654321");
        landlord.setRole("LANDLORD");
        landlord.setVerified(true);
        landlord.setAgentLicenseNumber("LIC123");
        landlord.setCreatedAt(fixedTimestamp);
        landlord.setUpdatedAt(fixedTimestamp);

        client = new Client();
        client.setUserId(UUID.randomUUID().toString());
        client.setName("Client User");
        client.setEmail("client@example.com");
        client.setPasswordHash("clientHash");
        client.setPhoneNumber("1122334455");
        client.setRole("CLIENT");
        client.setVerified(false);
        client.setReceivesMarketUpdates(true);
        client.setCreatedAt(fixedTimestamp);
        client.setUpdatedAt(fixedTimestamp);
    }

    // --- Constructor Tests ---
    @Test
    @DisplayName("Constructor should throw IllegalArgumentException for null connector")
    void constructor_nullConnector_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new UserRepositoryImpl(null);
        });
        assertEquals("Database connector cannot be null.", exception.getMessage());
    }

    // --- addUser Tests ---
    private void setupAddUserMocks(User user) throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
    }

    @Test
    @DisplayName("addUser should add a generic User successfully")
    void addUser_genericUser_success() throws SQLException {
        setupAddUserMocks(genericUser);
        boolean result = userRepository.addUser(genericUser);

        assertTrue(result);
        ArgumentCaptor<String> stringArgCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> boolArgCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<Integer> intArgCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(mockPreparedStatement).setString(1, genericUser.getUserId());
        verify(mockPreparedStatement).setString(2, genericUser.getName());
        verify(mockPreparedStatement).setString(3, genericUser.getEmail());
        verify(mockPreparedStatement).setString(4, genericUser.getPasswordHash());
        verify(mockPreparedStatement).setString(5, genericUser.getPhoneNumber());
        verify(mockPreparedStatement).setString(6, genericUser.getRole());
        verify(mockPreparedStatement).setBoolean(7, genericUser.isVerified());
        verify(mockPreparedStatement).setNull(8, Types.VARCHAR);    // agent_license_number
        verify(mockPreparedStatement).setNull(9, Types.BOOLEAN); // receives_market_updates
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("addUser should add a Landlord successfully")
    void addUser_landlord_success() throws SQLException {
        setupAddUserMocks(landlord);
        boolean result = userRepository.addUser(landlord);

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, landlord.getUserId());
        verify(mockPreparedStatement).setString(8, landlord.getAgentLicenseNumber());
        verify(mockPreparedStatement).setNull(9, Types.BOOLEAN);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("addUser should add a Client successfully")
    void addUser_client_success() throws SQLException {
        setupAddUserMocks(client);
        boolean result = userRepository.addUser(client);

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, client.getUserId());
        verify(mockPreparedStatement).setNull(8, Types.VARCHAR);
        verify(mockPreparedStatement).setBoolean(9, client.isReceivesMarketUpdates());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("addUser should return false for null user")
    void addUser_nullUser_returnsFalse() throws SQLException {
        assertFalse(userRepository.addUser(null));
        verify(mockConnector, never()).getConnection();
    }

    @Test
    @DisplayName("addUser should return false for user with null ID")
    void addUser_nullUserId_returnsFalse() throws SQLException {
        genericUser.setUserId(null);
        assertFalse(userRepository.addUser(genericUser));
        verify(mockConnector, never()).getConnection();
    }
    @Test
    @DisplayName("addUser should return false for user with null Role")
    void addUser_nullRole_returnsFalse() throws SQLException {
        genericUser.setRole(null);
        assertFalse(userRepository.addUser(genericUser));
        verify(mockConnector, never()).getConnection();
    }


    @Test
    @DisplayName("addUser should return false if executeUpdate returns 0")
    void addUser_executeUpdateReturnsZero_returnsFalse() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        assertFalse(userRepository.addUser(genericUser));
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("addUser should throw SQLException on connection error")
    void addUser_throwsSQLExceptionOnConnectionError() throws SQLException {
        when(mockConnector.getConnection()).thenThrow(new SQLException("Connection failed"));
        assertThrows(SQLException.class, () -> userRepository.addUser(genericUser));
    }

    // --- getUserById / getUserByEmail (tests getUserBy private method) ---
    private void mockResultSetForUser(User user, String roleOverride) throws SQLException {
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSet.getString("user_id")).thenReturn(user.getUserId());
        when(mockResultSet.getString("name")).thenReturn(user.getName());
        when(mockResultSet.getString("email")).thenReturn(user.getEmail());
        when(mockResultSet.getString("password_hash")).thenReturn(user.getPasswordHash());
        when(mockResultSet.getString("phone_number")).thenReturn(user.getPhoneNumber());
        String role = roleOverride != null ? roleOverride : user.getRole();
        when(mockResultSet.getString("role")).thenReturn(role);
        when(mockResultSet.getBoolean("is_verified")).thenReturn(user.isVerified());

        // Corrected lines:
        LocalDateTime userCreatedAt = user.getCreatedAt(); // Assuming this returns LocalDateTime
        LocalDateTime userUpdatedAt = user.getUpdatedAt(); // Assuming this returns LocalDateTime

        when(mockResultSet.getTimestamp("created_at"))
                .thenReturn(userCreatedAt != null ? Timestamp.valueOf(userCreatedAt) : null);
        when(mockResultSet.getTimestamp("updated_at"))
                .thenReturn(userUpdatedAt != null ? Timestamp.valueOf(userUpdatedAt) : null);

        if ("LANDLORD".equalsIgnoreCase(role) && user instanceof Landlord) {
            when(mockResultSet.getString("agent_license_number")).thenReturn(((Landlord) user).getAgentLicenseNumber());
        } else if ("CLIENT".equalsIgnoreCase(role) && user instanceof Client) {
            boolean receivesUpdates = ((Client) user).isReceivesMarketUpdates();
            when(mockResultSet.getBoolean("receives_market_updates")).thenReturn(receivesUpdates);
            // Ensure wasNull is mocked appropriately if relies on these specific objects
            // For this specific mock helper, it's often simpler to assume not null for true, or handle it in calling test.
            // Based on current tests, wasNull is generally mocked AFTER getBoolean.
            // If receives_market_updates could be null, then user.isReceivesMarketUpdates() for a Client
            // might need to reflect that, or the mock setup here for it too.
            // However, the timestamp was the direct issue.
        }
    }
    private void mockResultSetForClientReceivesMarketUpdates(Client client, boolean receives, boolean wasNull) throws SQLException {
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSet.getString("user_id")).thenReturn(client.getUserId());
        when(mockResultSet.getString("name")).thenReturn(client.getName());
        when(mockResultSet.getString("email")).thenReturn(client.getEmail());
        // ... other common fields
        when(mockResultSet.getString("role")).thenReturn("CLIENT");
        when(mockResultSet.getBoolean("receives_market_updates")).thenReturn(receives);
        when(mockResultSet.wasNull()).thenReturn(wasNull);
    }


    @Test
    @DisplayName("getUserById should return generic User")
    void getUserById_genericUserFound() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        mockResultSetForUser(genericUser, "USER"); // Explicitly set role for clarity

        Optional<User> result = userRepository.getUserById(genericUser.getUserId());

        assertTrue(result.isPresent());
        User foundUser = result.get();
        assertEquals(genericUser.getUserId(), foundUser.getUserId());
        assertEquals("USER", foundUser.getRole());
        assertFalse(foundUser instanceof Landlord);
        assertFalse(foundUser instanceof Client);
        verify(mockPreparedStatement).setString(1, genericUser.getUserId());
    }

    @Test
    @DisplayName("getUserById should return Landlord")
    void getUserById_landlordFound() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        mockResultSetForUser(landlord, "LANDLORD");

        Optional<User> result = userRepository.getUserById(landlord.getUserId());

        assertTrue(result.isPresent());
        User foundUser = result.get();
        assertTrue(foundUser instanceof Landlord);
        Landlord foundLandlord = (Landlord) foundUser;
        assertEquals(landlord.getUserId(), foundLandlord.getUserId());
        assertEquals("LANDLORD", foundLandlord.getRole());
        assertEquals(landlord.getAgentLicenseNumber(), foundLandlord.getAgentLicenseNumber());
        verify(mockPreparedStatement).setString(1, landlord.getUserId());
    }

    @Test
    @DisplayName("getUserById should return Client with market updates true")
    void getUserById_clientFound_marketUpdatesTrue() throws SQLException {
        client.setReceivesMarketUpdates(true);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        mockResultSetForUser(client, "CLIENT"); // wasNull will be false by default in helper

        Optional<User> result = userRepository.getUserById(client.getUserId());

        assertTrue(result.isPresent());
        User foundUser = result.get();
        assertTrue(foundUser instanceof Client);
        Client foundClient = (Client) foundUser;
        assertEquals(client.getUserId(), foundClient.getUserId());
        assertEquals("CLIENT", foundClient.getRole());
        assertTrue(foundClient.isReceivesMarketUpdates());
        verify(mockPreparedStatement).setString(1, client.getUserId());
    }

    @Test
    @DisplayName("getUserById should return Client with market updates false (DB value false, not null)")
    void getUserById_clientFound_marketUpdatesFalse_dbValueFalse() throws SQLException {
        client.setReceivesMarketUpdates(false); // Set this for consistency with mock
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        // Mock ResultSet specifically for this case
        mockResultSetForClientReceivesMarketUpdates(client, false, false);

        Optional<User> result = userRepository.getUserById(client.getUserId());

        assertTrue(result.isPresent());
        User foundUser = result.get();
        assertTrue(foundUser instanceof Client);
        Client foundClient = (Client) foundUser;
        assertFalse(foundClient.isReceivesMarketUpdates());
        verify(mockResultSet).wasNull(); // Ensure wasNull was checked
    }

    @Test
    @DisplayName("getUserById should return Client with market updates false (DB value NULL)")
    void getUserById_clientFound_marketUpdatesFalse_dbValueNull() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        // Mock ResultSet specifically for this case
        // The actual boolean value from getBoolean("receives_market_updates") doesn't matter if wasNull is true
        mockResultSetForClientReceivesMarketUpdates(client, false, true);

        Optional<User> result = userRepository.getUserById(client.getUserId());

        assertTrue(result.isPresent());
        User foundUser = result.get();
        assertTrue(foundUser instanceof Client);
        Client foundClient = (Client) foundUser;
        assertFalse(foundClient.isReceivesMarketUpdates());
        verify(mockResultSet).wasNull(); // Ensure wasNull was checked
    }


    @Test
    @DisplayName("getUserById should return empty Optional if user not found")
    void getUserById_userNotFound() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Optional<User> result = userRepository.getUserById("nonexistent");

        assertTrue(result.isEmpty());
        verify(mockPreparedStatement).setString(1, "nonexistent");
    }

    @Test
    @DisplayName("getUserById should return empty Optional for null userId")
    void getUserById_nullUserId_returnsEmpty() throws SQLException {
        Optional<User> result = userRepository.getUserById(null);
        assertTrue(result.isEmpty());
        verify(mockConnector, never()).getConnection();
    }

    @Test
    @DisplayName("getUserById should return empty Optional for empty userId")
    void getUserById_emptyUserId_returnsEmpty() throws SQLException {
        Optional<User> result = userRepository.getUserById("   ");
        assertTrue(result.isEmpty());
        verify(mockConnector, never()).getConnection();
    }

    @Test
    @DisplayName("getUserByEmail should return generic User")
    void getUserByEmail_genericUserFound() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        mockResultSetForUser(genericUser, "USER");

        Optional<User> result = userRepository.getUserByEmail(genericUser.getEmail());

        assertTrue(result.isPresent());
        assertEquals(genericUser.getEmail(), result.get().getEmail());
        verify(mockPreparedStatement).setString(1, genericUser.getEmail());
    }

    @Test
    @DisplayName("getUserByEmail should return empty Optional for null email")
    void getUserByEmail_nullEmail_returnsEmpty() throws SQLException {
        Optional<User> result = userRepository.getUserByEmail(null);
        assertTrue(result.isEmpty());
        verify(mockConnector, never()).getConnection();
    }

    @Test
    @DisplayName("getUserByEmail should return empty Optional for empty email")
    void getUserByEmail_emptyEmail_returnsEmpty() throws SQLException {
        Optional<User> result = userRepository.getUserByEmail("   ");
        assertTrue(result.isEmpty());
        verify(mockConnector, never()).getConnection();
    }


    // --- updateUser Tests ---
    @Test
    @DisplayName("updateUser should update generic User successfully")
    void updateUser_genericUser_success() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        boolean result = userRepository.updateUser(genericUser);

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, genericUser.getName());
        verify(mockPreparedStatement).setString(2, genericUser.getEmail());
        // ... other fields
        verify(mockPreparedStatement).setNull(7, Types.VARCHAR); // agent_license_number
        verify(mockPreparedStatement).setNull(8, Types.BOOLEAN); // receives_market_updates
        verify(mockPreparedStatement).setString(9, genericUser.getUserId());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("updateUser should update Landlord successfully")
    void updateUser_landlord_success() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        boolean result = userRepository.updateUser(landlord);

        assertTrue(result);
        verify(mockPreparedStatement).setString(7, landlord.getAgentLicenseNumber());
        verify(mockPreparedStatement).setNull(8, Types.BOOLEAN);
        verify(mockPreparedStatement).setString(9, landlord.getUserId());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("updateUser should update Client successfully")
    void updateUser_client_success() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        boolean result = userRepository.updateUser(client);

        assertTrue(result);
        verify(mockPreparedStatement).setNull(7, Types.VARCHAR);
        verify(mockPreparedStatement).setBoolean(8, client.isReceivesMarketUpdates());
        verify(mockPreparedStatement).setString(9, client.getUserId());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("updateUser should return false for null user")
    void updateUser_nullUser_returnsFalse() throws SQLException {
        assertFalse(userRepository.updateUser(null));
        verify(mockConnector, never()).getConnection();
    }

    @Test
    @DisplayName("updateUser should return false for user with null ID")
    void updateUser_nullUserId_returnsFalse() throws SQLException {
        genericUser.setUserId(null);
        assertFalse(userRepository.updateUser(genericUser));
        verify(mockConnector, never()).getConnection();
    }

    @Test
    @DisplayName("updateUser should return false if executeUpdate returns 0")
    void updateUser_executeUpdateReturnsZero_returnsFalse() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        assertFalse(userRepository.updateUser(genericUser));
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("updateUser should throw SQLException on connection error")
    void updateUser_throwsSQLExceptionOnConnectionError() throws SQLException {
        when(mockConnector.getConnection()).thenThrow(new SQLException("Connection failed"));
        assertThrows(SQLException.class, () -> userRepository.updateUser(genericUser));
    }

    // --- deleteUser Tests ---
    @Test
    @DisplayName("deleteUser should delete user successfully")
    void deleteUser_success() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        String userIdToDelete = "userId123";
        boolean result = userRepository.deleteUser(userIdToDelete);

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, userIdToDelete);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("deleteUser should return false if user not found (executeUpdate returns 0)")
    void deleteUser_userNotFound_returnsFalse() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        String userIdToDelete = "userId123";
        boolean result = userRepository.deleteUser(userIdToDelete);

        assertFalse(result);
        verify(mockPreparedStatement).setString(1, userIdToDelete);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("deleteUser should return false for null userId")
    void deleteUser_nullUserId_returnsFalse() throws SQLException {
        assertFalse(userRepository.deleteUser(null));
        verify(mockConnector, never()).getConnection();
    }

    @Test
    @DisplayName("deleteUser should return false for empty userId")
    void deleteUser_emptyUserId_returnsFalse() throws SQLException {
        assertFalse(userRepository.deleteUser("  "));
        verify(mockConnector, never()).getConnection();
    }

    @Test
    @DisplayName("deleteUser should throw SQLException on connection error")
    void deleteUser_throwsSQLExceptionOnConnectionError() throws SQLException {
        when(mockConnector.getConnection()).thenThrow(new SQLException("Connection failed"));
        assertThrows(SQLException.class, () -> userRepository.deleteUser("userId123"));
    }

    // --- getAllLandlords Tests ---
    @Test
    @DisplayName("getAllLandlords should return list of landlords")
    void getAllLandlords_multipleFound() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        // Mock first landlord
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false); // Two landlords
        when(mockResultSet.getString("user_id")).thenReturn(landlord.getUserId(), "landlordId2");
        when(mockResultSet.getString("name")).thenReturn(landlord.getName(), "Landlord Two");
        when(mockResultSet.getString("email")).thenReturn(landlord.getEmail(), "landlord2@example.com");
        when(mockResultSet.getString("password_hash")).thenReturn(landlord.getPasswordHash(), "hash2");
        when(mockResultSet.getString("phone_number")).thenReturn(landlord.getPhoneNumber(), "2222222222");
        when(mockResultSet.getString("role")).thenReturn("LANDLORD", "LANDLORD");
        when(mockResultSet.getBoolean("is_verified")).thenReturn(true, false);
        when(mockResultSet.getString("agent_license_number")).thenReturn(landlord.getAgentLicenseNumber(), "LIC456");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(fixedTimestamp, fixedTimestamp);
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(fixedTimestamp, fixedTimestamp);

        List<Landlord> landlords = userRepository.getAllLandlords();

        assertEquals(2, landlords.size());
        assertEquals(landlord.getUserId(), landlords.get(0).getUserId());
        assertEquals("LIC456", landlords.get(1).getAgentLicenseNumber());
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    @DisplayName("getAllLandlords should return empty list if none found")
    void getAllLandlords_noneFound() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Landlord> landlords = userRepository.getAllLandlords();

        assertTrue(landlords.isEmpty());
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    @DisplayName("getAllLandlords should throw SQLException on connection error")
    void getAllLandlords_throwsSQLExceptionOnConnectionError() throws SQLException {
        when(mockConnector.getConnection()).thenThrow(new SQLException("Connection failed"));
        assertThrows(SQLException.class, () -> userRepository.getAllLandlords());
    }

    // --- getAllClients Tests ---
    @Test
    @DisplayName("getAllClients should return list of clients with varied market updates")
    void getAllClients_multipleFound_variedMarketUpdates() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Prepare distinct data for three clients
        // Client 1 (using the global 'client' from setUp, but ensuring its fields are set for clarity here)
        client.setUserId("client1_id");
        client.setName("Client One");
        client.setEmail("client1@example.com");
        client.setPasswordHash("hash1");
        client.setPhoneNumber("1111111111");
        client.setRole("CLIENT");
        client.setVerified(true);
        client.setReceivesMarketUpdates(true);
        // Assuming client.createdAt/updatedAt are LocalDateTime from your domain object
        // and fixedTimestamp is a java.sql.Timestamp from setUp
        client.setCreatedAt(fixedTimestamp.toLocalDateTime());
        client.setUpdatedAt(fixedTimestamp.toLocalDateTime());


        Client client2 = new Client();
        client2.setUserId("client2_id");
        client2.setName("Client Two");
        client2.setEmail("client2@example.com");
        client2.setPasswordHash("hash2");
        client2.setPhoneNumber("2222222222");
        client2.setRole("CLIENT");
        client2.setVerified(false);
        client2.setReceivesMarketUpdates(false); // DB value false, not null
        client2.setCreatedAt(fixedTimestamp.toLocalDateTime().minusDays(1));
        client2.setUpdatedAt(fixedTimestamp.toLocalDateTime().minusDays(1));

        Client client3 = new Client(); // Market updates will be null in DB
        client3.setUserId("client3_id");
        client3.setName("Client Three");
        client3.setEmail("client3@example.com");
        client3.setPasswordHash("hash3");
        client3.setPhoneNumber("3333333333");
        client3.setRole("CLIENT");
        client3.setVerified(true);
        // The actual value of receivesMarketUpdates for client3 domain object doesn't matter here,
        // as rs.wasNull() will be true, making the effective value false.
        client3.setReceivesMarketUpdates(true); // Let's say it's true, to show wasNull takes precedence
        client3.setCreatedAt(fixedTimestamp.toLocalDateTime().minusDays(2));
        client3.setUpdatedAt(fixedTimestamp.toLocalDateTime().minusDays(2));

        // Mocking ResultSet.next() for 3 clients then false
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);

        // Stubbing ALL necessary columns for each client iteration
        when(mockResultSet.getString("user_id"))
                .thenReturn(client.getUserId())
                .thenReturn(client2.getUserId())
                .thenReturn(client3.getUserId());
        when(mockResultSet.getString("name"))
                .thenReturn(client.getName())
                .thenReturn(client2.getName())
                .thenReturn(client3.getName());
        when(mockResultSet.getString("email")) // <<< Stubbing for "email"
                .thenReturn(client.getEmail())
                .thenReturn(client2.getEmail())
                .thenReturn(client3.getEmail());
        when(mockResultSet.getString("password_hash"))
                .thenReturn(client.getPasswordHash())
                .thenReturn(client2.getPasswordHash())
                .thenReturn(client3.getPasswordHash());
        when(mockResultSet.getString("phone_number"))
                .thenReturn(client.getPhoneNumber())
                .thenReturn(client2.getPhoneNumber())
                .thenReturn(client3.getPhoneNumber());
        when(mockResultSet.getString("role")) // Role is always "CLIENT" for this query
                .thenReturn("CLIENT")
                .thenReturn("CLIENT")
                .thenReturn("CLIENT");
        when(mockResultSet.getBoolean("is_verified"))
                .thenReturn(client.isVerified())
                .thenReturn(client2.isVerified())
                .thenReturn(client3.isVerified());

        // Specifics for receives_market_updates and wasNull
        when(mockResultSet.getBoolean("receives_market_updates"))
                .thenReturn(client.isReceivesMarketUpdates()) // true for client 1
                .thenReturn(client2.isReceivesMarketUpdates()) // false for client 2
                .thenReturn(true); // For client 3, this raw value is true, but wasNull will override
        when(mockResultSet.wasNull())
                .thenReturn(false) // client 1: not null
                .thenReturn(false) // client 2: not null
                .thenReturn(true);  // client 3: null in DB (so market updates effectively becomes false)

        // Timestamps (converting LocalDateTime from domain objects to java.sql.Timestamp for mock)
        when(mockResultSet.getTimestamp("created_at"))
                .thenReturn(Timestamp.valueOf(client.getCreatedAt()))
                .thenReturn(Timestamp.valueOf(client2.getCreatedAt()))
                .thenReturn(Timestamp.valueOf(client3.getCreatedAt()));
        when(mockResultSet.getTimestamp("updated_at"))
                .thenReturn(Timestamp.valueOf(client.getUpdatedAt()))
                .thenReturn(Timestamp.valueOf(client2.getUpdatedAt()))
                .thenReturn(Timestamp.valueOf(client3.getUpdatedAt()));

        // --- Action ---
        List<Client> resultClients = userRepository.getAllClients();

        // --- Assertions ---
        assertEquals(3, resultClients.size());

        // Client 1 assertions
        Client resultClient1 = resultClients.get(0);
        assertEquals(client.getUserId(), resultClient1.getUserId());
        assertEquals(client.getName(), resultClient1.getName());
        assertEquals(client.getEmail(), resultClient1.getEmail());
        assertTrue(resultClient1.isReceivesMarketUpdates(), "Client 1 should receive updates");
        assertEquals(client.getCreatedAt(), resultClient1.getCreatedAt());


        // Client 2 assertions
        Client resultClient2 = resultClients.get(1);
        assertEquals(client2.getUserId(), resultClient2.getUserId());
        assertEquals(client2.getName(), resultClient2.getName());
        assertEquals(client2.getEmail(), resultClient2.getEmail());
        assertFalse(resultClient2.isReceivesMarketUpdates(), "Client 2 should not receive updates");
        assertEquals(client2.getCreatedAt(), resultClient2.getCreatedAt());

        // Client 3 assertions
        Client resultClient3 = resultClients.get(2);
        assertEquals(client3.getUserId(), resultClient3.getUserId());
        assertEquals(client3.getName(), resultClient3.getName());
        assertEquals(client3.getEmail(), resultClient3.getEmail());
        assertFalse(resultClient3.isReceivesMarketUpdates(), "Client 3 (due to DB null) should not receive updates");
        assertEquals(client3.getCreatedAt(), resultClient3.getCreatedAt());

        verify(mockPreparedStatement).executeQuery();
        // rs.wasNull() is called once per client after rs.getBoolean("receives_market_updates")
        verify(mockResultSet, times(3)).wasNull();
    }

    @Test
    @DisplayName("getAllClients should return empty list if none found")
    void getAllClients_noneFound() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Client> clients = userRepository.getAllClients();

        assertTrue(clients.isEmpty());
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    @DisplayName("getAllClients should throw SQLException on connection error")
    void getAllClients_throwsSQLExceptionOnConnectionError() throws SQLException {
        when(mockConnector.getConnection()).thenThrow(new SQLException("Connection failed"));
        assertThrows(SQLException.class, () -> userRepository.getAllClients());
    }

    // --- General SQLException handling for PreparedStatement and ResultSet ---
    @Test
    @DisplayName("getUserById should throw SQLException on prepareStatement error")
    void getUserById_throwsSQLExceptionOnPrepareStatementError() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("PrepareStatement failed"));
        assertThrows(SQLException.class, () -> userRepository.getUserById("someId"));
    }

    @Test
    @DisplayName("getUserById should throw SQLException on executeQuery error")
    void getUserById_throwsSQLExceptionOnExecuteQueryError() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("ExecuteQuery failed"));
        assertThrows(SQLException.class, () -> userRepository.getUserById("someId"));
    }
}