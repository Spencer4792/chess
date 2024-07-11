package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor currentTurn;
    private boolean whiteKingMoved;
    private boolean blackKingMoved;
    private boolean whiteRookMovedLeft;
    private boolean whiteRookMovedRight;
    private boolean blackRookMovedLeft;
    private boolean blackRookMovedRight;
    private ChessMove lastMove;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        currentTurn = TeamColor.WHITE;
        whiteKingMoved = false;
        blackKingMoved = false;
        whiteRookMovedLeft = false;
        whiteRookMovedRight = false;
        blackRookMovedLeft = false;
        blackRookMovedRight = false;
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
        return piece.pieceMoves(board, startPosition).stream()
                .filter(move -> {
                    try {
                        ChessGame testGame = this.clone();
                        testGame.makeMove(move);
                        return !testGame.isInCheck(currentTurn);
                    } catch (InvalidMoveException e) {
                        return false;
                    }
                }).collect(Collectors.toList());
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null || piece.getTeamColor() != currentTurn || !validMoves(move.getStartPosition()).contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }

        // Castling logic
        if (piece.getPieceType() == ChessPiece.PieceType.KING && Math.abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn()) == 2) {
            performCastling(move);
        } else if (piece.getPieceType() == ChessPiece.PieceType.PAWN && isEnPassantMove(move)) {
            performEnPassant(move);
        } else {
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);
            if (move.getPromotionPiece() != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                board.addPiece(move.getEndPosition(), new ChessPiece(currentTurn, move.getPromotionPiece()));
            }
        }

        updateFlagsAfterMove(move, piece);

        lastMove = move;
        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    private boolean isEnPassantMove(ChessMove move) {
        if (lastMove == null) {
            return false;
        }
        ChessPiece lastPiece = board.getPiece(lastMove.getEndPosition());
        if (lastPiece == null || lastPiece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return false;
        }
        if (Math.abs(lastMove.getEndPosition().getRow() - lastMove.getStartPosition().getRow()) != 2) {
            return false;
        }
        return move.getEndPosition().equals(new ChessPosition(lastMove.getEndPosition().getRow() + (currentTurn == TeamColor.WHITE ? -1 : 1), lastMove.getEndPosition().getColumn()));
    }

    private void performCastling(ChessMove move) throws InvalidMoveException {
        int startColumn = move.getStartPosition().getColumn();
        int endColumn = move.getEndPosition().getColumn();
        ChessPiece king = board.getPiece(move.getStartPosition());

        if (startColumn > endColumn) {
            // Castling left
            if (currentTurn == TeamColor.WHITE && !whiteKingMoved && !whiteRookMovedLeft &&
                    isEmptyBetween(new ChessPosition(1, 1), new ChessPosition(1, 5))) {
                board.addPiece(new ChessPosition(1, 3), king);
                board.addPiece(new ChessPosition(1, 1), null);
                board.addPiece(new ChessPosition(1, 4), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.ROOK));
                board.addPiece(new ChessPosition(1, 5), null);
            } else if (currentTurn == TeamColor.BLACK && !blackKingMoved && !blackRookMovedLeft &&
                    isEmptyBetween(new ChessPosition(8, 1), new ChessPosition(8, 5))) {
                board.addPiece(new ChessPosition(8, 3), king);
                board.addPiece(new ChessPosition(8, 1), null);
                board.addPiece(new ChessPosition(8, 4), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.ROOK));
                board.addPiece(new ChessPosition(8, 5), null);
            } else {
                throw new InvalidMoveException("Invalid castling move");
            }
        } else {
            // Castling right
            if (currentTurn == TeamColor.WHITE && !whiteKingMoved && !whiteRookMovedRight &&
                    isEmptyBetween(new ChessPosition(1, 5), new ChessPosition(1, 8))) {
                board.addPiece(new ChessPosition(1, 7), king);
                board.addPiece(new ChessPosition(1, 5), null);
                board.addPiece(new ChessPosition(1, 6), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.ROOK));
                board.addPiece(new ChessPosition(1, 8), null);
            } else if (currentTurn == TeamColor.BLACK && !blackKingMoved && !blackRookMovedRight &&
                    isEmptyBetween(new ChessPosition(8, 5), new ChessPosition(8, 8))) {
                board.addPiece(new ChessPosition(8, 7), king);
                board.addPiece(new ChessPosition(8, 5), null);
                board.addPiece(new ChessPosition(8, 6), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.ROOK));
                board.addPiece(new ChessPosition(8, 8), null);
            } else {
                throw new InvalidMoveException("Invalid castling move");
            }
        }
    }

    private void performEnPassant(ChessMove move) {
        ChessPosition capturedPawnPosition = new ChessPosition(lastMove.getEndPosition().getRow(), move.getEndPosition().getColumn());
        board.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));
        board.addPiece(move.getStartPosition(), null);
        board.addPiece(capturedPawnPosition, null);
    }

    private boolean isEmptyBetween(ChessPosition start, ChessPosition end) {
        int row = start.getRow();
        int colStart = Math.min(start.getColumn(), end.getColumn()) + 1;
        int colEnd = Math.max(start.getColumn(), end.getColumn());
        for (int col = colStart; col < colEnd; col++) {
            if (board.getPiece(new ChessPosition(row, col)) != null) {
                return false;
            }
        }
        return true;
    }

    private void updateFlagsAfterMove(ChessMove move, ChessPiece piece) {
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (currentTurn == TeamColor.WHITE) {
                whiteKingMoved = true;
            } else {
                blackKingMoved = true;
            }
        } else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            if (currentTurn == TeamColor.WHITE) {
                if (move.getStartPosition().equals(new ChessPosition(1, 1))) {
                    whiteRookMovedLeft = true;
                } else if (move.getStartPosition().equals(new ChessPosition(1, 8))) {
                    whiteRookMovedRight = true;
                }
            } else {
                if (move.getStartPosition().equals(new ChessPosition(8, 1))) {
                    blackRookMovedLeft = true;
                } else if (move.getStartPosition().equals(new ChessPosition(8, 8))) {
                    blackRookMovedRight = true;
                }
            }
        }
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);
        return board.getPieces().stream()
                .filter(piece -> piece.getTeamColor() != teamColor)
                .flatMap(piece -> piece.pieceMoves(board, board.getPosition(piece)).stream())
                .anyMatch(move -> move.getEndPosition().equals(kingPosition));
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && board.getPieces().stream()
                .filter(piece -> piece.getTeamColor() == teamColor)
                .flatMap(piece -> validMoves(board.getPosition(piece)).stream())
                .noneMatch(move -> {
                    try {
                        ChessGame testGame = this.clone();
                        testGame.makeMove(move);
                        return !testGame.isInCheck(teamColor);
                    } catch (InvalidMoveException e) {
                        return false;
                    }
                });
    }

    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && board.getPieces().stream()
                .filter(piece -> piece.getTeamColor() == teamColor)
                .flatMap(piece -> validMoves(board.getPosition(piece)).stream())
                .noneMatch(move -> {
                    try {
                        ChessGame testGame = this.clone();
                        testGame.makeMove(move);
                        return !testGame.isInCheck(teamColor);
                    } catch (InvalidMoveException e) {
                        return false;
                    }
                });
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return board;
    }

    private ChessPosition findKing(TeamColor teamColor) {
        return board.getPieces().stream()
                .filter(piece -> piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING)
                .map(board::getPosition)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No king found for team " + teamColor));
    }

    @Override
    protected ChessGame clone() {
        ChessGame clonedGame = new ChessGame();
        clonedGame.board = this.board.clone();
        clonedGame.currentTurn = this.currentTurn;
        clonedGame.whiteKingMoved = this.whiteKingMoved;
        clonedGame.blackKingMoved = this.blackKingMoved;
        clonedGame.whiteRookMovedLeft = this.whiteRookMovedLeft;
        clonedGame.whiteRookMovedRight = this.whiteRookMovedRight;
        clonedGame.blackRookMovedLeft = this.blackRookMovedLeft;
        clonedGame.blackRookMovedRight = this.blackRookMovedRight;
        clonedGame.lastMove = this.lastMove;
        return clonedGame;
    }
}
