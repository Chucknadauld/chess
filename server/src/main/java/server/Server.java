package server;

import dataaccess.MemoryDataAccess;
import spark.*;

import static spark.Spark.*;

public class Server {
    private final MemoryDataAccess memoryDataAccess = new MemoryDataAccess();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        delete("/db", new ClearHandler());
        post("/user", new RegisterHandler(memoryDataAccess));
        post("/session", new LoginHandler(memoryDataAccess));

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
