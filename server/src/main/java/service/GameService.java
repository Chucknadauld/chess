package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import service.requests.CreateGameRequest;
import service.results.CreateGameResult;

public class GameService {
    private final DataAccess dataAccess;
    static int nextGameID = 1;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    static void resetGameIDForTesting() {
        nextGameID = 1;
    }

    public CreateGameResult createGame(CreateGameRequest request) throws DataAccessException {
        // Validate gameName first (bad request validation)
        if (request.gameName() == null || request.gameName().isBlank()) {
            throw new BadRequestException("Missing game name");
        }

        // Check authentication (follows sequence diagram: Service -> DataAccess: getAuth(authToken))
        AuthData authData = dataAccess.getAuth(request.authToken());
        if (authData == null) {
            throw new UnauthorizedException("Invalid auth token");
        }

        // Create the game
        int gameID = generateGameID();
        ChessGame chessGame = new ChessGame();
        GameData gameData = new GameData(gameID, null, null, request.gameName(), chessGame);

        // Store the game (Service -> DataAccess: createGame(GameData))
        dataAccess.createGame(gameData);

        // Return gameID (Service -> Handler: CreateGameResult)
        return new CreateGameResult(gameID);
    }

    private int generateGameID() {
        return nextGameID++;
    }
}