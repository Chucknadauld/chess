package dataaccess;

import model.UserData;
import model.AuthData;
import model.GameData;
import chess.ChessGame;
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

    @Test
    public void createGamePositive() throws DataAccessException {
        var chessGame = new ChessGame();
        var game = new GameData(1, "white", "black", "testgame", chessGame);
        dataAccess.createGame(game);
        
        var gamesList = dataAccess.listGames();
        assertFalse(gamesList.isEmpty());
        assertEquals("testgame", gamesList.get(0).gameName());
    }

    @Test
    public void createGameNegative() throws DataAccessException {
        var game = new GameData(1, null, null, null, null);
        
        assertThrows(DataAccessException.class, () -> {
            dataAccess.createGame(game);
        });
    }

    @Test
    public void getGamePositive() throws DataAccessException {
        var chessGame = new ChessGame();
        var game = new GameData(1, "white", "black", "testgame", chessGame);
        dataAccess.createGame(game);
        
        var gamesList = dataAccess.listGames();
        var gameID = gamesList.get(0).gameID();
        
        var retrievedGame = dataAccess.getGame(gameID);
        assertNotNull(retrievedGame);
        assertEquals("testgame", retrievedGame.gameName());
        assertNotNull(retrievedGame.game());
    }

    @Test
    public void getGameNegative() throws DataAccessException {
        var retrievedGame = dataAccess.getGame(999);
        assertNull(retrievedGame);
    }

    @Test
    public void updateGamePositive() throws DataAccessException {
        var chessGame = new ChessGame();
        var game = new GameData(1, "white", "black", "testgame", chessGame);
        dataAccess.createGame(game);
        
        var gamesList = dataAccess.listGames();
        var originalGame = gamesList.get(0);
        
        var updatedGame = new GameData(originalGame.gameID(), "newwhite", "newblack", "updatedgame", chessGame);
        dataAccess.updateGame(updatedGame);
        
        var retrievedGame = dataAccess.getGame(originalGame.gameID());
        assertEquals("updatedgame", retrievedGame.gameName());
        assertEquals("newwhite", retrievedGame.whiteUsername());
    }

    @Test
    public void listGamesPositive() throws DataAccessException {
        var chessGame1 = new ChessGame();
        var chessGame2 = new ChessGame();
        var game1 = new GameData(1, "white1", "black1", "game1", chessGame1);
        var game2 = new GameData(2, "white2", "black2", "game2", chessGame2);
        
        dataAccess.createGame(game1);
        dataAccess.createGame(game2);
        
        var gamesList = dataAccess.listGames();
        assertEquals(2, gamesList.size());
    }

    @Test
    public void listGamesEmpty() throws DataAccessException {
        var gamesList = dataAccess.listGames();
        assertTrue(gamesList.isEmpty());
    }
} 