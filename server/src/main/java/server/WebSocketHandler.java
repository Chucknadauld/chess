package server;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.ServerMessage;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    
    private final ConcurrentHashMap<Session, String> sessionToAuthToken = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<Session, String>> gameToSessions = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();
    private static DataAccess dataAccess;

    public static void setDataAccess(DataAccess dataAccessInstance) {
        dataAccess = dataAccessInstance;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        System.out.println("WebSocket connection established");
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket connection closed");
        String authToken = sessionToAuthToken.remove(session);
        
        for (Integer gameID : gameToSessions.keySet()) {
            ConcurrentHashMap<Session, String> sessions = gameToSessions.get(gameID);
            if (sessions != null && sessions.containsKey(session)) {
                sessions.remove(session);
                
                try {
                    if (authToken != null) {
                        AuthData auth = dataAccess.getAuth(authToken);
                        if (auth != null) {
                            String user = auth.username();
                            String disconnectMsg = user + " disconnected";
                            broadcastToGame(gameID, disconnectMsg, session);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error handling disconnect: " + e.getMessage());
                }
                
                if (sessions.isEmpty()) {
                    gameToSessions.remove(gameID);
                }
                break;
            }
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            UserGameCommand baseCommand = gson.fromJson(message, UserGameCommand.class);
            
            switch (baseCommand.getCommandType()) {
                case CONNECT:
                    handleConnect(session, baseCommand);
                    break;
                case MAKE_MOVE:
                    handleMakeMove(session, message);
                    break;
                case LEAVE:
                    handleLeave(session, baseCommand);
                    break;
                case RESIGN:
                    handleResign(session, baseCommand);
                    break;
            }
        } catch (Exception e) {
            sendErrorMessage(session, "Invalid command: " + e.getMessage());
        }
    }

    private void handleConnect(Session session, UserGameCommand command) throws IOException {
        try {
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                sendErrorMessage(session, "Invalid auth token");
                return;
            }

            GameData game = dataAccess.getGame(command.getGameID());
            if (game == null) {
                sendErrorMessage(session, "Game not found");
                return;
            }

            sessionToAuthToken.put(session, command.getAuthToken());
            if (!gameToSessions.containsKey(command.getGameID())) {
                gameToSessions.put(command.getGameID(), new ConcurrentHashMap<>());
            }
            gameToSessions.get(command.getGameID()).put(session, command.getAuthToken());

            sendLoadGameMessage(session, game.game());

            String user = auth.username();
            boolean isPlayer = user.equals(game.whiteUsername()) || user.equals(game.blackUsername());
            String msg = isPlayer ? user + " joined the game" : user + " is observing the game";
            broadcastToGame(command.getGameID(), msg, session);

            System.out.println("User " + user + " connected to game " + command.getGameID());
        } catch (DataAccessException e) {
            sendErrorMessage(session, "Database error: " + e.getMessage());
        }
    }

    private void handleMakeMove(Session session, String message) throws IOException {
        try {
            MakeMoveCommand command = gson.fromJson(message, MakeMoveCommand.class);
            
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                sendErrorMessage(session, "Invalid auth token");
                return;
            }

            GameData game = dataAccess.getGame(command.getGameID());
            if (game == null) {
                sendErrorMessage(session, "Game not found");
                return;
            }

            String user = auth.username();
            chess.ChessGame chessGame = game.game();
            
            boolean isWhitePlayer = user.equals(game.whiteUsername());
            boolean isBlackPlayer = user.equals(game.blackUsername());
            
            if (!isWhitePlayer && !isBlackPlayer) {
                sendErrorMessage(session, "You are not a player in this game");
                return;
            }

            chess.ChessGame.TeamColor currentTurn = chessGame.getTeamTurn();
            if ((currentTurn == chess.ChessGame.TeamColor.WHITE && !isWhitePlayer) ||
                (currentTurn == chess.ChessGame.TeamColor.BLACK && !isBlackPlayer)) {
                sendErrorMessage(session, "It's not your turn");
                return;
            }

            chessGame.makeMove(command.getMove());
            
            GameData updatedGame = new GameData(game.gameID(), game.whiteUsername(), 
                                              game.blackUsername(), game.gameName(), chessGame);
            dataAccess.updateGame(updatedGame);

            ConcurrentHashMap<Session, String> sessions = gameToSessions.get(command.getGameID());
            if (sessions != null) {
                for (Session s : sessions.keySet()) {
                    if (s.isOpen()) {
                        sendLoadGameMessage(s, chessGame);
                    }
                }
            }

            String fromSquare = positionToString(command.getMove().getStartPosition());
            String toSquare = positionToString(command.getMove().getEndPosition());
            String moveMsg = user + " moved " + fromSquare + " to " + toSquare;
            broadcastToGame(command.getGameID(), moveMsg, null);

            chess.ChessGame.TeamColor nextTurn = chessGame.getTeamTurn();
            if (chessGame.isInCheck(nextTurn)) {
                if (chessGame.isInCheckmate(nextTurn)) {
                    String checkmateMsg = user + " wins! " + (nextTurn == chess.ChessGame.TeamColor.WHITE ? "White" : "Black") + " is in checkmate";
                    broadcastToGame(command.getGameID(), checkmateMsg, null);
                } else {
                    String checkMsg = (nextTurn == chess.ChessGame.TeamColor.WHITE ? "White" : "Black") + " is in check";
                    broadcastToGame(command.getGameID(), checkMsg, null);
                }
            } else if (chessGame.isInStalemate(nextTurn)) {
                String stalemateMsg = "Game is a draw by stalemate";
                broadcastToGame(command.getGameID(), stalemateMsg, null);
            }

        } catch (chess.InvalidMoveException e) {
            sendErrorMessage(session, "Invalid move: " + e.getMessage());
        } catch (DataAccessException e) {
            sendErrorMessage(session, "Database error: " + e.getMessage());
        }
    }

    private void handleLeave(Session session, UserGameCommand command) throws IOException {
        try {
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                sendErrorMessage(session, "Invalid auth token");
                return;
            }

            String user = auth.username();
            Integer gameID = command.getGameID();

            sessionToAuthToken.remove(session);
            ConcurrentHashMap<Session, String> sessions = gameToSessions.get(gameID);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    gameToSessions.remove(gameID);
                }
            }

            String leaveMsg = user + " left the game";
            broadcastToGame(gameID, leaveMsg, session);

            System.out.println("User " + user + " left game " + gameID);
        } catch (DataAccessException e) {
            sendErrorMessage(session, "Database error: " + e.getMessage());
        }
    }

    private void handleResign(Session session, UserGameCommand command) throws IOException {
        try {
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                sendErrorMessage(session, "Invalid auth token");
                return;
            }

            GameData game = dataAccess.getGame(command.getGameID());
            if (game == null) {
                sendErrorMessage(session, "Game not found");
                return;
            }

            String user = auth.username();
            boolean isWhitePlayer = user.equals(game.whiteUsername());
            boolean isBlackPlayer = user.equals(game.blackUsername());
            
            if (!isWhitePlayer && !isBlackPlayer) {
                sendErrorMessage(session, "You are not a player in this game");
                return;
            }

            String winner;
            if (isWhitePlayer) {
                winner = game.blackUsername() != null ? game.blackUsername() : "Black";
            } else {
                winner = game.whiteUsername() != null ? game.whiteUsername() : "White";
            }

            String resignMsg = user + " resigned. " + winner + " wins!";
            broadcastToGame(command.getGameID(), resignMsg, null);

            System.out.println("User " + user + " resigned from game " + command.getGameID());
        } catch (DataAccessException e) {
            sendErrorMessage(session, "Database error: " + e.getMessage());
        }
    }

    private void sendErrorMessage(Session session, String errorMessage) throws IOException {
        ErrorMessage error = new ErrorMessage(errorMessage);
        session.getRemote().sendString(gson.toJson(error));
    }

    private void sendLoadGameMessage(Session session, chess.ChessGame game) throws IOException {
        LoadGameMessage loadGame = new LoadGameMessage(game);
        session.getRemote().sendString(gson.toJson(loadGame));
    }

    private void sendNotificationMessage(Session session, String message) throws IOException {
        NotificationMessage notification = new NotificationMessage(message);
        session.getRemote().sendString(gson.toJson(notification));
    }

    private void broadcastToGame(Integer gameID, String message, Session excludeSession) throws IOException {
        ConcurrentHashMap<Session, String> sessions = gameToSessions.get(gameID);
        if (sessions != null) {
            for (Session s : sessions.keySet()) {
                if (!s.equals(excludeSession) && s.isOpen()) {
                    sendNotificationMessage(s, message);
                }
            }
        }
    }

    private String positionToString(chess.ChessPosition pos) {
        char col = (char) ('a' + pos.getColumn() - 1);
        char row = (char) ('0' + pos.getRow());
        return "" + col + row;
    }
}