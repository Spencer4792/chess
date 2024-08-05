package ui;

import client.ChessClient;
import client.ClientException;
import model.GameData;
import model.GameState;

import java.util.Collection;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class PostloginUI {
  private final ChessClient client;
  private final Scanner scanner;

  public PostloginUI(ChessClient client) {
    this.client = client;
    this.scanner = new Scanner(System.in);
  }

  public void display() {
    while (true) {
      System.out.println(ERASE_SCREEN + SET_TEXT_COLOR_BLUE + "Chess Game - Postlogin Menu" + RESET_TEXT_COLOR);
      System.out.println("1. Help");
      System.out.println("2. Logout");
      System.out.println("3. Create Game");
      System.out.println("4. List Games");
      System.out.println("5. Join Game");
      System.out.println("6. Observe Game");
      System.out.print("Enter your choice: ");

      String choice = scanner.nextLine().trim();
      switch (choice) {
        case "1" -> displayHelp();
        case "2" -> logout();
        case "3" -> createGame();
        case "4" -> listGames();
        case "5" -> joinGame();
        case "6" -> observeGame();
        default -> System.out.println(SET_TEXT_COLOR_RED + "Invalid choice. Please try again." + RESET_TEXT_COLOR);
      }
    }
  }

  private void displayHelp() {
    System.out.println(ERASE_SCREEN + SET_TEXT_COLOR_GREEN + "Chess Game Help:" + RESET_TEXT_COLOR);
    System.out.println("- Use 'Create Game' to start a new game");
    System.out.println("- Use 'List Games' to see available games");
    System.out.println("- Use 'Join Game' to participate in a game");
    System.out.println("- Use 'Observe Game' to watch a game");
    System.out.println("- Use 'Logout' to exit to the main menu");
    System.out.println("\nPress Enter to continue...");
    scanner.nextLine();
  }

  private void logout() {
    try {
      client.getServer().logout(client.getAuthToken());
      System.out.println(SET_TEXT_COLOR_GREEN + "Logout successful!" + RESET_TEXT_COLOR);
      client.logout();
    } catch (ClientException e) {
      System.out.println(SET_TEXT_COLOR_RED + "Logout failed: " + e.getMessage() + RESET_TEXT_COLOR);
    }
  }

  private void createGame() {
    System.out.print("Enter game name: ");
    String gameName = scanner.nextLine();

    try {
      int gameId = client.getServer().createGame(client.getAuthToken(), gameName);
      System.out.println(SET_TEXT_COLOR_GREEN + "Game created successfully! Game ID: " + gameId + RESET_TEXT_COLOR);
    } catch (ClientException e) {
      System.out.println(SET_TEXT_COLOR_RED + "Failed to create game: " + e.getMessage() + RESET_TEXT_COLOR);
    }
  }

  private void listGames() {
    try {
      Collection<GameData> games = client.getServer().listGames(client.getAuthToken());
      System.out.println(ERASE_SCREEN + SET_TEXT_COLOR_GREEN + "Available Games:" + RESET_TEXT_COLOR);
      int i = 1;
      for (GameData game : games) {
        System.out.printf("%d. %s (White: %s, Black: %s)%n",
                i++, game.gameName(), game.whiteUsername(), game.blackUsername());
      }
      System.out.println("\nPress Enter to continue...");
      scanner.nextLine();
    } catch (ClientException e) {
      System.out.println(SET_TEXT_COLOR_RED + "Failed to list games: " + e.getMessage() + RESET_TEXT_COLOR);
    }
  }

  private void joinGame() {
    System.out.print("Enter game number: ");
    int gameNumber = Integer.parseInt(scanner.nextLine());
    System.out.print("Enter color (WHITE/BLACK): ");
    String color = scanner.nextLine().toUpperCase();

    try {
      Collection<GameData> games = client.getServer().listGames(client.getAuthToken());
      GameData[] gamesArray = games.toArray(new GameData[0]);
      if (gameNumber <= 0 || gameNumber > gamesArray.length) {
        throw new IllegalArgumentException("Invalid game number");
      }
      GameData selectedGame = gamesArray[gameNumber - 1];
      client.getServer().joinGame(client.getAuthToken(), selectedGame.gameID(), color);
      System.out.println(SET_TEXT_COLOR_GREEN + "Joined game successfully!" + RESET_TEXT_COLOR);
      GameState gameState = new GameState(selectedGame.gameID(), selectedGame.gameName(),
              selectedGame.whiteUsername(), selectedGame.blackUsername(), selectedGame.game());
      ChessboardUI.displayChessboard(gameState);
    } catch (Exception e) {
      System.out.println(SET_TEXT_COLOR_RED + "Failed to join game: " + e.getMessage() + RESET_TEXT_COLOR);
    }
  }

  private void observeGame() {
    System.out.print("Enter game number: ");
    int gameNumber = Integer.parseInt(scanner.nextLine());

    try {
      Collection<GameData> games = client.getServer().listGames(client.getAuthToken());
      GameData[] gamesArray = games.toArray(new GameData[0]);
      if (gameNumber <= 0 || gameNumber > gamesArray.length) {
        throw new IllegalArgumentException("Invalid game number");
      }
      GameData selectedGame = gamesArray[gameNumber - 1];
      client.getServer().joinGame(client.getAuthToken(), selectedGame.gameID(), null);
      System.out.println(SET_TEXT_COLOR_GREEN + "Observing game successfully!" + RESET_TEXT_COLOR);
      GameState gameState = new GameState(selectedGame.gameID(), selectedGame.gameName(),
              selectedGame.whiteUsername(), selectedGame.blackUsername(), selectedGame.game());
      ChessboardUI.displayChessboard(gameState);
    } catch (Exception e) {
      System.out.println(SET_TEXT_COLOR_RED + "Failed to observe game: " + e.getMessage() + RESET_TEXT_COLOR);
    }
  }
}