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
        
        while (true) {
            if (authToken == null) {
                preloginMenu();
            } else {
                postloginMenu();
            }
        }
    }

    private void preloginMenu() {
        System.out.print("[LOGGED_OUT] >>> ");
        String input = scanner.nextLine().trim().toLowerCase();
        
        switch (input) {
            case "help" -> System.out.println("Help command not yet implemented");
            case "register" -> System.out.println("Register command not yet implemented");
            case "login" -> System.out.println("Login command not yet implemented");
            case "quit" -> {
                System.out.println("Goodbye!");
                System.exit(0);
            }
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
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