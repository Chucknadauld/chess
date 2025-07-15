package service;

import model.UserData;
import service.requests.RegisterRequest;
import service.results.RegisterResult;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Test
    public void registerPositiveTest() throws DataAccessException {
        UserService service = new UserService();
        RegisterRequest req = new RegisterRequest("chuck", "password123", "chuck@email.com");

        RegisterResult result = service.register(req);

        assertEquals("chuck", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void registerDuplicateUsernameTest() throws DataAccessException {
        UserService service = new UserService();

        RegisterRequest req1 = new RegisterRequest("charlie", "pass1", "charlie@email.com");
        RegisterRequest req2 = new RegisterRequest("charlie", "pass2", "charlie2@email.com");

        service.register(req1); // should succeed

        assertThrows(AlreadyTakenException.class, () -> {
            service.register(req2); // should fail
        });
    }
}
