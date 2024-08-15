package ui;

import chess.ChessMove;
import chess.ChessPosition;

public class UIUtils {
  public static ChessMove parseMove(String input) {
    String[] parts = input.split("\\s+");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid move format. Use 'e2 e4' format.");
    }
    ChessPosition start = parsePosition(parts[0]);
    ChessPosition end = parsePosition(parts[1]);
    return new ChessMove(start, end, null); // Assuming no promotion for simplicity
  }

  public static ChessPosition parsePosition(String pos) {
    if (pos.length() != 2) {
      throw new IllegalArgumentException("Invalid position format. Use 'e2' format.");
    }
    int col = pos.charAt(0) - 'a' + 1;
    int row = Character.getNumericValue(pos.charAt(1));
    return new ChessPosition(row, col);
  }
}