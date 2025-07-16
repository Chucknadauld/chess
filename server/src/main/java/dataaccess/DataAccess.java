package dataaccess;

import model.GameData;
import model.UserData;
import model.AuthData;

import java.util.List;

public interface DataAccess {
    void clear() throws DataAccessException;
    
    void clearGames() throws DataAccessException;
    void clearAuths() throws DataAccessException;
    void clearUsers() throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    void createUser(UserData user) throws DataAccessException;

    void createAuth(AuthData authData) throws DataAccessException;
    
    AuthData getAuth(String authToken) throws DataAccessException;
    
    void deleteAuth(String authToken) throws DataAccessException;

    void createGame(GameData game) throws DataAccessException;

    List<GameData> listGames() throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    void updateGame(GameData updatedGame) throws DataAccessException;
}
