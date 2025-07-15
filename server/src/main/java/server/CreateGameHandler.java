package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import service.BadRequestException;
import service.GameService;
import service.UnauthorizedException;
import service.requests.CreateGameRequest;
import service.results.CreateGameResult;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class CreateGameHandler implements Route {
    private final DataAccess dataAccess;
    private final Gson gson = new Gson();

    public CreateGameHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            // Get authToken from header
            String authToken = request.headers("authorization");
            
            // Get gameName from body
            Map<String, String> body = gson.fromJson(request.body(), Map.class);
            String gameName = body != null ? body.get("gameName") : null;

            // Call the service (no validation in handler)
            GameService service = new GameService(dataAccess);
            CreateGameRequest createGameRequest = new CreateGameRequest(authToken, gameName);
            CreateGameResult result = service.createGame(createGameRequest);

            response.status(200);
            return gson.toJson(result);

        } catch (BadRequestException e) {
            response.status(400);
            return gson.toJson(Map.of("message", "Error: bad request"));
        } catch (UnauthorizedException e) {
            response.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        } catch (DataAccessException e) {
            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}