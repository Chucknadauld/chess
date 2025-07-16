package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.requests.ListGamesRequest;
import service.results.CreateGameResult;
import service.results.JoinGameResult;
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
        if (request.gameName() == null || request.gameName().isBlank()) {
            throw new BadRequestException("Missing game name");
        }

        AuthData authData = dataAccess.getAuth(request.authToken());
        if (authData == null) {
            throw new UnauthorizedException("Invalid auth token");
        }

        int gameID = generateGameID();
        ChessGame chessGame = new ChessGame();
        GameData gameData = new GameData(gameID, null, null, request.gameName(), chessGame);

        dataAccess.createGame(gameData);

        return new CreateGameResult(gameID);
    }

    public ListGamesResult listGames(ListGamesRequest request) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(request.authToken());
        if (authData == null) {
            throw new UnauthorizedException("Invalid auth token");
        }

        List<GameData> games = dataAccess.listGames();

        return new ListGamesResult(games);
    }

    public JoinGameResult joinGame(JoinGameRequest request) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(request.authToken());
        if (authData == null) {
            throw new UnauthorizedException("Invalid auth token");
        }

        GameData gameData = dataAccess.getGame(request.gameID());
        if (gameData == null) {
            throw new BadRequestException("Missing game data");
        }

        String playerColor = request.playerColor();

        if (playerColor == null || (!playerColor.equals("BLACK") && !playerColor.equals("WHITE"))) {
            throw new BadRequestException("Invalid player color");
        }

        String currentPlayer = (playerColor.equals("WHITE")) ? gameData.whiteUsername() : gameData.blackUsername();
        if (currentPlayer != null) {
            throw new AlreadyTakenException("Player color is already taken");
        }

        String username = authData.username();
        if (playerColor.equals("WHITE")) {
            GameData updatedGameData = new GameData(
                request.gameID(), 
                username, 
                gameData.blackUsername(), 
                gameData.gameName(), 
                gameData.game());
            dataAccess.updateGame(updatedGameData);
        }
        if (playerColor.equals("BLACK")) {
            GameData updatedGameData = new GameData(
                request.gameID(), 
                gameData.whiteUsername(), 
                username, 
                gameData.gameName(), 
                gameData.game());
            dataAccess.updateGame(updatedGameData);
        }

        return new JoinGameResult();
    }

    private int generateGameID() {
        return nextGameID++;
    }
}