package Data.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnectorImpl implements IDatabaseConnector {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public DatabaseConnectorImpl(String dbUrl, String dbUser, String dbPassword) {
        if (dbUrl == null || dbUser == null || dbPassword == null) {
            throw new IllegalArgumentException("Database URL, user, and password cannot be null.");
        }
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;

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