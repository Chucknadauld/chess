package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.LogoutResult;
import service.results.RegisterResult;

import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        UserData existingUser = dataAccess.getUser(request.username());
        if (existingUser != null) {
            throw new AlreadyTakenException("Username is already taken");
        }

        UserData userData = new UserData(request.username(), request.password(), request.email());
        String token = UUID.randomUUID().toString();
        AuthData authData = new AuthData(token, request.username());

        dataAccess.createUser(userData);
        dataAccess.createAuth(authData);

        return new RegisterResult(userData.username(), token);
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        UserData user = dataAccess.getUser(request.username());

        if (user == null || !user.password().equals(request.password())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, user.username());
        dataAccess.createAuth(auth);

        return new LoginResult(user.username(), token);
    }

    public LogoutResult logout(LogoutRequest request) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(request.authToken());
        if (authData == null) {
            throw new UnauthorizedException("Invalid auth token");
        }

        dataAccess.deleteAuth(request.authToken());

        return new LogoutResult();
    }
}