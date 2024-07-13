package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor teamTurn;
    private ChessMove lastMove;

    public ChessGame() {
        this.board = new ChessBoard();
        this.teamTurn = TeamColor.WHITE; // White starts the game
        this.board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : possibleMoves) {
            ChessBoard clonedBoard = board.clone();
            clonedBoard.addPiece(move.getEndPosition(), piece);
            clonedBoard.addPiece(move.getStartPosition(), null);
            if (!isInCheck(clonedBoard, piece.getTeamColor())) {
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("No piece at start position.");
        }
        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Not the correct team's turn.");
        }
        if (!validMoves(move.getStartPosition()).contains(move)) {
            throw new InvalidMoveException("Invalid move.");
        }

        // Make the move
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);

        // Handle promotion if needed
        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }

        // Handle en passant
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            if (move.getEndPosition().getRow() == (piece.getTeamColor() == TeamColor.WHITE ? 5 : 4)) {
                ChessPosition enPassantPosition = new ChessPosition(move.getStartPosition().getRow(), move.getEndPosition().getCol());
                ChessPiece enPassantPawn = board.getPiece(enPassantPosition);
                if (enPassantPawn != null && enPassantPawn.getPieceType() == ChessPiece.PieceType.PAWN && enPassantPawn.getTeamColor() != piece.getTeamColor() &&
                        lastMove != null && lastMove.getStartPosition().getRow() == (piece.getTeamColor() == TeamColor.WHITE ? 7 : 2) && lastMove.getEndPosition().getRow() == (piece.getTeamColor() == TeamColor.WHITE ? 5 : 4) &&
                        lastMove.getEndPosition().getCol() == enPassantPosition.getCol()) {
                    board.addPiece(enPassantPosition, null);
                }
            }
        }

        lastMove = move;

        // Check for check
        if (isInCheck(piece.getTeamColor())) {
            // Undo the move if it puts the player's king in check
            board.addPiece(move.getStartPosition(), piece);
            board.addPiece(move.getEndPosition(), null);
            throw new InvalidMoveException("Move puts king in check.");
        }

        // Switch turn
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(board, teamColor);
    }

    private boolean isInCheck(ChessBoard board, TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(board, teamColor);
        if (kingPosition == null) return false; // King not found
        for (ChessPiece piece : board.getPieces()) {
            if (piece.getTeamColor() != teamColor) {
                for (ChessMove move : piece.pieceMoves(board, board.getPosition(piece))) {
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        for (ChessPiece piece : board.getPieces()) {
            if (piece.getTeamColor() == teamColor) {
                Collection<ChessMove> moves = piece.pieceMoves(board, board.getPosition(piece));
                for (ChessMove move : moves) {
                    ChessBoard clonedBoard = board.clone();
                    clonedBoard.addPiece(move.getEndPosition(), piece);
                    clonedBoard.addPiece(move.getStartPosition(), null);
                    if (!isInCheck(clonedBoard, teamColor)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        for (ChessPiece piece : board.getPieces()) {
            if (piece.getTeamColor() == teamColor) {
                for (ChessMove move : piece.pieceMoves(board, board.getPosition(piece))) {
                    ChessBoard clonedBoard = board.clone();
                    clonedBoard.addPiece(move.getEndPosition(), piece);
                    clonedBoard.addPiece(move.getStartPosition(), null);
                    if (!isInCheck(clonedBoard, teamColor)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    private ChessPosition findKingPosition(ChessBoard board, TeamColor teamColor) {
        for (Map.Entry<ChessPosition, ChessPiece> entry : board.getBoard().entrySet()) {
            ChessPiece piece = entry.getValue();
            if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING &&
                    piece.getTeamColor() == teamColor) {
                return entry.getKey();
            }
        }
        return null;
    }

    private ChessPosition findKingPosition(TeamColor teamColor) {
        return findKingPosition(this.board, teamColor);
    }
}
