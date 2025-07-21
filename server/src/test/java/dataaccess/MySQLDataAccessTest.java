package dataaccess;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MySQLDataAccessTest {

    @Test
    public void testDatabaseConnection() throws DataAccessException {
        MySQLDataAccess dataAccess = new MySQLDataAccess();
        assertNotNull(dataAccess);
    }
} 