package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameState;

import static ui.EscapeSequences.*;

public class ChessboardUI {

  public static void displayChessboard(GameState gameState) {
    System.out.println("Game: " + gameState.getGameName());
    System.out.println("White: " + gameState.getWhiteUsername());
    System.out.println("Black: " + gameState.getBlackUsername());
    System.out.println();

    displayWhitePerspective(gameState.getGame());
    System.out.println();
    displayBlackPerspective(gameState.getGame());
  }

  private static void displayWhitePerspective(ChessGame game) {
    System.out.println("White's Perspective:");
    displayBoard(game, true);
  }

  private static void displayBlackPerspective(ChessGame game) {
    System.out.println("Black's Perspective:");
    displayBoard(game, false);
  }

  private static void displayBoard(ChessGame game, boolean whiteAtBottom) {
    String[] letters = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};
    if (!whiteAtBottom) {
      for (int i = 0; i < letters.length / 2; i++) {
        String temp = letters[i];
        letters[i] = letters[letters.length - 1 - i];
        letters[letters.length - 1 - i] = temp;
      }
    }

    // Print column letters
    System.out.print("    ");
    for (String letter : letters) {
      System.out.print(letter);
    }
    System.out.println();

    for (int row = 0; row < 8; row++) {
      int displayRow = whiteAtBottom ? 8 - row : row + 1;
      System.out.printf("%d | ", displayRow);

      for (int col = 0; col < 8; col++) {
        int actualRow = whiteAtBottom ? 8 - row : row + 1;
        int actualCol = whiteAtBottom ? col + 1 : 8 - col;
        ChessPosition position = new ChessPosition(actualRow, actualCol);
        ChessPiece piece = game.getBoard().getPiece(position);

        boolean isLightSquare = (row + col) % 2 == 0;
        String bgColor = isLightSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
        System.out.print(bgColor);

        if (piece == null) {
          System.out.print(EMPTY);
        } else {
          System.out.print(getPieceSymbol(piece));
        }
      }

      System.out.print(RESET_BG_COLOR + " | " + displayRow);
      System.out.println();
    }

    // Print column letters again
    System.out.print("    ");
    for (String letter : letters) {
      System.out.print(letter);
    }
    System.out.println();
  }

  private static String getPieceSymbol(ChessPiece piece) {
    String color = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? SET_TEXT_COLOR_BLUE : SET_TEXT_COLOR_RED;
    String symbol = switch (piece.getPieceType()) {
      case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
      case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
      case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
      case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
      case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
      case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
    };
    return color + symbol + RESET_TEXT_COLOR;
  }
}