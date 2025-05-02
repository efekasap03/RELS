package com.rels.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Concrete implementation of IDatabaseConnector for standard JDBC connections,
 * specifically configured for MySQL in this example.
 *
 * Assumes the MySQL JDBC driver (e.g., mysql-connector-java) is available
 * on the classpath. Modern drivers typically self-register.
 */
public class DatabaseConnectorImpl implements IDatabaseConnector {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    /**
     * Constructs a DatabaseConnectorImpl with necessary connection details.
     *
     * @param dbUrl      The JDBC URL for the database
     * @param dbUser     The database username.
     * @param dbPassword The database password.
     */
    public DatabaseConnectorImpl(String dbUrl, String dbUser, String dbPassword) {
        if (dbUrl == null || dbUser == null || dbPassword == null) {
            throw new IllegalArgumentException("Database URL, user, and password cannot be null.");
        }
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;

        // Optional: Explicitly load the driver if auto-registration is not reliable
        // try {
        //     Class.forName("com.mysql.cj.jdbc.Driver");
        // } catch (ClassNotFoundException e) {
        //     System.err.println("ERROR: MySQL JDBC Driver not found. Ensure it's on the classpath.");
        //     // Depending on application design, might throw a RuntimeException here
        // }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() throws SQLException {
        // DriverManager attempts to establish a connection based on the provided credentials.
        // This method directly throws SQLException if connection fails.
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    /**
     * {@inheritDoc}
     *
     * Note: In modern Java, connections obtained should ideally be managed
     * using try-with-resources blocks in the calling code (e.g., Repositories),
     * which automatically handles closing. This method provides an explicit
     * closing mechanism if needed.
     */
    @Override
    public void closeConnection(Connection conn) throws SQLException {
        if (conn != null) {
            try {
                // Check if connection is already closed before attempting to close again
                if (!conn.isClosed()) {
                    conn.close();
                    // System.out.println("Database connection closed."); // Optional logging
                }
            } catch (SQLException e) {
                System.err.println("ERROR closing database connection: " + e.getMessage());
                // Re-throwing the exception as per the interface contract
                throw e;
            }
        }
    }
}