package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import service.requests.LoginRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;

import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

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

    public LoginResult login(LoginRequest request) throws DataAccessException {
        // Find user
        UserData user = dataAccess.getUser(request.username());

        if (user == null || !user.password().equals(request.password())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // Generate token and save it
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, user.username());
        dataAccess.createAuth(auth);

        return new LoginResult(user.username(), token);
    }
}