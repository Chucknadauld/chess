package server;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    
    private final ConcurrentHashMap<Session, String> sessionToAuthToken = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<Session, String>> gameToSessions = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        System.out.println("WebSocket connection established");
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket connection closed");
        String authToken = sessionToAuthToken.remove(session);
        gameToSessions.values().forEach(sessions -> sessions.remove(session));
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            
            switch (command.getCommandType()) {
                case CONNECT:
                    handleConnect(session, command);
                    break;
                case MAKE_MOVE:
                    handleMakeMove(session, command);
                    break;
                case LEAVE:
                    handleLeave(session, command);
                    break;
                case RESIGN:
                    handleResign(session, command);
                    break;
            }
        } catch (Exception e) {
            sendErrorMessage(session, "Invalid command: " + e.getMessage());
        }
    }

    private void handleConnect(Session session, UserGameCommand command) throws IOException {
        sessionToAuthToken.put(session, command.getAuthToken());
        gameToSessions.computeIfAbsent(command.getGameID(), k -> new ConcurrentHashMap<>())
                     .put(session, command.getAuthToken());
        System.out.println("Client connected to game " + command.getGameID());
    }

    private void handleMakeMove(Session session, UserGameCommand command) throws IOException {
        System.out.println("Handling make move for game " + command.getGameID());
    }

    private void handleLeave(Session session, UserGameCommand command) throws IOException {
        System.out.println("Handling leave for game " + command.getGameID());
    }

    private void handleResign(Session session, UserGameCommand command) throws IOException {
        System.out.println("Handling resign for game " + command.getGameID());
    }

    private void sendErrorMessage(Session session, String errorMessage) throws IOException {
        ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        session.getRemote().sendString(gson.toJson(error));
    }
}