package ui;

import static ui.EscapeSequences.*;

public class ChessboardUI {

  public static void displayChessboard() {
    displayWhitePerspective();
    System.out.println();
    displayBlackPerspective();
  }

  private static void displayWhitePerspective() {
    System.out.println("White's Perspective:");
    displayBoard(true);
  }

  private static void displayBlackPerspective() {
    System.out.println("Black's Perspective:");
    displayBoard(false);
  }

  private static void displayBoard(boolean whiteAtBottom) {
    String[] letters = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};
    String[] pieces = {EMPTY, WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP, WHITE_QUEEN, WHITE_KING, WHITE_PAWN,
            BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_QUEEN, BLACK_KING, BLACK_PAWN};

    if (!whiteAtBottom) {
      for (int i = 0; i < letters.length / 2; i++) {
        String temp = letters[i];
        letters[i] = letters[letters.length - 1 - i];
        letters[letters.length - 1 - i] = temp;
      }
    }

    // Print column letters
    System.out.print("   ");
    for (String letter : letters) {
      System.out.print(letter);
    }
    System.out.println();

    for (int row = 0; row < 8; row++) {
      int displayRow = whiteAtBottom ? 8 - row : row + 1;
      System.out.print(displayRow + " ");

      for (int col = 0; col < 8; col++) {
        boolean isLightSquare = (row + col) % 2 == 0;
        String bgColor = isLightSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
        System.out.print(bgColor);

        // For simplicity, just print empty squares
        System.out.print(EMPTY);
      }

      System.out.print(RESET_BG_COLOR + " " + displayRow);
      System.out.println();
    }

    // Print column letters again
    System.out.print("   ");
    for (String letter : letters) {
      System.out.print(letter);
    }
    System.out.println();
  }
}