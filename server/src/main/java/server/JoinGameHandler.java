package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import service.AlreadyTakenException;
import service.BadRequestException;
import service.GameService;
import service.UnauthorizedException;
import service.requests.JoinGameRequest;
import service.results.JoinGameResult;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class JoinGameHandler implements Route {
    private final DataAccess dataAccess;
    private final Gson gson = new Gson();

    public JoinGameHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            // Get auth token from header
            String authToken = request.headers("authorization");

            // Get JoinRequest from body
            Map<String, Object> body = gson.fromJson(request.body(), Map.class);
            String playerColor = (String) body.get("playerColor");
            int gameID = ((Double) body.get("gameID")).intValue();

            // Call the service
            GameService service = new GameService(dataAccess);
            JoinGameRequest joinGameRequest = new JoinGameRequest(authToken, playerColor, gameID);
            JoinGameResult result = service.joinGame(joinGameRequest);

            response.status(200);
            return gson.toJson(result);

        // Catch errors
        } catch (UnauthorizedException e) {
            response.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        } catch (BadRequestException e) {
            response.status(400);
            return gson.toJson(Map.of("message", "Error: bad request"));
        } catch (AlreadyTakenException e) {
            response.status(403);
            return gson.toJson(Map.of("message", "Error: already taken"));
        } catch (DataAccessException e) {
            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}