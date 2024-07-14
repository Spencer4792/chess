package chess;

import java.util.ArrayList;
import java.util.Collection;

public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;
    private boolean hasMoved;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
        this.hasMoved = false;
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

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return pieceMoves(board, myPosition, null);
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, ChessMove lastMove) {
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
                addPawnMoves(board, myPosition, moves, lastMove);
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

        // Castling
        if (!hasMoved) {
            // Kingside castling
            ChessPosition kingsideRookPosition = new ChessPosition(myPosition.getRow(), 8);
            if (canCastle(board, myPosition, kingsideRookPosition)) {
                moves.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow(), myPosition.getCol() + 2), null));
            }

            // Queenside castling
            ChessPosition queensideRookPosition = new ChessPosition(myPosition.getRow(), 1);
            if (canCastle(board, myPosition, queensideRookPosition)) {
                moves.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow(), myPosition.getCol() - 2), null));
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

    private void addPawnMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves, ChessMove lastMove) {
        int direction = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promotionRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 8 : 1;

        // Move forward one step
        ChessPosition oneStep = new ChessPosition(myPosition.getRow() + direction, myPosition.getCol());
        if (isValidPosition(oneStep) && board.getPiece(oneStep) == null) {
            addPawnMove(myPosition, oneStep, promotionRow, moves);

            // Move forward two steps from starting position
            if (myPosition.getRow() == startRow) {
                ChessPosition twoSteps = new ChessPosition(myPosition.getRow() + 2 * direction, myPosition.getCol());
                if (board.getPiece(twoSteps) == null) {
                    moves.add(new ChessMove(myPosition, twoSteps, null));
                }
            }
        }

        // Capture diagonally
        for (int colOffset : new int[]{-1, 1}) {
            ChessPosition capturePos = new ChessPosition(myPosition.getRow() + direction, myPosition.getCol() + colOffset);
            if (isValidPosition(capturePos)) {
                ChessPiece pieceToCapture = board.getPiece(capturePos);
                if (pieceToCapture != null && pieceToCapture.getTeamColor() != pieceColor) {
                    addPawnMove(myPosition, capturePos, promotionRow, moves);
                }
            }
        }

        // En passant
        if ((pieceColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 5) ||
                (pieceColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 4)) {
            if (lastMove != null && lastMove.getEndPosition().getRow() == myPosition.getRow() &&
                    Math.abs(lastMove.getEndPosition().getCol() - myPosition.getCol()) == 1) {
                ChessPiece lastMovedPiece = board.getPiece(lastMove.getEndPosition());
                if (lastMovedPiece != null && lastMovedPiece.getPieceType() == PieceType.PAWN &&
                        lastMovedPiece.getTeamColor() != pieceColor &&
                        Math.abs(lastMove.getStartPosition().getRow() - lastMove.getEndPosition().getRow()) == 2) {
                    ChessPosition capturePosition = new ChessPosition(
                            myPosition.getRow() + direction,
                            lastMove.getEndPosition().getCol()
                    );
                    moves.add(new ChessMove(myPosition, capturePosition, null));
                }
            }
        }
    }

    private void addPawnMove(ChessPosition from, ChessPosition to, int promotionRow, Collection<ChessMove> moves) {
        if (to.getRow() == promotionRow) {
            moves.add(new ChessMove(from, to, PieceType.QUEEN));
            moves.add(new ChessMove(from, to, PieceType.ROOK));
            moves.add(new ChessMove(from, to, PieceType.BISHOP));
            moves.add(new ChessMove(from, to, PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(from, to, null));
        }
    }

    private void addMovesInDirections(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves, int[][] directions) {
        for (int[] direction : directions) {
            for (int step = 1; step <= 7; step++) {
                ChessPosition newPos = new ChessPosition(myPosition.getRow() + step * direction[0], myPosition.getCol() + step * direction[1]);
                if (!isValidPosition(newPos)) {
                    break;
                }
                ChessPiece pieceAtNewPos = board.getPiece(newPos);
                if (pieceAtNewPos == null) {
                    moves.add(new ChessMove(myPosition, newPos, null));
                } else {
                    if (pieceAtNewPos.getTeamColor() != pieceColor) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    }
                    break;
                }
            }
        }
    }

    private boolean canCastle(ChessBoard board, ChessPosition kingPosition, ChessPosition rookPosition) {
        ChessPiece rook = board.getPiece(rookPosition);
        if (rook == null || rook.getPieceType() != PieceType.ROOK || rook.hasMoved()) {
            return false;
        }

        int start = Math.min(kingPosition.getCol(), rookPosition.getCol());
        int end = Math.max(kingPosition.getCol(), rookPosition.getCol());
        for (int col = start + 1; col < end; col++) {
            ChessPosition position = new ChessPosition(kingPosition.getRow(), col);
            if (board.getPiece(position) != null) {
                return false;
            }
        }

        // Check if the king is in check or passes through check
        ChessGame.TeamColor oppositeColor = (pieceColor == ChessGame.TeamColor.WHITE) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        int direction = kingPosition.getCol() < rookPosition.getCol() ? 1 : -1;
        for (int col = kingPosition.getCol(); col != rookPosition.getCol(); col += direction) {
            ChessPosition position = new ChessPosition(kingPosition.getRow(), col);
            if (isSquareUnderAttack(board, position, oppositeColor)) {
                return false;
            }
        }

        return true;
    }

    private boolean isSquareUnderAttack(ChessBoard board, ChessPosition position, ChessGame.TeamColor attackingColor) {
        for (ChessPosition pos : board.getBoard().keySet()) {
            ChessPiece piece = board.getPiece(pos);
            if (piece != null && piece.getTeamColor() == attackingColor && piece.getPieceType() != PieceType.KING) {
                Collection<ChessMove> moves = piece.pieceMoves(board, pos, null);
                for (ChessMove move : moves) {
                    if (move.getEndPosition().equals(position)) {
                        return true;
                    }
                }
            }
        }
        return false;
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