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
        RegisterRequest registerRequest = new RegisterRequest("chuck", "password123", "chuck@email.com");

        RegisterResult result = service.register(registerRequest);

        assertEquals("chuck", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void registerDuplicateUsernameTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        RegisterRequest registerRequest1 = new RegisterRequest("charlie", "pass1", "charlie@email.com");
        RegisterRequest registerRequest2 = new RegisterRequest("charlie", "pass2", "charlie2@email.com");

        service.register(registerRequest1);

        assertThrows(AlreadyTakenException.class, () -> {
            service.register(registerRequest2);
        });
    }

    @Test
    public void registerEmptyUsernameTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());
        
        RegisterRequest registerRequest = new RegisterRequest("", "password", "email@test.com");
        
        assertDoesNotThrow(() -> {
            service.register(registerRequest);
        });
    }

    @Test
    public void loginPositiveTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        RegisterRequest registerRequest = new RegisterRequest("testuser", "testpass", "test@email.com");
        service.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest("testuser", "testpass");
        LoginResult result = service.login(loginRequest);

        assertEquals("testuser", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void loginUserNotFoundTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        LoginRequest loginRequest = new LoginRequest("nonexistent", "password");

        assertThrows(UnauthorizedException.class, () -> {
            service.login(loginRequest);
        });
    }

    @Test
    public void loginWrongPasswordTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        RegisterRequest registerRequest = new RegisterRequest("testuser", "correctpass", "test@email.com");
        service.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpass");

        assertThrows(UnauthorizedException.class, () -> {
            service.login(loginRequest);
        });
    }

    @Test
    public void loginNullPasswordTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        RegisterRequest registerRequest = new RegisterRequest("testuser", "realpass", "test@email.com");
        service.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest("testuser", null);

        assertThrows(UnauthorizedException.class, () -> {
            service.login(loginRequest);
        });
    }

    @Test
    public void logoutPositiveTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        RegisterRequest registerRequest = new RegisterRequest("testuser", "testpass", "test@email.com");
        RegisterResult registerResult = service.register(registerRequest);
        String authToken = registerResult.authToken();

        LogoutRequest logoutRequest = new LogoutRequest(authToken);
        LogoutResult result = service.logout(logoutRequest);

        assertNotNull(result);
    }

    @Test
    public void logoutInvalidTokenTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        LogoutRequest logoutRequest = new LogoutRequest("invalid-token");

        assertThrows(UnauthorizedException.class, () -> {
            service.logout(logoutRequest);
        });
    }

    @Test
    public void logoutTokenInvalidatedTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        RegisterRequest registerRequest = new RegisterRequest("testuser", "testpass", "test@email.com");
        RegisterResult registerResult = service.register(registerRequest);
        String authToken = registerResult.authToken();

        LogoutRequest logoutRequest = new LogoutRequest(authToken);
        service.logout(logoutRequest);

        assertThrows(UnauthorizedException.class, () -> {
            service.logout(logoutRequest);
        });
    }

    @Test
    public void logoutNullTokenTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        LogoutRequest logoutRequest = new LogoutRequest(null);

        assertThrows(UnauthorizedException.class, () -> {
            service.logout(logoutRequest);
        });
    }

    @Test
    public void multipleLoginsGenerateDifferentTokensTest() throws DataAccessException {
        UserService service = new UserService(new MemoryDataAccess());

        RegisterRequest registerRequest = new RegisterRequest("testuser", "testpass", "test@email.com");
        RegisterResult registerResult = service.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest("testuser", "testpass");
        LoginResult login1 = service.login(loginRequest);
        LoginResult login2 = service.login(loginRequest);

        assertNotEquals(registerResult.authToken(), login1.authToken());
        assertNotEquals(login1.authToken(), login2.authToken());
        assertNotEquals(registerResult.authToken(), login2.authToken());
    }
}
