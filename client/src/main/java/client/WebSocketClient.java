package client;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebSocketClient {
    
    private Session session;
    private final Gson gson = new Gson();
    private MessageHandler messageHandler;

    public interface MessageHandler {
        void handleLoadGame(chess.ChessGame game);
        void handleError(String errorMessage);
        void handleNotification(String message);
    }

    public WebSocketClient(String serverUrl, MessageHandler messageHandler) throws Exception {
        this.messageHandler = messageHandler;
        URI uri = new URI(serverUrl.replace("http", "ws") + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("WebSocket connection opened");
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            ServerMessage baseMessage = gson.fromJson(message, ServerMessage.class);
            
            switch (baseMessage.getServerMessageType()) {
                case LOAD_GAME:
                    LoadGameMessage loadGameMessage = gson.fromJson(message, LoadGameMessage.class);
                    messageHandler.handleLoadGame(loadGameMessage.getGame());
                    break;
                case ERROR:
                    ErrorMessage errorMessage = gson.fromJson(message, ErrorMessage.class);
                    messageHandler.handleError(errorMessage.getErrorMessage());
                    break;
                case NOTIFICATION:
                    NotificationMessage notificationMessage = gson.fromJson(message, NotificationMessage.class);
                    messageHandler.handleNotification(notificationMessage.getMessage());
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("WebSocket connection closed: " + closeReason.getReasonPhrase());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }

    public void sendCommand(UserGameCommand command) throws IOException {
        if (session != null && session.isOpen()) {
            String message = gson.toJson(command);
            session.getBasicRemote().sendText(message);
        } else {
            throw new IOException("WebSocket connection is not open");
        }
    }

    public void close() throws IOException {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}