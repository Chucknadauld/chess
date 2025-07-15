package service;

import dataaccess.MemoryDataAccess;
import model.UserData;
import service.requests.LoginRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Test
    public void registerPositiveTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());
        RegisterRequest req = new RegisterRequest("chuck", "password123", "chuck@email.com");

        RegisterResult result = service.register(req);

        assertEquals("chuck", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void registerDuplicateUsernameTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        RegisterRequest req1 = new RegisterRequest("charlie", "pass1", "charlie@email.com");
        RegisterRequest req2 = new RegisterRequest("charlie", "pass2", "charlie2@email.com");

        service.register(req1); // should succeed

        assertThrows(AlreadyTakenException.class, () -> {
            service.register(req2); // should fail
        });
    }

    @Test
    public void loginPositiveTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        // First register a user
        RegisterRequest regReq = new RegisterRequest("testuser", "testpass", "test@email.com");
        service.register(regReq);

        // Now try to login
        LoginRequest loginReq = new LoginRequest("testuser", "testpass");
        LoginResult result = service.login(loginReq);

        assertEquals("testuser", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void loginUserNotFoundTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        // Try to login with non-existent user
        LoginRequest loginReq = new LoginRequest("nonexistent", "password");

        assertThrows(UnauthorizedException.class, () -> {
            service.login(loginReq);
        });
    }

    @Test
    public void loginWrongPasswordTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        // First register a user
        RegisterRequest regReq = new RegisterRequest("testuser", "correctpass", "test@email.com");
        service.register(regReq);

        // Try to login with wrong password
        LoginRequest loginReq = new LoginRequest("testuser", "wrongpass");

        assertThrows(UnauthorizedException.class, () -> {
            service.login(loginReq);
        });
    }
}
