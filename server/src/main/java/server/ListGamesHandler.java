package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import service.BadRequestException;
import service.GameService;
import service.UnauthorizedException;
import service.requests.ListGamesRequest;
import service.results.ListGamesResult;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class ListGamesHandler implements Route {
    private final DataAccess dataAccess;
    private final Gson gson = new Gson();

    public ListGamesHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            // Get authToken from header
            String authToken = request.headers("authorization");

            // Call the service
            GameService service = new GameService(dataAccess);
            ListGamesRequest listGamesRequest = new ListGamesRequest(authToken);
            ListGamesResult result = service.listGames(listGamesRequest);

            // Return ListGamesResult
            response.status(200);
            return gson.toJson(result);

        // Catch exceptions
        } catch (UnauthorizedException e) {
            response.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        } catch (DataAccessException e) {
            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
