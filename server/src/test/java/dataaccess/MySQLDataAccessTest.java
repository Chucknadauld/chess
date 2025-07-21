package dataaccess;

import model.UserData;
import model.AuthData;
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

    @Test
    public void createAuthPositive() throws DataAccessException {
        var user = new UserData("testuser", "password", "test@example.com");
        dataAccess.createUser(user);
        
        var auth = new AuthData("token123", "testuser");
        dataAccess.createAuth(auth);
        
        var retrievedAuth = dataAccess.getAuth("token123");
        assertNotNull(retrievedAuth);
        assertEquals("token123", retrievedAuth.authToken());
        assertEquals("testuser", retrievedAuth.username());
    }

    @Test
    public void createAuthNegative() throws DataAccessException {
        var auth = new AuthData("token123", "nonexistent");
        
        assertThrows(DataAccessException.class, () -> {
            dataAccess.createAuth(auth);
        });
    }

    @Test
    public void getAuthPositive() throws DataAccessException {
        var user = new UserData("testuser", "password", "test@example.com");
        dataAccess.createUser(user);
        
        var auth = new AuthData("token123", "testuser");
        dataAccess.createAuth(auth);
        
        var retrievedAuth = dataAccess.getAuth("token123");
        assertNotNull(retrievedAuth);
        assertEquals("testuser", retrievedAuth.username());
    }

    @Test
    public void getAuthNegative() throws DataAccessException {
        var retrievedAuth = dataAccess.getAuth("nonexistent");
        assertNull(retrievedAuth);
    }

    @Test
    public void deleteAuthPositive() throws DataAccessException {
        var user = new UserData("testuser", "password", "test@example.com");
        dataAccess.createUser(user);
        
        var auth = new AuthData("token123", "testuser");
        dataAccess.createAuth(auth);
        
        dataAccess.deleteAuth("token123");
        
        var retrievedAuth = dataAccess.getAuth("token123");
        assertNull(retrievedAuth);
    }
} 