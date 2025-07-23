package server;

import com.google.gson.Gson;
import service.ClearService;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;

public class ClearHandler implements Route {
    private final DataAccess dataAccess;

    public ClearHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public Object handle(Request request, Response response) {
        ClearService service = new ClearService(dataAccess);
        try {
            service.clearApplication();
            response.status(200);
            return new Gson().toJson(new HashMap<>());
        } catch (DataAccessException e) {
            response.status(500);
            HashMap<String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Error: " + e.getMessage());
            return new Gson().toJson(responseMap);
        }
    }
}
