package server;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;
import service.UserService;
import service.requests.LoginRequest;
import service.results.LoginResult;
import service.UnauthorizedException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;

import java.util.Map;

public class LoginHandler implements Route {
    private final DataAccess dataAccess;
    private final Gson gson = new Gson();

    public LoginHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);

            // Parse and validate request
            String username = loginRequest.username();
            String password = loginRequest.password();
            if (username == null || username.isBlank() ||
                    password == null || password.isBlank()) {
                response.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            // Call the service layer
            UserService service = new UserService(dataAccess);
            LoginResult result = service.login(loginRequest);

            response.status(200);
            return gson.toJson(result);

        } catch (UnauthorizedException e) {
            response.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        } catch (DataAccessException e) {
            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
