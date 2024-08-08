package chess;

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;
import java.io.IOException;

public class ChessBoard implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<ChessPosition, ChessPiece> board;

    public ChessBoard() {
        board = new HashMap<>();
    }

    public void addPiece(ChessPosition position, ChessPiece piece) {
        if (piece == null) {
            board.remove(position);
        } else {
            board.put(position, piece);
        }
    }

    public ChessPiece getPiece(ChessPosition position) {
        return board.get(position);
    }

    public void resetBoard() {
        board.clear();
        // Add pawns
        for (int col = 1; col <= 8; col++) {
            board.put(new ChessPosition(2, col), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            board.put(new ChessPosition(7, col), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
        // Add other pieces
        setPieceRow(1, ChessGame.TeamColor.WHITE);
        setPieceRow(8, ChessGame.TeamColor.BLACK);
    }

    private void setPieceRow(int row, ChessGame.TeamColor color) {
        board.put(new ChessPosition(row, 1), new ChessPiece(color, ChessPiece.PieceType.ROOK));
        board.put(new ChessPosition(row, 2), new ChessPiece(color, ChessPiece.PieceType.KNIGHT));
        board.put(new ChessPosition(row, 3), new ChessPiece(color, ChessPiece.PieceType.BISHOP));
        board.put(new ChessPosition(row, 4), new ChessPiece(color, ChessPiece.PieceType.QUEEN));
        board.put(new ChessPosition(row, 5), new ChessPiece(color, ChessPiece.PieceType.KING));
        board.put(new ChessPosition(row, 6), new ChessPiece(color, ChessPiece.PieceType.BISHOP));
        board.put(new ChessPosition(row, 7), new ChessPiece(color, ChessPiece.PieceType.KNIGHT));
        board.put(new ChessPosition(row, 8), new ChessPiece(color, ChessPiece.PieceType.ROOK));
    }

    public Map<ChessPosition, ChessPiece> getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessBoard that = (ChessBoard) o;
        return board.equals(that.board);
    }

    @Override
    public int hashCode() {
        return board.hashCode();
    }

    @Override
    public String toString() {
        return "ChessBoard{" +
                "board=" + board +
                '}';
    }

    @Override
    protected ChessBoard clone() {
        ChessBoard clonedBoard = new ChessBoard();
        for (Map.Entry<ChessPosition, ChessPiece> entry : this.board.entrySet()) {
            clonedBoard.addPiece(entry.getKey(), entry.getValue());
        }
        return clonedBoard;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}