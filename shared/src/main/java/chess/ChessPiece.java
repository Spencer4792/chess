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
        for (int[] direction : directions) {
            ChessPosition newPos = new ChessPosition(myPosition.getRow() + direction[0], myPosition.getCol() + direction[1]);
            if (isValidPosition(newPos) && canMoveTo(board, myPosition, newPos)) {
                moves.add(new ChessMove(myPosition, newPos, null));
            }
        }
    }

    private void addQueenMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int[][] directions = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
        };
        addMovesInDirections(board, myPosition, moves, directions);
    }

    private void addBishopMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int[][] directions = {
                {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
        };
        addMovesInDirections(board, myPosition, moves, directions);
    }

    private void addKnightMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int[][] jumps = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };
        for (int[] jump : jumps) {
            ChessPosition newPos = new ChessPosition(myPosition.getRow() + jump[0], myPosition.getCol() + jump[1]);
            if (isValidPosition(newPos) && canMoveTo(board, myPosition, newPos)) {
                moves.add(new ChessMove(myPosition, newPos, null));
            }
        }
    }

    private void addRookMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int[][] directions = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };
        addMovesInDirections(board, myPosition, moves, directions);
    }

    private void addPawnMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int direction = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int promotionRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 8 : 1;

        // Move forward one step
        ChessPosition oneStep = new ChessPosition(myPosition.getRow() + direction, myPosition.getCol());
        if (isValidPosition(oneStep) && board.getPiece(oneStep) == null) {
            if (oneStep.getRow() == promotionRow) {
                addPromotionMoves(myPosition, oneStep, moves);
            } else {
                moves.add(new ChessMove(myPosition, oneStep, null));
            }
        }

        // Move forward two steps from starting position
        if ((pieceColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2) ||
                (pieceColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7)) {
            ChessPosition twoSteps = new ChessPosition(myPosition.getRow() + 2 * direction, myPosition.getCol());
            if (board.getPiece(oneStep) == null && board.getPiece(twoSteps) == null) {
                moves.add(new ChessMove(myPosition, twoSteps, null));
            }
        }

        // Capture diagonally
        int[][] captures = {
                {direction, 1}, {direction, -1}
        };
        for (int[] capture : captures) {
            ChessPosition capturePos = new ChessPosition(myPosition.getRow() + capture[0], myPosition.getCol() + capture[1]);
            if (isValidPosition(capturePos) && board.getPiece(capturePos) != null &&
                    board.getPiece(capturePos).getTeamColor() != pieceColor) {
                if (capturePos.getRow() == promotionRow) {
                    addPromotionMoves(myPosition, capturePos, moves);
                } else {
                    moves.add(new ChessMove(myPosition, capturePos, null));
                }
            }
        }
    }

    private void addPromotionMoves(ChessPosition start, ChessPosition end, Collection<ChessMove> moves) {
        moves.add(new ChessMove(start, end, PieceType.QUEEN));
        moves.add(new ChessMove(start, end, PieceType.ROOK));
        moves.add(new ChessMove(start, end, PieceType.BISHOP));
        moves.add(new ChessMove(start, end, PieceType.KNIGHT));
    }

    private void addMovesInDirections(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves, int[][] directions) {
        for (int[] direction : directions) {
            for (int step=1; step <= 8; step++) {
                ChessPosition newPos = new ChessPosition(myPosition.getRow() + step * direction[0], myPosition.getCol() + step * direction[1]);
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
                position.getCol() >= 1 && position.getCol() <= 8;
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
