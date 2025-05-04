package Data.repository.interfaces; // Make sure package declaration is correct

import Data.domain.Client;
import Data.domain.Landlord;
import Data.domain.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional; // Import Optional

/**
 * Interface defining data access operations for User entities,
 * including specialized types Landlord and Client.
 */
public interface IUserRepository {

    boolean addUser(User user) throws SQLException;
    Optional<User> getUserById(String userId) throws SQLException;
    Optional<User> getUserByEmail(String email) throws SQLException;
    boolean updateUser(User user) throws SQLException;
    boolean deleteUser(String userId) throws SQLException;
    List<Landlord> getAllLandlords() throws SQLException;
    List<Client> getAllClients() throws SQLException;

}