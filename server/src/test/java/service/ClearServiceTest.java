package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ClearServiceTest {

    @Test
    public void positiveClearTest() {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        ClearService service = new ClearService(dataAccess);
        assertDoesNotThrow(service::clearApplication);
    }

    @Test
    public void clearRemovesAllDataTest() throws DataAccessException {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        
        // Add some test data
        UserData user = new UserData("testuser", "testpass", "test@email.com");
        AuthData auth = new AuthData("test-token", "testuser");
        dataAccess.createUser(user);
        dataAccess.createAuth(auth);
        
        // Clear everything
        ClearService service = new ClearService(dataAccess);
        service.clearApplication();
        
        // Verify data is gone
        assertNull(dataAccess.getUser("testuser"));
        assertNull(dataAccess.getAuth("test-token"));
    }
}
