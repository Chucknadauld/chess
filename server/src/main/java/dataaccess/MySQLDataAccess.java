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

    private void createTables(java.sql.Connection connection) throws SQLException {
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

        try (var statement = connection.prepareStatement(createUsersTable)) {
            statement.executeUpdate();
        }
        try (var statement = connection.prepareStatement(createAuthTable)) {
            statement.executeUpdate();
        }
        try (var statement = connection.prepareStatement(createGamesTable)) {
            statement.executeUpdate();
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            try (var deleteGamesStatement = connection.prepareStatement("DELETE FROM games")) {
                deleteGamesStatement.executeUpdate();
            }
            try (var deleteAuthStatement = connection.prepareStatement("DELETE FROM auth")) {
                deleteAuthStatement.executeUpdate();
            }
            try (var deleteUsersStatement = connection.prepareStatement("DELETE FROM users")) {
                deleteUsersStatement.executeUpdate();
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
        var selectUserSQL = "SELECT username, password, email FROM users WHERE username = ?";
        try (var connection = DatabaseManager.getConnection();
             var preparedStatement = connection.prepareStatement(selectUserSQL)) {
            preparedStatement.setString(1, username);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new UserData(resultSet.getString("username"), resultSet.getString("password"), resultSet.getString("email"));
            }
            return null;
        } catch (SQLException ex) {
            throw new DataAccessException("Error retrieving user", ex);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        var hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(user.password(), org.mindrot.jbcrypt.BCrypt.gensalt());
        var insertUserSQL = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (var connection = DatabaseManager.getConnection();
             var preparedStatement = connection.prepareStatement(insertUserSQL)) {
            preparedStatement.setString(1, user.username());
            preparedStatement.setString(2, hashedPassword);
            preparedStatement.setString(3, user.email());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error inserting user", ex);
        }
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        var insertAuthSQL = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var connection = DatabaseManager.getConnection();
             var preparedStatement = connection.prepareStatement(insertAuthSQL)) {
            preparedStatement.setString(1, authData.authToken());
            preparedStatement.setString(2, authData.username());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error inserting auth", ex);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var selectAuthSQL = "SELECT authToken, username FROM auth WHERE authToken = ?";
        try (var connection = DatabaseManager.getConnection();
             var preparedStatement = connection.prepareStatement(selectAuthSQL)) {
            preparedStatement.setString(1, authToken);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new AuthData(resultSet.getString("authToken"), resultSet.getString("username"));
            }
            return null;
        } catch (SQLException ex) {
            throw new DataAccessException("Error retrieving auth", ex);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var deleteAuthSQL = "DELETE FROM auth WHERE authToken = ?";
        try (var connection = DatabaseManager.getConnection();
             var preparedStatement = connection.prepareStatement(deleteAuthSQL)) {
            preparedStatement.setString(1, authToken);
            preparedStatement.executeUpdate();
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