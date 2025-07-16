package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import service.requests.CreateGameRequest;
import service.requests.ListGamesRequest;
import service.results.CreateGameResult;
import service.results.ListGamesResult;

import java.util.List;

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
        // Validate gameName
        if (request.gameName() == null || request.gameName().isBlank()) {
            throw new BadRequestException("Missing game name");
        }

        // Check authentication
        AuthData authData = dataAccess.getAuth(request.authToken());
        if (authData == null) {
            throw new UnauthorizedException("Invalid auth token");
        }

        // Create the game
        int gameID = generateGameID();
        ChessGame chessGame = new ChessGame();
        GameData gameData = new GameData(gameID, null, null, request.gameName(), chessGame);

        // Store the game
        dataAccess.createGame(gameData);

        // Return gameID
        return new CreateGameResult(gameID);
    }

    public ListGamesResult listGames(ListGamesRequest request) throws DataAccessException {
        // Validate authentication
        AuthData authData = dataAccess.getAuth(request.authToken());
        if (authData == null) {
            throw new UnauthorizedException("Invalid auth token");
        }

        // Get the games
        List<GameData> games = dataAccess.listGames();

        // Return games (List<GameData>)
        return new ListGamesResult(games);
    }

    private int generateGameID() {
        return nextGameID++;
    }
}