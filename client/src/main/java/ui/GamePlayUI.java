package ui;

import client.ChessClient;
import client.ClientException;
import model.GameState;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class GamePlayUI {
  private final ChessClient client;
  private final Scanner scanner;
  private final int gameId;

  public GamePlayUI(ChessClient client, int gameId) {
    this.client = client;
    this.scanner = new Scanner(System.in);
    this.gameId = gameId;
  }


  private void makeMove(ChessMove move) {
    try {
      client.makeMove(gameId, move);
      System.out.println(SET_TEXT_COLOR_GREEN + "Move made successfully!" + RESET_TEXT_COLOR);
    } catch (ClientException e) {
      System.out.println(SET_TEXT_COLOR_RED + "Error making move: " + e.getMessage() + RESET_TEXT_COLOR);
    } catch (Exception e) {
      System.out.println(SET_TEXT_COLOR_RED + "Unexpected error: " + e.getMessage() + RESET_TEXT_COLOR);
    }
  }

  private void displayBoard(GameState gameState) {
    ChessboardUI.displayChessboard(gameState);
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
}