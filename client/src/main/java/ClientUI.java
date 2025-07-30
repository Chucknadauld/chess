import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import ui.EscapeSequences;

public class ClientUI {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private String authToken;
    private List<ServerFacade.GameData> currentGames;
    private Integer currentGameID;
    private String playerColor;

    public ClientUI(String serverUrl) {
        this.serverFacade = new ServerFacade(serverUrl);
        this.scanner = new Scanner(System.in);
        this.authToken = null;
        this.currentGames = new ArrayList<>();
        this.currentGameID = null;
        this.playerColor = null;
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
            case "help", "h" -> printPreloginHelp();
            case "register", "r" -> handleRegister();
            case "login", "l" -> handleLogin();
            case "quit", "q", "exit" -> {
                System.out.println("Goodbye!");
                System.exit(0);
            }
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void printPreloginHelp() {
        System.out.println("register (r) - to create an account");
        System.out.println("login (l) - to play chess");
        System.out.println("quit (q) - to exit");
        System.out.println("help (h) - to see this message");
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
            System.out.println("Registration failed: " + e.getMessage());
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
            System.out.println("Login failed: " + e.getMessage());
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
            System.out.println("Logout failed: " + e.getMessage());
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
            System.out.println("Game created successfully! Game ID: " + result.gameID());
        } catch (Exception e) {
            System.out.println("Create game failed: " + e.getMessage());
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
            System.out.println("List games failed: " + e.getMessage());
        }
    }

    private void handlePlayGame() {
        try {
            if (currentGames.isEmpty()) {
                System.out.println("No games available. Use 'list' to see games.");
                return;
            }

            System.out.print("Game number: ");
            String input = scanner.nextLine().trim();

            int gameNumber;
            try {
                gameNumber = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid game number.");
                return;
            }

            if (gameNumber < 1 || gameNumber > currentGames.size()) {
                System.out.println("Game number out of range.");
                return;
            }

            System.out.print("Color (WHITE/BLACK): ");
            String colorInput = scanner.nextLine().trim().toUpperCase();

            if (!colorInput.equals("WHITE") && !colorInput.equals("BLACK")) {
                System.out.println("Invalid color. Must be WHITE or BLACK.");
                return;
            }

            ServerFacade.GameData game = currentGames.get(gameNumber - 1);
            serverFacade.joinGame(authToken, colorInput, game.gameID());
            
            currentGameID = game.gameID();
            playerColor = colorInput;
            
            System.out.println("Joined game as " + colorInput + " player!");
            System.out.println("Game: " + game.gameName());
            displayBoard();
            
        } catch (Exception e) {
            System.out.println("Join game failed: " + e.getMessage());
        }
    }

    private void handleObserveGame() {
        try {
            if (currentGames.isEmpty()) {
                System.out.println("No games available. Use 'list' to see games.");
                return;
            }

            System.out.print("Game number: ");
            String input = scanner.nextLine().trim();

            int gameNumber;
            try {
                gameNumber = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid game number.");
                return;
            }

            if (gameNumber < 1 || gameNumber > currentGames.size()) {
                System.out.println("Game number out of range.");
                return;
            }

            ServerFacade.GameData game = currentGames.get(gameNumber - 1);
            serverFacade.joinGame(authToken, null, game.gameID());
            
            currentGameID = game.gameID();
            playerColor = null;
            
            System.out.println("Now observing game: " + game.gameName());
            displayBoard();
            
        } catch (Exception e) {
            System.out.println("Observe game failed: " + e.getMessage());
        }
    }

    private void displayBoard() {
        boolean whiteBottom = playerColor == null || playerColor.equals("WHITE");
        
        String[][] board = createStartingBoard();
        
        System.out.println();
        
        if (whiteBottom) {
            printColumnLabels(false);
            for (int row = 7; row >= 0; row--) {
                printRow(board, row, row + 1, false);
            }
            printColumnLabels(false);
        } else {
            printColumnLabels(true);
            for (int row = 0; row < 8; row++) {
                printRow(board, row, 8 - row, true);
            }
            printColumnLabels(true);
        }
        
        System.out.println();
    }

    private String[][] createStartingBoard() {
        String[][] board = new String[8][8];
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = EscapeSequences.EMPTY;
            }
        }
        
        board[0][0] = EscapeSequences.BLACK_ROOK;
        board[0][1] = EscapeSequences.BLACK_KNIGHT;
        board[0][2] = EscapeSequences.BLACK_BISHOP;
        board[0][3] = EscapeSequences.BLACK_QUEEN;
        board[0][4] = EscapeSequences.BLACK_KING;
        board[0][5] = EscapeSequences.BLACK_BISHOP;
        board[0][6] = EscapeSequences.BLACK_KNIGHT;
        board[0][7] = EscapeSequences.BLACK_ROOK;
        
        for (int col = 0; col < 8; col++) {
            board[1][col] = EscapeSequences.BLACK_PAWN;
        }
        
        for (int col = 0; col < 8; col++) {
            board[6][col] = EscapeSequences.WHITE_PAWN;
        }
        
        board[7][0] = EscapeSequences.WHITE_ROOK;
        board[7][1] = EscapeSequences.WHITE_KNIGHT;
        board[7][2] = EscapeSequences.WHITE_BISHOP;
        board[7][3] = EscapeSequences.WHITE_QUEEN;
        board[7][4] = EscapeSequences.WHITE_KING;
        board[7][5] = EscapeSequences.WHITE_BISHOP;
        board[7][6] = EscapeSequences.WHITE_KNIGHT;
        board[7][7] = EscapeSequences.WHITE_ROOK;
        
        return board;
    }

    private void printColumnLabels(boolean flipped) {
        System.out.print("    ");
        if (flipped) {
            for (char col = 'h'; col >= 'a'; col--) {
                System.out.print(" " + col + "  ");
            }
        } else {
            for (char col = 'a'; col <= 'h'; col++) {
                System.out.print(" " + col + "  ");
            }
        }
        System.out.println();
    }

    private void printRow(String[][] board, int row, int rowLabel, boolean flipped) {
        System.out.print(" " + rowLabel + " ");
        
        for (int i = 0; i < 8; i++) {
            int col = flipped ? 7 - i : i;
            boolean isLightSquare = (row + col) % 2 == 0;
            
            if (isLightSquare) {
                System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);
                System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
            } else {
                System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN);
                System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
            }
            
            System.out.print(board[row][col]);
            System.out.print(EscapeSequences.RESET_BG_COLOR);
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        }
        
        System.out.println(" " + rowLabel + " ");
    }

    private void postloginMenu() {
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
            case "help", "h" -> printPostloginHelp();
            case "logout", "lo" -> handleLogout();
            case "create", "c" -> handleCreateGame();
            case "list", "ls" -> handleListGames();
            case "play", "p" -> handlePlayGame();
            case "observe", "o" -> handleObserveGame();
            case "quit", "q", "exit" -> {
                System.out.println("Goodbye!");
                System.exit(0);
            }
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void printPostloginHelp() {
        System.out.println("create (c) <name> - create a new game");
        System.out.println("list (ls) - list all games");
        System.out.println("play (p) <id> [WHITE|BLACK] - join a game as a player");
        System.out.println("observe (o) <id> - watch a game");
        System.out.println("logout (lo) - sign out");
        System.out.println("quit (q) - exit");
        System.out.println("help (h) - see this message");
    }
}