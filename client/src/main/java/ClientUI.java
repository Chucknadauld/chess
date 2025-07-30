import java.util.Scanner;

public class ClientUI {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private String authToken;

    public ClientUI(String serverUrl) {
        this.serverFacade = new ServerFacade(serverUrl);
        this.scanner = new Scanner(System.in);
        this.authToken = null;
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
            String username = scanner.nextLine();
            if (username == null) return;
            username = username.trim();
            
            System.out.print("Password: ");
            String password = scanner.nextLine();
            if (password == null) return;
            password = password.trim();
            
            System.out.print("Email: ");
            String email = scanner.nextLine();
            if (email == null) return;
            email = email.trim();

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
            String username = scanner.nextLine();
            if (username == null) return;
            username = username.trim();
            
            System.out.print("Password: ");
            String password = scanner.nextLine();
            if (password == null) return;
            password = password.trim();

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
            System.out.println("You have been logged out.");
        } catch (Exception e) {
            System.out.println("Logout failed: " + e.getMessage());
            authToken = null;
        }
    }

    private void handleCreateGame() {
        try {
            System.out.print("Game name: ");
            String gameName = scanner.nextLine();
            if (gameName == null) return;
            gameName = gameName.trim();

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
            case "list", "ls" -> System.out.println("List games command not yet implemented");
            case "play", "p" -> System.out.println("Play game command not yet implemented");
            case "observe", "o" -> System.out.println("Observe game command not yet implemented");
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