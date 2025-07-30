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

    private void postloginMenu() {
        System.out.print("[LOGGED_IN] >>> ");
        String input = scanner.nextLine().trim().toLowerCase();
        
        switch (input) {
            case "help" -> System.out.println("Postlogin help not yet implemented");
            case "logout" -> System.out.println("Logout command not yet implemented");
            case "create" -> System.out.println("Create game command not yet implemented");
            case "list" -> System.out.println("List games command not yet implemented");
            case "play" -> System.out.println("Play game command not yet implemented");
            case "observe" -> System.out.println("Observe game command not yet implemented");
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }
}