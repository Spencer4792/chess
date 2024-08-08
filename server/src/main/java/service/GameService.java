package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.*;
import model.*;
import java.util.Collection;
import java.util.UUID;

public class GameService {
  private final DataAccess dataAccess;

  public GameService(DataAccess dataAccess) {
    this.dataAccess = dataAccess;
  }

  public Collection<GameData> listGames(String authToken) throws DataAccessException {
    if (dataAccess.getAuth(authToken) == null) {
      throw new DataAccessException("Error: unauthorized");
    }
    return dataAccess.listGames();
  }

  public int createGame(String authToken, String gameName) throws DataAccessException {
    if (dataAccess.getAuth(authToken) == null) {
      throw new DataAccessException("Error: unauthorized");
    }
    if (gameName == null || gameName.isEmpty()) {
      throw new DataAccessException("Error: bad request");
    }
    int gameID = Math.abs(UUID.randomUUID().hashCode());
    ChessGame newChessGame = new ChessGame();
    GameData newGame = new GameData(gameID, null, null, gameName, newChessGame);
    dataAccess.createGame(newGame);
    return gameID;
  }

  public void joinGame(String authToken, int gameID, ChessGame.TeamColor playerColor) throws DataAccessException {
    AuthData auth = dataAccess.getAuth(authToken);
    if (auth == null) {
      throw new DataAccessException("Error: unauthorized");
    }

    GameData game = dataAccess.getGame(gameID);
    if (game == null) {
      throw new DataAccessException("Error: bad request");
    }

    String username = auth.username();
    if (playerColor == ChessGame.TeamColor.WHITE) {
      if (game.whiteUsername() != null) {
        throw new DataAccessException("Error: already taken");
      }
      game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
    } else if (playerColor == ChessGame.TeamColor.BLACK) {
      if (game.blackUsername() != null) {
        throw new DataAccessException("Error: already taken");
      }
      game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
    } else {
      throw new DataAccessException("Error: bad request");
    }

    dataAccess.updateGame(game);
  }

  public void makeMove(String authToken, int gameId, ChessMove move) throws DataAccessException {
    try {
      AuthData auth = dataAccess.getAuth(authToken);
      if (auth == null) {
        throw new DataAccessException("unauthorized");
      }

      GameData game = dataAccess.getGame(gameId);
      if (game == null) {
        throw new DataAccessException("game not found");
      }

      ChessGame chessGame = game.game();
      String currentPlayer = chessGame.getTeamTurn() == ChessGame.TeamColor.WHITE ? game.whiteUsername() : game.blackUsername();

      if (!currentPlayer.equals(auth.username())) {
        throw new DataAccessException("not your turn");
      }

      chessGame.makeMove(move);

      GameData updatedGame = new GameData(
              game.gameID(),
              game.whiteUsername(),
              game.blackUsername(),
              game.gameName(),
              chessGame
      );

      dataAccess.updateGame(updatedGame);
    } catch (InvalidMoveException e) {
      throw new DataAccessException("invalid move: " + e.getMessage());
    }
  }

  public GameData getGameState(String authToken, int gameId) throws DataAccessException {
    AuthData auth = dataAccess.getAuth(authToken);
    if (auth == null) {
      throw new DataAccessException("unauthorized");
    }

    GameData game = dataAccess.getGame(gameId);
    if (game == null) {
      throw new DataAccessException("game not found");
    }

    return game;
  }
}