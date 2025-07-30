package client;

import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static int port;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    private ServerFacade serverFacade;

    @BeforeEach
    public void setup() throws Exception {
        serverFacade = new ServerFacade("http://localhost:" + port);
        serverFacade.clear();
    }

    @Test
    public void registerPositive() throws Exception {
        ServerFacade.RegisterResult result = serverFacade.register("testuser", "password", "test@example.com");
        
        Assertions.assertNotNull(result.authToken());
        Assertions.assertEquals("testuser", result.username());
    }

    @Test
    public void registerNegative() throws Exception {
        serverFacade.register("testuser", "password", "test@example.com");
        
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            serverFacade.register("testuser", "password2", "test2@example.com");
        });
        
        Assertions.assertTrue(exception.getMessage().contains("already taken"));
    }

    @Test
    public void loginPositive() throws Exception {
        serverFacade.register("testuser", "password", "test@example.com");
        
        ServerFacade.LoginResult result = serverFacade.login("testuser", "password");
        
        Assertions.assertNotNull(result.authToken());
        Assertions.assertEquals("testuser", result.username());
    }

    @Test
    public void loginNegative() throws Exception {
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            serverFacade.login("nonexistent", "password");
        });
        
        Assertions.assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    public void logoutPositive() throws Exception {
        ServerFacade.RegisterResult registerResult = serverFacade.register("testuser", "password", "test@example.com");
        
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.logout(registerResult.authToken());
        });
    }

    @Test
    public void logoutNegative() throws Exception {
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            serverFacade.logout("invalidtoken");
        });
        
        Assertions.assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    public void createGamePositive() throws Exception {
        ServerFacade.RegisterResult registerResult = serverFacade.register("testuser", "password", "test@example.com");
        
        ServerFacade.CreateGameResult result = serverFacade.createGame(registerResult.authToken(), "Test Game");
        
        Assertions.assertTrue(result.gameID() > 0);
    }

    @Test
    public void createGameNegative() throws Exception {
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            serverFacade.createGame("invalidtoken", "Test Game");
        });
        
        Assertions.assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    public void listGamesPositive() throws Exception {
        ServerFacade.RegisterResult registerResult = serverFacade.register("testuser", "password", "test@example.com");
        serverFacade.createGame(registerResult.authToken(), "Game 1");
        serverFacade.createGame(registerResult.authToken(), "Game 2");
        
        ServerFacade.ListGamesResult result = serverFacade.listGames(registerResult.authToken());
        
        Assertions.assertEquals(2, result.games().size());
    }

    @Test
    public void listGamesNegative() throws Exception {
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            serverFacade.listGames("invalidtoken");
        });
        
        Assertions.assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    public void joinGamePositive() throws Exception {
        ServerFacade.RegisterResult registerResult = serverFacade.register("testuser", "password", "test@example.com");
        ServerFacade.CreateGameResult createResult = serverFacade.createGame(registerResult.authToken(), "Test Game");
        
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.joinGame(registerResult.authToken(), "WHITE", createResult.gameID());
        });
    }

    @Test
    public void joinGameNegative() throws Exception {
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            serverFacade.joinGame("invalidtoken", "WHITE", 999);
        });
        
        Assertions.assertTrue(exception.getMessage().contains("unauthorized"));
    }

}
