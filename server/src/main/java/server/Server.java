package server;

import dataaccess.MemoryDataAccess;
import spark.*;

import static spark.Spark.*;

public class Server {
    private final MemoryDataAccess memoryDataAccess = new MemoryDataAccess();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        delete("/db", new ClearHandler(memoryDataAccess));
        post("/user", new RegisterHandler(memoryDataAccess));
        post("/session", new LoginHandler(memoryDataAccess));
        delete("/session", new LogoutHandler(memoryDataAccess));

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
