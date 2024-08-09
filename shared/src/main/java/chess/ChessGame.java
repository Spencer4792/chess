package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.io.Serializable;

public class ChessGame implements Serializable {
    private static final long serialVersionUID = 1L;
    private ChessBoard board;
    private TeamColor teamTurn;
    private ChessMove lastMove;

    public ChessGame() {
        this.board = new ChessBoard();
        this.teamTurn = TeamColor.WHITE;
        initializeBoard();
    }

    public void initializeBoard() {
        // Set up pawns
        for (int i = 1; i <= 8; i++) {
            board.addPiece(new ChessPosition(2, i), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            board.addPiece(new ChessPosition(7, i), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }

        // Set up other pieces
        board.addPiece(new ChessPosition(1, 1), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        board.addPiece(new ChessPosition(1, 8), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        board.addPiece(new ChessPosition(8, 1), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        board.addPiece(new ChessPosition(8, 8), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.ROOK));

        board.addPiece(new ChessPosition(1, 2), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        board.addPiece(new ChessPosition(1, 7), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        board.addPiece(new ChessPosition(8, 2), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        board.addPiece(new ChessPosition(8, 7), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));

        board.addPiece(new ChessPosition(1, 3), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        board.addPiece(new ChessPosition(1, 6), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        board.addPiece(new ChessPosition(8, 3), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        board.addPiece(new ChessPosition(8, 6), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.BISHOP));

        board.addPiece(new ChessPosition(1, 4), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        board.addPiece(new ChessPosition(8, 4), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.QUEEN));

        board.addPiece(new ChessPosition(1, 5), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.KING));
        board.addPiece(new ChessPosition(8, 5), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.KING));
    }

    public String debugBoardState() {
        StringBuilder state = new StringBuilder();
        for (int row = 8; row >= 1; row--) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece == null) {
                    state.append("-- ");
                } else {
                    state.append(piece.getTeamColor().toString().charAt(0))
                            .append(piece.getPieceType().toString().charAt(0))
                            .append(" ");
                }
            }
            state.append("\n");
        }
        return state.toString();
    }

    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition, lastMove);
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

        // Handle en passant
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            if (Math.abs(move.getStartPosition().getCol() - move.getEndPosition().getCol()) == 1 &&
                    board.getPiece(move.getEndPosition()) == null) {
                ChessPosition capturedPawnPosition = new ChessPosition(move.getStartPosition().getRow(), move.getEndPosition().getCol());
                board.addPiece(capturedPawnPosition, null);
            }
        }

        // Handle castling
        if (piece.getPieceType() == ChessPiece.PieceType.KING &&
                Math.abs(move.getStartPosition().getCol() - move.getEndPosition().getCol()) == 2) {
            int rookStartCol = move.getEndPosition().getCol() > move.getStartPosition().getCol() ? 8 : 1;
            int rookEndCol = move.getEndPosition().getCol() > move.getStartPosition().getCol() ? 6 : 4;
            ChessPosition rookStart = new ChessPosition(move.getStartPosition().getRow(), rookStartCol);
            ChessPosition rookEnd = new ChessPosition(move.getStartPosition().getRow(), rookEndCol);

            ChessPiece rook = board.getPiece(rookStart);
            board.addPiece(rookEnd, rook);
            board.addPiece(rookStart, null);
        }

        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);

        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }

        lastMove = move;
        piece.setHasMoved(true);

        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(board, teamColor);
    }

    public boolean isInCheck(ChessBoard board, TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(board, teamColor);
        if (kingPosition == null) {
            return false;
        }

        TeamColor oppositeColor = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        for (Map.Entry<ChessPosition, ChessPiece> entry : board.getBoard().entrySet()) {
            ChessPiece piece = entry.getValue();
            if (piece != null && piece.getTeamColor() == oppositeColor) {
                Collection<ChessMove> moves = piece.pieceMoves(board, entry.getKey(), lastMove);
                for (ChessMove move : moves) {
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        for (Map.Entry<ChessPosition, ChessPiece> entry : board.getBoard().entrySet()) {
            ChessPiece piece = entry.getValue();
            if (piece != null && piece.getTeamColor() == teamColor) {
                Collection<ChessMove> moves = validMoves(entry.getKey());
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

    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        for (Map.Entry<ChessPosition, ChessPiece> entry : board.getBoard().entrySet()) {
            ChessPiece piece = entry.getValue();
            if (piece != null && piece.getTeamColor() == teamColor) {
                Collection<ChessMove> moves = validMoves(entry.getKey());
                if (!moves.isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    private ChessPosition findKingPosition(ChessBoard board, TeamColor teamColor) {
        for (Map.Entry<ChessPosition, ChessPiece> entry : board.getBoard().entrySet()) {
            ChessPiece piece = entry.getValue();
            if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return this.board;
    }

    public ChessMove getLastMove() {
        return lastMove;
    }

    public void setLastMove(ChessMove lastMove) {
        this.lastMove = lastMove;
    }
}