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
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement("DELETE FROM games")) {
                ps.executeUpdate();
            }
            try (var ps = conn.prepareStatement("DELETE FROM auth")) {
                ps.executeUpdate();
            }
            try (var ps = conn.prepareStatement("DELETE FROM users")) {
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error clearing database", ex);
        }
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
        var statement = "SELECT username, password, email FROM users WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, username);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
            }
            return null;
        } catch (SQLException ex) {
            throw new DataAccessException("Error retrieving user", ex);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        var hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(user.password(), org.mindrot.jbcrypt.BCrypt.gensalt());
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, user.username());
            ps.setString(2, hashedPassword);
            ps.setString(3, user.email());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error inserting user", ex);
        }
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, authData.authToken());
            ps.setString(2, authData.username());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error inserting auth", ex);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var statement = "SELECT authToken, username FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, authToken);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return new AuthData(rs.getString("authToken"), rs.getString("username"));
            }
            return null;
        } catch (SQLException ex) {
            throw new DataAccessException("Error retrieving auth", ex);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var statement = "DELETE FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, authToken);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error deleting auth", ex);
        }
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