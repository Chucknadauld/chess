package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MySQLDataAccessTest {

    private MySQLDataAccess dataAccess;

    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess = new MySQLDataAccess();
        dataAccess.clear();
    }

    @Test
    public void createUserPositive() throws DataAccessException {
        var user = new UserData("testuser", "password", "test@example.com");
        dataAccess.createUser(user);
        
        var retrievedUser = dataAccess.getUser("testuser");
        assertNotNull(retrievedUser);
        assertEquals("testuser", retrievedUser.username());
        assertEquals("test@example.com", retrievedUser.email());
        assertNotEquals("password", retrievedUser.password());
        assertTrue(org.mindrot.jbcrypt.BCrypt.checkpw("password", retrievedUser.password()));
    }

    @Test
    public void createUserNegative() throws DataAccessException {
        var user = new UserData("testuser", "password", "test@example.com");
        dataAccess.createUser(user);
        
        assertThrows(DataAccessException.class, () -> {
            dataAccess.createUser(user);
        });
    }

    @Test
    public void getUserPositive() throws DataAccessException {
        var user = new UserData("testuser", "password", "test@example.com");
        dataAccess.createUser(user);
        
        var retrievedUser = dataAccess.getUser("testuser");
        assertNotNull(retrievedUser);
        assertEquals("testuser", retrievedUser.username());
    }

    @Test
    public void getUserNegative() throws DataAccessException {
        var retrievedUser = dataAccess.getUser("nonexistent");
        assertNull(retrievedUser);
    }

    @Test
    public void clearPositive() throws DataAccessException {
        var user = new UserData("testuser", "password", "test@example.com");
        dataAccess.createUser(user);
        
        dataAccess.clear();
        
        var retrievedUser = dataAccess.getUser("testuser");
        assertNull(retrievedUser);
    }
} 