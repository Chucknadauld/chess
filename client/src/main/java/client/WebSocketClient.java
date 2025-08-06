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
            ServerMessage msg = gson.fromJson(message, ServerMessage.class);
            
            if (msg.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
                LoadGameMessage loadMsg = gson.fromJson(message, LoadGameMessage.class);
                messageHandler.handleLoadGame(loadMsg.getGame());
            } else if (msg.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
                ErrorMessage errMsg = gson.fromJson(message, ErrorMessage.class);
                messageHandler.handleError(errMsg.getErrorMessage());
            } else if (msg.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
                NotificationMessage notifMsg = gson.fromJson(message, NotificationMessage.class);
                messageHandler.handleNotification(notifMsg.getMessage());
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
            String msg = gson.toJson(command);
            session.getBasicRemote().sendText(msg);
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