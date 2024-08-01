package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;

public class ChessGame {
    private ChessBoard board;
    private TeamColor teamTurn;
    private ChessMove lastMove;

    public ChessGame() {
        this.board = new ChessBoard();
        this.teamTurn = TeamColor.WHITE;
        this.board.resetBoard();
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
        return board;
    }

    public ChessMove getLastMove() {
        return lastMove;
    }

    public void setLastMove(ChessMove lastMove) {
        this.lastMove = lastMove;
    }
}