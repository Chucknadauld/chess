package service;

import dataaccess.MemoryDataAccess;
import service.requests.CreateGameRequest;
import service.requests.ListGamesRequest;
import service.requests.RegisterRequest;
import service.results.CreateGameResult;
import service.results.ListGamesResult;
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

        CreateGameRequest createGameRequest = new CreateGameRequest(authToken, "Test Game");
        CreateGameResult result = gameService.createGame(createGameRequest);

        assertTrue(result.gameID() > 0);
        assertEquals(1, result.gameID());
    }

    @Test
    public void createGameInvalidAuthTokenTest() throws DataAccessException {
        GameService service = new GameService(new MemoryDataAccess());

        CreateGameRequest createGameRequest = new CreateGameRequest("invalid-token", "Test Game");

        assertThrows(UnauthorizedException.class, () -> {
            service.createGame(createGameRequest);
        });
    }

    @Test
    public void createGameNullAuthTokenTest() throws DataAccessException {
        GameService service = new GameService(new MemoryDataAccess());

        CreateGameRequest createGameRequest = new CreateGameRequest(null, "Test Game");

        assertThrows(UnauthorizedException.class, () -> {
            service.createGame(createGameRequest);
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

        CreateGameRequest createGameRequest = new CreateGameRequest(authToken, null);

        assertThrows(BadRequestException.class, () -> {
            gameService.createGame(createGameRequest);
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

        CreateGameRequest createGameRequest = new CreateGameRequest(authToken, "");

        assertThrows(BadRequestException.class, () -> {
            gameService.createGame(createGameRequest);
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

        CreateGameRequest createGameRequest1 = new CreateGameRequest(authToken, "Game 1");
        CreateGameRequest createGameRequest2 = new CreateGameRequest(authToken, "Game 2");
        CreateGameRequest createGameRequest3 = new CreateGameRequest(authToken, "Game 3");

        CreateGameResult result1 = gameService.createGame(createGameRequest1);
        CreateGameResult result2 = gameService.createGame(createGameRequest2);
        CreateGameResult result3 = gameService.createGame(createGameRequest3);

        assertEquals(1, result1.gameID());
        assertEquals(2, result2.gameID());
        assertEquals(3, result3.gameID());
    }

    @Test
    public void listGamesPositiveTest() throws DataAccessException {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        GameService gameService = new GameService(dataAccess);
        UserService userService = new UserService(dataAccess);

        RegisterRequest registerRequest = new RegisterRequest("testuser", "testpass", "test@email.com");
        RegisterResult registerResult = userService.register(registerRequest);
        String authToken = registerResult.authToken();

        CreateGameRequest createGameRequest1 = new CreateGameRequest(authToken, "Chess Game 1");
        CreateGameRequest createGameRequest2 = new CreateGameRequest(authToken, "Chess Game 2");
        gameService.createGame(createGameRequest1);
        gameService.createGame(createGameRequest2);

        ListGamesRequest listGamesRequest = new ListGamesRequest(authToken);
        ListGamesResult result = gameService.listGames(listGamesRequest);

        assertNotNull(result.games());
        assertEquals(2, result.games().size());
        assertEquals("Chess Game 1", result.games().get(0).gameName());
        assertEquals("Chess Game 2", result.games().get(1).gameName());
    }

    @Test
    public void listGamesInvalidAuthTokenTest() throws DataAccessException {
        GameService service = new GameService(new MemoryDataAccess());

        ListGamesRequest listGamesRequest = new ListGamesRequest("invalid-token");

        assertThrows(UnauthorizedException.class, () -> {
            service.listGames(listGamesRequest);
        });
    }

    @Test
    public void listGamesEmptyListTest() throws DataAccessException {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        GameService gameService = new GameService(dataAccess);
        UserService userService = new UserService(dataAccess);

        RegisterRequest registerRequest = new RegisterRequest("testuser", "testpass", "test@email.com");
        RegisterResult registerResult = userService.register(registerRequest);
        String authToken = registerResult.authToken();

        ListGamesRequest listGamesRequest = new ListGamesRequest(authToken);
        ListGamesResult result = gameService.listGames(listGamesRequest);

        assertNotNull(result.games());
        assertEquals(0, result.games().size());
        assertTrue(result.games().isEmpty());
    }

    @Test
    public void listGamesNullAuthTokenTest() throws DataAccessException {
        GameService service = new GameService(new MemoryDataAccess());

        ListGamesRequest listGamesRequest = new ListGamesRequest(null);

        assertThrows(UnauthorizedException.class, () -> {
            service.listGames(listGamesRequest);
        });
    }
}