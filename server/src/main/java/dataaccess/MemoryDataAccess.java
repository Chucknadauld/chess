package dataaccess;

import java.util.Map;
import java.util.HashMap;

import model.UserData;
import model.AuthData;

public class MemoryDataAccess implements DataAccess {

    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        users.clear();
        authTokens.clear();
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public void createAuth(AuthData auth) {
        authTokens.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        authTokens.remove(authToken);
    }
}
