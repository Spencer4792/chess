package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor currentTurn;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        currentTurn = TeamColor.WHITE;
    }

    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null || piece.getTeamColor() != currentTurn) {
            return null;
        }
        return piece.pieceMoves(board, startPosition);
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null || piece.getTeamColor() != currentTurn || !piece.pieceMoves(board, move.getStartPosition()).contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);
        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public boolean isInCheck(TeamColor teamColor) {
        // Implement check logic
        return false;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        // Implement checkmate logic
        return false;
    }

    public boolean isInStalemate(TeamColor teamColor) {
        // Implement stalemate logic
        return false;
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return board;
    }
}
