package dataaccess;

import model.GameData;
import model.UserData;
import model.AuthData;
import java.util.List;
import java.sql.SQLException;

public class MySQLDataAccess implements DataAccess {

    public MySQLDataAccess() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            createTables(conn);
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to configure database", ex);
        }
    }

    private void createTables(java.sql.Connection conn) throws SQLException {
        var createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(255) PRIMARY KEY,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL
            )""";

        var createAuthTable = """
            CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(255) PRIMARY KEY,
                username VARCHAR(255) NOT NULL,
                FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
            )""";

        var createGamesTable = """
            CREATE TABLE IF NOT EXISTS games (
                gameID INT AUTO_INCREMENT PRIMARY KEY,
                whiteUsername VARCHAR(255),
                blackUsername VARCHAR(255),
                gameName VARCHAR(255) NOT NULL,
                game TEXT NOT NULL,
                FOREIGN KEY (whiteUsername) REFERENCES users(username) ON DELETE SET NULL,
                FOREIGN KEY (blackUsername) REFERENCES users(username) ON DELETE SET NULL
            )""";

        try (var stmt = conn.prepareStatement(createUsersTable)) {
            stmt.executeUpdate();
        }
        try (var stmt = conn.prepareStatement(createAuthTable)) {
            stmt.executeUpdate();
        }
        try (var stmt = conn.prepareStatement(createGamesTable)) {
            stmt.executeUpdate();
        }
    }

    @Override
    public void clear() throws DataAccessException {
        // TODO: Implement
    }

    @Override
    public void clearGames() throws DataAccessException {
        // TODO: Implement
    }

    @Override
    public void clearAuths() throws DataAccessException {
        // TODO: Implement
    }

    @Override
    public void clearUsers() throws DataAccessException {
        // TODO: Implement
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        // TODO: Implement
        return null;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        // TODO: Implement
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        // TODO: Implement
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        // TODO: Implement
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        // TODO: Implement
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        // TODO: Implement
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        // TODO: Implement
        return null;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        // TODO: Implement
        return null;
    }

    @Override
    public void updateGame(GameData updatedGame) throws DataAccessException {
        // TODO: Implement
    }
} 