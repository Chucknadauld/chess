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
            if (result.authToken() != null) {
                System.out.println("Registration successful! You are now logged in.");
                return result.authToken();
            } else {
                System.out.println("Registration failed: " + result.message());
                return null;
            }
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
            if (result.authToken() != null) {
                System.out.println("Login successful!");
                return result.authToken();
            } else {
                System.out.println("Login failed: " + result.message());
                return null;
            }
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