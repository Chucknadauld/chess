package server;

import com.google.gson.Gson;
import service.UserService;
import service.requests.RegisterRequest;
import service.results.RegisterResult;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import service.AlreadyTakenException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class RegisterHandler implements Route {

    private final DataAccess dataAccess;
    private final Gson gson = new Gson();

    public RegisterHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            RegisterRequest registerRequest = gson.fromJson(request.body(), RegisterRequest.class);

            if (registerRequest.username() == null || registerRequest.username().isBlank() ||
                    registerRequest.password() == null || registerRequest.password().isBlank() ||
                    registerRequest.email() == null || registerRequest.email().isBlank()) {
                response.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            UserService service = new UserService(dataAccess);
            RegisterResult result = service.register(registerRequest);

            response.status(200);
            return gson.toJson(result);

        } catch (AlreadyTakenException e) {
            response.status(403);
            return gson.toJson(Map.of("message", "Error: already taken"));
        } catch (DataAccessException e) {
            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
