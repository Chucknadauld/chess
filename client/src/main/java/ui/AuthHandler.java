package ui;

import client.ServerFacade;
import java.util.Scanner;

public class AuthHandler {
    private final ServerFacade serverFacade;
    private final Scanner scanner;

    public AuthHandler(ServerFacade serverFacade, Scanner scanner) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
    }

    public String handleRegister() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();

        try {
            ServerFacade.RegisterResult result = serverFacade.register(username, password, email);
            System.out.println("Registration successful! Welcome " + result.username() + "!");
            return result.authToken();
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
            return null;
        }
    }

    public String handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            ServerFacade.LoginResult result = serverFacade.login(username, password);
            System.out.println("Login successful! Welcome back " + result.username() + "!");
            return result.authToken();
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            return null;
        }
    }

    public void handleLogout(String authToken) {
        try {
            serverFacade.logout(authToken);
            System.out.println("Logged out successfully.");
        } catch (Exception e) {
            System.out.println("Logout failed: " + e.getMessage());
        }
    }
}