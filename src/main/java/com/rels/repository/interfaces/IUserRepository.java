package com.rels.repository.interfaces; // Make sure package declaration is correct

import com.rels.domain.Client;
import com.rels.domain.Landlord;
import com.rels.domain.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional; // Import Optional

/**
 * Interface defining data access operations for User entities,
 * including specialized types Landlord and Client.
 */
public interface IUserRepository {

    /**
     * Adds a new user to the database. The specific type (User, Landlord, Client)
     * and its associated role/fields are determined by the passed User object.
     *
     * @param user The User object (or subclass Landlord/Client) to add.
     *             Must have userId, role, and required fields set.
     * @return true if the user was added successfully, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean addUser(User user) throws SQLException;

    /**
     * Retrieves a user by their unique ID.
     * The implementation should instantiate the correct subclass (Landlord or Client)
     * based on the 'role' field retrieved from the database.
     *
     * @param userId The unique ID (UUID as String) of the user to retrieve.
     * @return An Optional containing the User (as Landlord or Client) if found,
     *         otherwise Optional.empty().
     * @throws SQLException if a database access error occurs.
     */
    Optional<User> getUserById(String userId) throws SQLException;

    /**
     * Retrieves a user by their unique email address.
     * The implementation should instantiate the correct subclass (Landlord or Client)
     * based on the 'role' field retrieved from the database.
     *
     * @param email The email address of the user to retrieve.
     * @return An Optional containing the User (as Landlord or Client) if found,
     *         otherwise Optional.empty().
     * @throws SQLException if a database access error occurs.
     */
    Optional<User> getUserByEmail(String email) throws SQLException;

    /**
     * Updates an existing user's information in the database.
     * The implementation should correctly update common fields and any
     * role-specific fields present in the provided User object (or subclass).
     *
     * @param user The User object (or subclass) containing the updated data.
     *             The userId field identifies the user to update.
     * @return true if the user was updated successfully (e.g., 1 row affected),
     *         false otherwise (e.g., user not found).
     * @throws SQLException if a database access error occurs.
     */
    boolean updateUser(User user) throws SQLException;

    /**
     * Deletes a user from the database using their unique ID.
     * Note: Database constraints (e.g., a Landlord with properties) might prevent deletion.
     *
     * @param userId The unique ID (UUID as String) of the user to delete.
     * @return true if the user was deleted successfully (e.g., 1 row affected),
     *         false otherwise (e.g., user not found).
     * @throws SQLException if a database access error occurs or constraints are violated.
     */
    boolean deleteUser(String userId) throws SQLException;

    /**
     * Retrieves a list of all users designated specifically as Landlords.
     *
     * @return A List containing all found Landlord objects. The list may be empty.
     * @throws SQLException if a database access error occurs.
     */
    List<Landlord> getAllLandlords() throws SQLException;

    /**
     * Retrieves a list of all users designated specifically as Clients.
     *
     * @return A List containing all found Client objects. The list may be empty.
     * @throws SQLException if a database access error occurs.
     */
    List<Client> getAllClients() throws SQLException;

}