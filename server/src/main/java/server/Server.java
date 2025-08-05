package server;

import dataaccess.MySQLDataAccess;
import dataaccess.DataAccessException;
import spark.*;

import static spark.Spark.*;

public class Server {
    private final MySQLDataAccess dataAccess;

    public Server() {
        try {
            dataAccess = new MySQLDataAccess();
        } catch (DataAccessException ex) {
            throw new RuntimeException("Failed to initialize database", ex);
        }
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        webSocket("/ws", WebSocketHandler.class);

        delete("/db", new ClearHandler(dataAccess));
        post("/user", new RegisterHandler(dataAccess));
        post("/session", new LoginHandler(dataAccess));
        delete("/session", new LogoutHandler(dataAccess));
        post("/game", new CreateGameHandler(dataAccess));
        get("/game", new ListGamesHandler(dataAccess));
        put("/game", new JoinGameHandler(dataAccess));

        Spark.exception(Exception.class, (exception, request, response) -> {
            response.status(500);
            response.type("application/json");
            String errorMessage = "Unknown error";
            if (exception != null && exception.getMessage() != null) {
                errorMessage = exception.getMessage();
            }
            response.body("{\"message\": \"Error: " + errorMessage + "\"}");
        });

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
