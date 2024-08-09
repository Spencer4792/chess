package ui;

import client.ChessClient;
import client.ClientException;
import model.AuthData;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class PreloginUI {
  private final ChessClient client;
  private final Scanner scanner;

  public PreloginUI(ChessClient client) {
    this.client = client;
    this.scanner = new Scanner(System.in);
  }

  public void display() {
    while (true) {
      System.out.println(ERASE_SCREEN + SET_TEXT_COLOR_BLUE + "Chess Game - Prelogin Menu" + RESET_TEXT_COLOR);
      System.out.println("1. Help");
      System.out.println("2. Quit");
      System.out.println("3. Login");
      System.out.println("4. Register");
      System.out.print("Enter your choice (number or command): ");

      String choice = scanner.nextLine().trim().toLowerCase();
      switch (choice) {
        case "1":
        case "help":
          displayHelp();
          break;
        case "2":
        case "quit":
          System.out.println("Goodbye!");
          System.exit(0);
          break;
        case "3":
        case "login":
          login();
          break;
        case "4":
        case "register":
          register();
          break;
        default:
          System.out.println(SET_TEXT_COLOR_RED + "Invalid choice. Please try again." + RESET_TEXT_COLOR);
          break;
      }
    }
  }

  private void displayHelp() {
    System.out.println(ERASE_SCREEN + SET_TEXT_COLOR_GREEN + "Chess Game Help:" + RESET_TEXT_COLOR);
    System.out.println("- Use 'Help' to bring up an explanation of available options");
    System.out.println("- Use 'Login' to access your account");
    System.out.println("- Use 'Register' to create a new account");
    System.out.println("- Use 'Quit' to exit the program");
    System.out.println("\nPress Enter to continue...");
    scanner.nextLine();
  }

  private void login() {
    System.out.print("Enter username: ");
    String username = scanner.nextLine();
    System.out.print("Enter password: ");
    String password = scanner.nextLine();

    try {
      AuthData authData = client.getServer().login(username, password);
      client.setAuthToken(authData.authToken());
      System.out.println(SET_TEXT_COLOR_GREEN + "Login successful!" + RESET_TEXT_COLOR);
      client.switchToPostlogin();
    } catch (ClientException e) {
      System.out.println(SET_TEXT_COLOR_RED + "Login failed: " + e.getMessage() + RESET_TEXT_COLOR);
    }
  }

  private void register() {
    System.out.print("Enter username: ");
    String username = scanner.nextLine();
    System.out.print("Enter password: ");
    String password = scanner.nextLine();
    System.out.print("Enter email: ");
    String email = scanner.nextLine();

    try {
      AuthData authData = client.getServer().register(username, password, email);
      client.setAuthToken(authData.authToken());
      System.out.println(SET_TEXT_COLOR_GREEN + "Registration successful!" + RESET_TEXT_COLOR);
      client.switchToPostlogin();
    } catch (ClientException e) {
      System.out.println(SET_TEXT_COLOR_RED + "Registration failed: " + e.getMessage() + RESET_TEXT_COLOR);
    }
  }
}