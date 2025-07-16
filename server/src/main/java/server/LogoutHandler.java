package server;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;
import service.UserService;
import service.requests.LogoutRequest;
import service.results.LogoutResult;
import service.UnauthorizedException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;

import java.util.Map;

public class LogoutHandler implements Route {
    private final DataAccess dataAccess;
    private final Gson gson = new Gson();

    public LogoutHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            // Get authToken
            String authToken = request.headers("authorization");
            
            // Validate it
            if (authToken == null || authToken.isBlank()) {
                response.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            UserService service = new UserService(dataAccess);
            LogoutRequest logoutRequest = new LogoutRequest(authToken);
            LogoutResult result = service.logout(logoutRequest);

            response.status(200);
            return gson.toJson(Map.of());

        } catch (UnauthorizedException e) {
            response.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        } catch (DataAccessException e) {
            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
} 