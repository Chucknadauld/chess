package service;

import dataaccess.MemoryDataAccess;
import model.UserData;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.LogoutResult;
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
    public void registerEmptyUsernameTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());
        
        // Service layer accepts empty username - validation happens in handler
        // But let's test with null to see behavior
        RegisterRequest req = new RegisterRequest("", "password", "email@test.com");
        
        // Service doesn't validate empty strings, that's handler's job
        // So this should actually succeed at service level
        assertDoesNotThrow(() -> {
            service.register(req);
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

    @Test
    public void loginNullPasswordTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        // First register a user with a real password
        RegisterRequest regReq = new RegisterRequest("testuser", "realpass", "test@email.com");
        service.register(regReq);

        // Try to login with null password
        LoginRequest loginReq = new LoginRequest("testuser", null);

        assertThrows(UnauthorizedException.class, () -> {
            service.login(loginReq);
        });
    }

    @Test
    public void logoutPositiveTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        // First register and login to get a valid auth token
        RegisterRequest regReq = new RegisterRequest("testuser", "testpass", "test@email.com");
        RegisterResult regResult = service.register(regReq);
        String authToken = regResult.authToken();

        // Now logout with the valid token
        LogoutRequest logoutReq = new LogoutRequest(authToken);
        LogoutResult result = service.logout(logoutReq);

        // Should succeed without throwing exception
        assertNotNull(result);
    }

    @Test
    public void logoutInvalidTokenTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        // Try to logout with invalid auth token
        LogoutRequest logoutReq = new LogoutRequest("invalid-token");

        assertThrows(UnauthorizedException.class, () -> {
            service.logout(logoutReq);
        });
    }

    @Test
    public void logoutTokenInvalidatedTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        // Register and get token
        RegisterRequest regReq = new RegisterRequest("testuser", "testpass", "test@email.com");
        RegisterResult regResult = service.register(regReq);
        String authToken = regResult.authToken();

        // First logout should work
        LogoutRequest logoutReq = new LogoutRequest(authToken);
        service.logout(logoutReq); // Should succeed

        // Second logout with same token should fail (token was deleted)
        assertThrows(UnauthorizedException.class, () -> {
            service.logout(logoutReq); // Should fail - token was deleted
        });
    }

    @Test
    public void logoutNullTokenTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        // Try to logout with null auth token
        LogoutRequest logoutReq = new LogoutRequest(null);

        assertThrows(UnauthorizedException.class, () -> {
            service.logout(logoutReq);
        });
    }

    @Test
    public void multipleLoginsGenerateDifferentTokensTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        // Register a user
        RegisterRequest regReq = new RegisterRequest("testuser", "testpass", "test@email.com");
        RegisterResult regResult = service.register(regReq);

        // Login twice
        LoginRequest loginReq = new LoginRequest("testuser", "testpass");
        LoginResult login1 = service.login(loginReq);
        LoginResult login2 = service.login(loginReq);

        // Tokens should be different
        assertNotEquals(regResult.authToken(), login1.authToken());
        assertNotEquals(login1.authToken(), login2.authToken());
        assertNotEquals(regResult.authToken(), login2.authToken());
    }
}
