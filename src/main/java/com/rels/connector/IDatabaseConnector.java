package com.rels.connector;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface defining the contract for obtaining and closing database connections.
 */
public interface IDatabaseConnector {

    /**
     * Attempts to establish a connection to the database.
     *
     * @return A Connection object representing the database connection.
     * @throws SQLException if a database access error occurs.
     */
    Connection getConnection() throws SQLException;

    /**
     * Attempts to close the given database connection.
     * Includes checks for null and already closed connections.
     *
     * @param conn The connection to close. Can be null.
     * @throws SQLException if a database access error occurs during closing.
     */
    void closeConnection(Connection conn) throws SQLException;
}