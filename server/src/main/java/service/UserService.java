package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import service.requests.RegisterRequest;
import service.results.RegisterResult;

import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess = new MemoryDataAccess(); // eventually injectx

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        // Check if username exists
        UserData existingUser = dataAccess.getUser(request.username());
        if (existingUser != null) {
            throw new AlreadyTakenException("Username is already taken");
        }


        // Create new UserData and AuthData
        UserData userData = new UserData(request.username(), request.password(), request.email());
        String token = UUID.randomUUID().toString();
        AuthData authData = new AuthData(token, request.username());

        // Store in memory
        dataAccess.createUser(userData);
        dataAccess.createAuth(authData);

        return new RegisterResult(userData.username(), token);
    }
}