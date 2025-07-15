package service;

import dataaccess.MemoryDataAccess;
import service.requests.CreateGameRequest;
import service.requests.RegisterRequest;
import service.results.CreateGameResult;
import service.results.RegisterResult;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    @BeforeEach
    public void resetGameID() {
        GameService.resetGameIDForTesting();
    }

    @Test
    public void createGamePositiveTest() throws DataAccessException {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        GameService gameService = new GameService(dataAccess);
        UserService userService = new UserService(dataAccess);

        RegisterRequest registerRequest = new RegisterRequest("testuser", "testpass", "test@email.com");
        RegisterResult registerResult = userService.register(registerRequest);
        String authToken = registerResult.authToken();

        CreateGameRequest createRequest = new CreateGameRequest(authToken, "Test Game");
        CreateGameResult result = gameService.createGame(createRequest);

        assertTrue(result.gameID() > 0);
        assertEquals(1, result.gameID());
    }

    @Test
    public void createGameInvalidAuthTokenTest() throws DataAccessException {
        GameService service = new GameService(new MemoryDataAccess());

        CreateGameRequest createRequest = new CreateGameRequest("invalid-token", "Test Game");

        assertThrows(UnauthorizedException.class, () -> {
            service.createGame(createRequest);
        });
    }

    @Test
    public void createGameNullAuthTokenTest() throws DataAccessException {
        GameService service = new GameService(new MemoryDataAccess());

        CreateGameRequest createRequest = new CreateGameRequest(null, "Test Game");

        assertThrows(UnauthorizedException.class, () -> {
            service.createGame(createRequest);
        });
    }

    @Test
    public void createGameMissingGameNameTest() throws DataAccessException {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        GameService gameService = new GameService(dataAccess);
        UserService userService = new UserService(dataAccess);

        RegisterRequest registerRequest = new RegisterRequest("testuser", "testpass", "test@email.com");
        RegisterResult registerResult = userService.register(registerRequest);
        String authToken = registerResult.authToken();

        CreateGameRequest createRequest = new CreateGameRequest(authToken, null);

        assertThrows(BadRequestException.class, () -> {
            gameService.createGame(createRequest);
        });
    }

    @Test
    public void createGameEmptyGameNameTest() throws DataAccessException {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        GameService gameService = new GameService(dataAccess);
        UserService userService = new UserService(dataAccess);

        RegisterRequest registerRequest = new RegisterRequest("testuser", "testpass", "test@email.com");
        RegisterResult registerResult = userService.register(registerRequest);
        String authToken = registerResult.authToken();

        CreateGameRequest createRequest = new CreateGameRequest(authToken, "");

        assertThrows(BadRequestException.class, () -> {
            gameService.createGame(createRequest);
        });
    }

    @Test
    public void createMultipleGamesIncrementingIDsTest() throws DataAccessException {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        GameService gameService = new GameService(dataAccess);
        UserService userService = new UserService(dataAccess);

        RegisterRequest registerRequest = new RegisterRequest("testuser", "testpass", "test@email.com");
        RegisterResult registerResult = userService.register(registerRequest);
        String authToken = registerResult.authToken();

        CreateGameRequest createRequest1 = new CreateGameRequest(authToken, "Game 1");
        CreateGameRequest createRequest2 = new CreateGameRequest(authToken, "Game 2");
        CreateGameRequest createRequest3 = new CreateGameRequest(authToken, "Game 3");

        CreateGameResult result1 = gameService.createGame(createRequest1);
        CreateGameResult result2 = gameService.createGame(createRequest2);
        CreateGameResult result3 = gameService.createGame(createRequest3);

        assertEquals(1, result1.gameID());
        assertEquals(2, result2.gameID());
        assertEquals(3, result3.gameID());
    }
}