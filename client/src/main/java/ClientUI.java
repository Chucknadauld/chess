import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import client.ServerFacade;
import client.WebSocketClient;
import ui.EscapeSequences;
import ui.ChessBoardRenderer;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.HashSet;

public class ClientUI {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private String authToken;
    private List<ServerFacade.GameData> currentGames;
    private Integer currentGameID;
    private String playerColor;
    private final Gson gson;
    private ChessGame currentGame;
    private final ChessBoardRenderer boardRenderer;

    public ClientUI(String serverUrl) {
        this.serverFacade = new ServerFacade(serverUrl);
        this.scanner = new Scanner(System.in);
        this.authToken = null;
        this.currentGames = new ArrayList<>();
        this.currentGameID = null;
        this.playerColor = null;
        this.gson = new Gson();
        this.currentGame = null;
        this.boardRenderer = new ChessBoardRenderer();
    }

    public void run() {
        System.out.println("Welcome to 240 Chess. Type Help to get started.");
        
        try {
            while (true) {
                if (authToken == null) {
                    preloginMenu();
                } else {
                    postloginMenu();
                }
            }
        } catch (Exception e) {
            System.out.println("An unexpected error occurred. Goodbye!");
            System.exit(1);
        }
    }

    private void preloginMenu() {
        System.out.print("[LOGGED_OUT] >>> ");
        String input = scanner.nextLine();
        
        if (input == null) {
            System.out.println("Goodbye!");
            System.exit(0);
        }
        
        input = input.trim().toLowerCase();
        
        if (input.isEmpty()) {
            return;
        }
        
        switch (input) {
            case "help" -> printPreloginHelp();
            case "register" -> handleRegister();
            case "login" -> handleLogin();
            case "quit", "exit" -> {
                System.out.println("Goodbye!");
                System.exit(0);
            }
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void printPreloginHelp() {
        System.out.println("register - to create an account");
        System.out.println("login - to play chess");
        System.out.println("quit - to exit");
        System.out.println("help - to see this message");
    }

    private void handleRegister() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                System.out.println("All fields are required.");
                return;
            }

            ServerFacade.RegisterResult result = serverFacade.register(username, password, email);
            authToken = result.authToken();
            System.out.println("Registration successful! Welcome " + result.username() + "!");
        } catch (Exception e) {
            System.out.println(parseErrorMessage(e.getMessage()));
        }
    }

    private void handleLogin() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            if (username.isEmpty() || password.isEmpty()) {
                System.out.println("Username and password are required.");
                return;
            }

            ServerFacade.LoginResult result = serverFacade.login(username, password);
            authToken = result.authToken();
            System.out.println("Login successful! Welcome back " + result.username() + "!");
        } catch (Exception e) {
            System.out.println(parseErrorMessage(e.getMessage()));
        }
    }

    private void handleLogout() {
        try {
            serverFacade.logout(authToken);
            authToken = null;
            currentGames.clear();
            currentGameID = null;
            playerColor = null;
            System.out.println("You have been logged out.");
        } catch (Exception e) {
            System.out.println(parseErrorMessage(e.getMessage()));
        }
    }

    private void handleCreateGame() {
        try {
            System.out.print("Game name: ");
            String gameName = scanner.nextLine().trim();

            if (gameName.isEmpty()) {
                System.out.println("Game name is required.");
                return;
            }

            ServerFacade.CreateGameResult result = serverFacade.createGame(authToken, gameName);
            System.out.println("Game created successfully!");
        } catch (Exception e) {
            System.out.println(parseErrorMessage(e.getMessage()));
        }
    }

    private void handleListGames() {
        try {
            ServerFacade.ListGamesResult result = serverFacade.listGames(authToken);
            currentGames = result.games();
            
            if (currentGames.isEmpty()) {
                System.out.println("No games available.");
                return;
            }
            
            System.out.println("Games:");
            for (int i = 0; i < currentGames.size(); i++) {
                ServerFacade.GameData game = currentGames.get(i);
                int gameNumber = i + 1;
                String white = game.whiteUsername() != null ? game.whiteUsername() : "";
                String black = game.blackUsername() != null ? game.blackUsername() : "";
                
                System.out.println(gameNumber + ". " + game.gameName() + 
                    " (White: " + white + ", Black: " + black + ")");
            }
        } catch (Exception e) {
            System.out.println(parseErrorMessage(e.getMessage()));
        }
    }

    private void handlePlayGame() {
        try {
            ServerFacade.GameData game = promptUserForGame();
            if (game == null) {
                return;
            }
    
            System.out.print("Color (WHITE/BLACK): ");
            String colorInput = scanner.nextLine().trim().toUpperCase();
    
            if (!colorInput.equals("WHITE") && !colorInput.equals("BLACK")) {
                System.out.println("Invalid color. Must be WHITE or BLACK.");
                return;
            }
    
            serverFacade.joinGame(authToken, colorInput, game.gameID());
    
            currentGameID = game.gameID();
            playerColor = colorInput;
    
            System.out.println("Joined game as " + colorInput + " player!");
            System.out.println("Game: " + game.gameName());
            connectToGameWebSocket();
    
        } catch (Exception e) {
            System.out.println(parseErrorMessage(e.getMessage()));
        }
    }    

    private void handleObserveGame() {
        try {
            ServerFacade.GameData game = promptUserForGame();
            if (game == null) {
                return;
            }
    
            currentGameID = game.gameID();
            playerColor = null;
    
            System.out.println("Now observing game: " + game.gameName());
            connectToGameWebSocket();
    
        } catch (Exception e) {
            System.out.println(parseErrorMessage(e.getMessage()));
        }
    }

    private void postloginMenu() {
        if (currentGameID != null) {
            gameplayMenu();
        } else {
            mainMenu();
        }
    }

    private void mainMenu() {
        System.out.print("[LOGGED_IN] >>> ");
        String input = scanner.nextLine();
        
        if (input == null) {
            System.out.println("Goodbye!");
            System.exit(0);
        }
        
        input = input.trim().toLowerCase();
        
        if (input.isEmpty()) {
            return;
        }
        
        switch (input) {
            case "help" -> printPostloginHelp();
            case "logout" -> handleLogout();
            case "create" -> handleCreateGame();
            case "list" -> handleListGames();
            case "play" -> handlePlayGame();
            case "observe" -> handleObserveGame();
            case "quit", "exit" -> {
                System.out.println("Goodbye!");
                System.exit(0);
            }
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void gameplayMenu() {
        System.out.print(">>> ");
        String input = scanner.nextLine();
        
        if (input == null) {
            return;
        }
        
        input = input.trim();
        
        if (input.isEmpty()) {
            return;
        }
        
        String[] parts = input.split(" ");
        String command = parts[0].toLowerCase();
        
        switch (command) {
            case "help" -> printGameplayHelp();
            case "move" -> handleMove(parts);
            case "leave" -> handleLeaveGame();
            case "resign" -> handleResignGame();
            case "redraw" -> redrawBoard();
            case "highlight" -> handleHighlight(parts);
            case "clear" -> clearHighlights();
            case "quit", "exit" -> handleLeaveGame();
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void printPostloginHelp() {
        System.out.println("create - create a new game");
        System.out.println("list - list all games");
        System.out.println("play - join a game as a player");
        System.out.println("observe - watch a game");
        System.out.println("logout - sign out");
        System.out.println("quit - exit");
    }

    private ServerFacade.GameData promptUserForGame() {
        if (currentGames.isEmpty()) {
            System.out.println("No games available. Use 'list' to see games.");
            return null;
        }
    
        System.out.print("Game number: ");
        String input = scanner.nextLine().trim();
    
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid game number.");
            return null;
        }
    
        if (gameNumber < 1 || gameNumber > currentGames.size()) {
            System.out.println("Game number out of range.");
            return null;
        }
    
        return currentGames.get(gameNumber - 1);
    }

    private String parseErrorMessage(String errorMessage) {
        try {
            int jsonStart = errorMessage.indexOf("{\"message\":");
            if (jsonStart != -1) {
                return extractMessageFromJson(errorMessage, jsonStart);
            }
        } catch (Exception e) {
        }
        return errorMessage;
    }

    private String extractMessageFromJson(String errorMessage, int jsonStart) {
        int jsonEnd = errorMessage.lastIndexOf("}") + 1;
        if (jsonEnd > jsonStart) {
            String jsonPart = errorMessage.substring(jsonStart, jsonEnd);
            JsonObject jsonError = gson.fromJson(jsonPart, JsonObject.class);
            if (jsonError != null && jsonError.has("message")) {
                String cleanMessage = jsonError.get("message").getAsString();
                if (cleanMessage.startsWith("Error: ")) {
                    cleanMessage = cleanMessage.substring(7);
                }
                return errorMessage.substring(0, jsonStart) + cleanMessage;
            }
        }
        return errorMessage;
    }

    private void connectToGameWebSocket() {
        try {
            WebSocketClient.MessageHandler messageHandler = new WebSocketClient.MessageHandler() {
                public void handleLoadGame(ChessGame game) {
                    currentGame = game;
                    boardRenderer.clearHighlights();
                    System.out.println("\n=== Game Updated ===");
                    boardRenderer.displayGameBoard(game, playerColor);
                    System.out.print(">>> ");
                }

                public void handleError(String errorMessage) {
                    System.out.println("Error: " + errorMessage);
                }

                public void handleNotification(String message) {
                    System.out.println("\n>>> " + message);
                    System.out.print(">>> ");
                }
            };

            serverFacade.connectToGame(authToken, currentGameID, messageHandler);
            System.out.println("Connected to game via WebSocket");
        } catch (Exception e) {
            System.out.println("Failed to connect to game: " + e.getMessage());
        }
    }

    private void printGameplayHelp() {
        System.out.println("Available commands:");
        if (playerColor != null) {
            System.out.println("move <from> <to> - make a move (e.g., move e2 e4)");
            System.out.println("resign - forfeit the game");
        }
        System.out.println("redraw - redraw the board");
        System.out.println("highlight <position> - show legal moves (e.g., highlight e2)");
        System.out.println("clear - clear move highlights");
        System.out.println("leave - leave the game");
    }

    private void handleMove(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Usage: move <from> <to> (e.g., move e2 e4)");
            return;
        }

        try {
            ChessPosition from = parsePosition(parts[1]);
            ChessPosition to = parsePosition(parts[2]);
            ChessMove move = new ChessMove(from, to, null);
            
            serverFacade.makeMove(authToken, currentGameID, move);
            System.out.println("Move sent!");
        } catch (Exception e) {
            System.out.println("Invalid move: " + e.getMessage());
        }
    }

    private void handleLeaveGame() {
        try {
            if (currentGameID != null) {
                serverFacade.leaveGame(authToken, currentGameID);
                serverFacade.disconnectFromGame();
                currentGameID = null;
                playerColor = null;
                System.out.println("Left the game");
            }
        } catch (Exception e) {
            System.out.println("Error leaving game: " + e.getMessage());
        }
    }

    private void handleResignGame() {
        if (playerColor == null) {
            System.out.println("Observers cannot resign");
            return;
        }

        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if (confirm.equals("yes") || confirm.equals("y")) {
            try {
                serverFacade.resignGame(authToken, currentGameID);
                System.out.println("You have resigned from the game");
            } catch (Exception e) {
                System.out.println("Error resigning: " + e.getMessage());
            }
        } else {
            System.out.println("Resign cancelled");
        }
    }

    private void redrawBoard() {
        System.out.println("\nCurrent board state:");
        if (currentGame != null) {
            boardRenderer.displayGameBoard(currentGame, playerColor);
            System.out.print(">>> ");
        } else {
            String[][] emptyBoard = boardRenderer.createEmptyBoard();
            boardRenderer.displayBoard(emptyBoard, true);
        }
    }

    private void handleHighlight(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: highlight <position> (e.g., highlight e2)");
            return;
        }

        if (currentGame == null) {
            System.out.println("No game loaded yet");
            return;
        }

        try {
            ChessPosition pos = parsePosition(parts[1]);
            HashSet<ChessPosition> highlights = new HashSet<>();

            Collection<ChessMove> legalMoves = currentGame.validMoves(pos);
            if (legalMoves.isEmpty()) {
                System.out.println("No legal moves for piece at " + parts[1]);
                return;
            }

            for (ChessMove move : legalMoves) {
                highlights.add(move.getEndPosition());
            }
            
            boardRenderer.setHighlightedSquares(highlights);
            System.out.println("Highlighting " + legalMoves.size() + " legal moves for piece at " + parts[1] + ":");
            boardRenderer.displayGameBoard(currentGame, playerColor);
            System.out.print(">>> ");
        } catch (Exception e) {
            System.out.println("Invalid position: " + e.getMessage());
        }
    }

    private void clearHighlights() {
        boardRenderer.clearHighlights();
        System.out.println("Cleared move highlights");
        if (currentGame != null) {
            boardRenderer.displayGameBoard(currentGame, playerColor);
            System.out.print(">>> ");
        }
    }

    private ChessPosition parsePosition(String pos) {
        if (pos.length() != 2) {
            throw new IllegalArgumentException("Invalid position format");
        }
        
        char col = pos.charAt(0);
        char row = pos.charAt(1);
        
        if (col < 'a' || col > 'h' || row < '1' || row > '8') {
            throw new IllegalArgumentException("Position out of bounds");
        }
        
        return new ChessPosition(row - '0', col - 'a' + 1);
    }
}