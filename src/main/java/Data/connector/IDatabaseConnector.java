package Data.connector;
import java.sql.Connection;
import java.sql.SQLException;

public interface IDatabaseConnector {

    Connection getConnection() throws SQLException;

    void closeConnection(Connection conn) throws SQLException;
}