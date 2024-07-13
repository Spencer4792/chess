package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class ChessGame {
    private ChessBoard board;
    private TeamColor teamTurn;
    private ChessMove lastMove;

    public ChessGame() {
        this.board = new ChessBoard();
        this.teamTurn = TeamColor.WHITE; // White starts the game
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

        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);

        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }

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

        if (isInCheck(piece.getTeamColor())) {
            board.addPiece(move.getStartPosition(), piece);
            board.addPiece(move.getEndPosition(), null);
            throw new InvalidMoveException("Move puts king in check.");
        }

        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(board, teamColor);
    }

    public boolean isInCheck(ChessBoard board, TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(board, teamColor);
        if (kingPosition == null) {
            System.out.println("King not found for team: " + teamColor);
            return false;
        }

        for (ChessPiece piece : board.getPieces()) {
            if (piece.getTeamColor() != teamColor) {
                Collection<ChessMove> moves = piece.pieceMoves(board, board.getPosition(piece));
                for (ChessMove move : moves) {
                    if (move.getEndPosition().equals(kingPosition)) {
                        System.out.println("Check detected by " + piece.getPieceType() + " at " + board.getPosition(piece) + " to " + kingPosition);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(board, teamColor)) {
            return false; // Cannot be in checkmate if not in check.
        }

        for (ChessPiece piece : board.getPieces()) {
            if (piece.getTeamColor() == teamColor) {
                ChessPosition piecePosition = board.getPosition(piece);
                Collection<ChessMove> possibleMoves = piece.pieceMoves(board, piecePosition);

                for (ChessMove move : possibleMoves) {
                    ChessBoard clonedBoard = board.clone();
                    executeMove(clonedBoard, move, piece);

                    if (!isInCheck(clonedBoard, teamColor)) {
                        System.out.println("Escaping move found: " + move + " by " + piece.getPieceType());
                        return false; // A valid move found that does not result in check
                    }
                }
            }
        }

        System.out.println("No valid moves available; " + teamColor + " is in checkmate.");
        return true;
    }

    private void executeMove(ChessBoard board, ChessMove move, ChessPiece piece) {
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);
        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }
    }



    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
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

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return board;
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

    private void updateKingPosition(ChessBoard board, ChessPosition newPosition, TeamColor teamColor) {
        ChessPosition oldKingPosition = findKingPosition(board, teamColor);
        if (oldKingPosition != null) {
            board.addPiece(oldKingPosition, null);
        }
        board.addPiece(newPosition, new ChessPiece(teamColor, ChessPiece.PieceType.KING));
    }
}
