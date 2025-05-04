package Data;

import Data.connector.DatabaseConnectorImpl;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // Database connection parameters
        String dbUrl = "jdbc:mysql://161.35.157.249:3306/relsdb?useSSL=false&serverTimezone=UTC";
        String dbUser = "regular";
        String dbPassword = "regularpass"; // You'll need to enter this when prompted

        // Create database connector
        DatabaseConnectorImpl connector = new DatabaseConnectorImpl(dbUrl, dbUser, dbPassword);

        // Test the connection
        try (Connection conn = connector.getConnection()) {
            System.out.println("Successfully connected to the database!");
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
