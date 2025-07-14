package server;

import com.google.gson.Gson;
import service.ClearService;
import dataaccess.DataAccessException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;

public class ClearHandler implements Route {
    @Override
    public Object handle(Request request, Response response) {
        ClearService service = new ClearService();
        try {
            service.clearApplication();
            response.status(200);
            return new Gson().toJson(new HashMap<>()); // return {}
        } catch (DataAccessException e) {
            response.status(500);
            return new Gson().toJson(
                    new HashMap<>() {{
                        put("message", "Error: " + e.getMessage());
                    }}
            );
        }
    }
}
