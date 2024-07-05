package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    public PieceType getPieceType() {
        return type;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        switch (type) {
            case KING:
                addKingMoves(board, myPosition, moves);
                break;
            case QUEEN:
                addQueenMoves(board, myPosition, moves);
                break;
            case BISHOP:
                addBishopMoves(board, myPosition, moves);
                break;
            case KNIGHT:
                addKnightMoves(board, myPosition, moves);
                break;
            case ROOK:
                addRookMoves(board, myPosition, moves);
                break;
            case PAWN:
                addPawnMoves(board, myPosition, moves);
                break;
        }
        return moves;
    }

    private void addKingMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int[][] directions = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
        };
        addMovesInDirections(board, myPosition, moves, directions, 1);
    }

    private void addQueenMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int[][] directions = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
        };
        addMovesInDirections(board, myPosition, moves, directions, 8);
    }

    private void addBishopMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int[][] directions = {
                {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
        };
        addMovesInDirections(board, myPosition, moves, directions, 8);
    }

    private void addKnightMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int[][] jumps = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };
        for (int[] jump : jumps) {
            ChessPosition newPos = new ChessPosition(myPosition.getRow() + jump[0], myPosition.getColumn() + jump[1]);
            if (isValidPosition(newPos) && canMoveTo(board, myPosition, newPos)) {
                moves.add(new ChessMove(myPosition, newPos, null));
            }
        }
    }

    private void addRookMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int[][] directions = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };
        addMovesInDirections(board, myPosition, moves, directions, 8);
    }

    private void addPawnMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int direction = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        ChessPosition oneStep = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
        if (isValidPosition(oneStep) && board.getPiece(oneStep) == null) {
            moves.add(new ChessMove(myPosition, oneStep, null));
            if ((pieceColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2) ||
                    (pieceColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7)) {
                ChessPosition twoSteps = new ChessPosition(myPosition.getRow() + 2 * direction, myPosition.getColumn());
                if (board.getPiece(twoSteps) == null) {
                    moves.add(new ChessMove(myPosition, twoSteps, null));
                }
            }
        }
        // Diagonal captures
        int[][] captures = {
                {direction, 1}, {direction, -1}
        };
        for (int[] capture : captures) {
            ChessPosition capturePos = new ChessPosition(myPosition.getRow() + capture[0], myPosition.getColumn() + capture[1]);
            if (isValidPosition(capturePos) && board.getPiece(capturePos) != null &&
                    board.getPiece(capturePos).getTeamColor() != pieceColor) {
                moves.add(new ChessMove(myPosition, capturePos, null));
            }
        }
    }

    private void addMovesInDirections(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves, int[][] directions, int maxSteps) {
        for (int[] direction : directions) {
            for (int step = 1; step <= maxSteps; step++) {
                ChessPosition newPos = new ChessPosition(myPosition.getRow() + step * direction[0], myPosition.getColumn() + step * direction[1]);
                if (!isValidPosition(newPos) || !canMoveTo(board, myPosition, newPos)) {
                    break;
                }
                moves.add(new ChessMove(myPosition, newPos, null));
                if (board.getPiece(newPos) != null) {
                    break;
                }
            }
        }
    }

    private boolean isValidPosition(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8 &&
                position.getColumn() >= 1 && position.getColumn() <= 8;
    }

    private boolean canMoveTo(ChessBoard board, ChessPosition from, ChessPosition to) {
        ChessPiece pieceAtDestination = board.getPiece(to);
        return pieceAtDestination == null || pieceAtDestination.getTeamColor() != this.pieceColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return 31 * pieceColor.hashCode() + type.hashCode();
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
    }
}
