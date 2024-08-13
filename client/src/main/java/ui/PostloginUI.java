package ui;

import client.ChessClient;
import client.ClientException;
import model.GameData;
import model.GameState;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class PostloginUI {
  private final ChessClient client;
  private final Scanner scanner;
  private GameState currentGameState;

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
      System.out.print("Enter your choice (number or command): ");

      String choice = scanner.nextLine().trim().toLowerCase();
      try {
        switch (choice) {
          case "1":
          case "help":
            displayHelp();
            break;
          case "2":
          case "logout":
            logout();
            return;
          case "3":
          case "create game":
            createGame();
            break;
          case "4":
          case "list games":
            listGames();
            break;
          case "5":
          case "join game":
            joinGame();
            break;
          case "6":
          case "observe game":
            observeGame();
            break;
          default:
            System.out.println(SET_TEXT_COLOR_RED + "Invalid choice. Please try again." + RESET_TEXT_COLOR);
            break;
        }
      } catch (Exception e) {
        System.out.println(SET_TEXT_COLOR_RED + "An error occurred: " + e.getMessage() + RESET_TEXT_COLOR);
      }
    }
  }

  private void displayHelp() {
    System.out.println(ERASE_SCREEN + SET_TEXT_COLOR_GREEN + "Chess Game Help:" + RESET_TEXT_COLOR);
    System.out.println("- Use 'Help' to bring up an explanation of available options");
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
      System.out.println(SET_TEXT_COLOR_GREEN + "Game created successfully!" + RESET_TEXT_COLOR);
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
    String input = scanner.nextLine().trim();
    if (input.isEmpty()) {
      System.out.println(SET_TEXT_COLOR_RED + "Invalid input. Please enter a game number." + RESET_TEXT_COLOR);
      return;
    }
    int gameNumber;
    try {
      gameNumber = Integer.parseInt(input);
    } catch (NumberFormatException e) {
      System.out.println(SET_TEXT_COLOR_RED + "Invalid input. Please enter a valid number." + RESET_TEXT_COLOR);
      return;
    }

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
      client.joinGame(selectedGame.gameID());
      playGame(selectedGame.gameID());
    } catch (Exception e) {
      System.out.println(SET_TEXT_COLOR_RED + "Failed to join game: " + e.getMessage() + RESET_TEXT_COLOR);
    }
  }

  private void playGame(int gameId) {
    while (true) {
      displayBoard();
      System.out.println("Enter your move (e.g., e2 e4), 'resign', or 'leave':");
      String input = scanner.nextLine().trim().toLowerCase();

      if (input.equals("resign")) {
        try {
          client.resignGame(gameId);
          System.out.println(SET_TEXT_COLOR_YELLOW + "You have resigned from the game." + RESET_TEXT_COLOR);
          break;
        } catch (Exception e) {
          System.out.println(SET_TEXT_COLOR_RED + "Failed to resign: " + e.getMessage() + RESET_TEXT_COLOR);
        }
      } else if (input.equals("leave")) {
        try {
          client.leaveGame(gameId);
          System.out.println(SET_TEXT_COLOR_YELLOW + "You have left the game." + RESET_TEXT_COLOR);
          break;
        } catch (Exception e) {
          System.out.println(SET_TEXT_COLOR_RED + "Failed to leave game: " + e.getMessage() + RESET_TEXT_COLOR);
        }
      } else {
        try {
          ChessMove move = parseMove(input);
          client.makeMove(gameId, move);
        } catch (Exception e) {
          System.out.println(SET_TEXT_COLOR_RED + "Invalid move: " + e.getMessage() + RESET_TEXT_COLOR);
        }
      }
    }
  }

  private void displayBoard() {
    if (currentGameState != null) {
      ChessboardUI.displayChessboard(currentGameState);
    } else {
      System.out.println(SET_TEXT_COLOR_YELLOW + "Waiting for game state..." + RESET_TEXT_COLOR);
    }
  }

  private ChessMove parseMove(String input) {
    String[] parts = input.split("\\s+");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid move format. Use 'e2 e4' format.");
    }
    ChessPosition start = parsePosition(parts[0]);
    ChessPosition end = parsePosition(parts[1]);
    return new ChessMove(start, end, null); // Assuming no promotion for simplicity
  }

  private ChessPosition parsePosition(String pos) {
    if (pos.length() != 2) {
      throw new IllegalArgumentException("Invalid position format. Use 'e2' format.");
    }
    int col = pos.charAt(0) - 'a' + 1;
    int row = Character.getNumericValue(pos.charAt(1));
    return new ChessPosition(row, col);
  }

  public void updateGameState(GameState gameState) {
    this.currentGameState = gameState;
    displayBoard();
  }

  public void showNotification(String message) {
    System.out.println(SET_TEXT_COLOR_GREEN + "Notification: " + message + RESET_TEXT_COLOR);
  }

  public void showError(String errorMessage) {
    System.out.println(SET_TEXT_COLOR_RED + "Error: " + errorMessage + RESET_TEXT_COLOR);
  }

  private void observeGame() {
    System.out.print("Enter game number: ");
    String input = scanner.nextLine().trim();
    try {
      int gameNumber = Integer.parseInt(input);
      Collection<GameData> games = client.getServer().listGames(client.getAuthToken());
      GameData[] gamesArray = games.toArray(new GameData[0]);
      if (gameNumber <= 0 || gameNumber > gamesArray.length) {
        System.out.println(SET_TEXT_COLOR_RED + "Invalid game number. Please try again." + RESET_TEXT_COLOR);
        return;
      }
      GameData selectedGame = gamesArray[gameNumber - 1];
      System.out.println(SET_TEXT_COLOR_GREEN + "Observing game: " + selectedGame.gameName() + RESET_TEXT_COLOR);

      // Create a dummy ChessGame for display purposes
      ChessGame dummyGame = new ChessGame();
      dummyGame.getBoard().resetBoard();

      // Display the board
      ChessboardUI.displayChessboard(new GameState(selectedGame.gameID(), selectedGame.gameName(),
              selectedGame.whiteUsername(), selectedGame.blackUsername(), dummyGame));

      System.out.println("Press Enter to return to the menu...");
      scanner.nextLine();
    } catch (NumberFormatException e) {
      System.out.println(SET_TEXT_COLOR_RED + "Invalid input. Please enter a valid number." + RESET_TEXT_COLOR);
    } catch (Exception e) {
      System.out.println(SET_TEXT_COLOR_RED + "Failed to observe game: " + e.getMessage() + RESET_TEXT_COLOR);
    }
  }
}