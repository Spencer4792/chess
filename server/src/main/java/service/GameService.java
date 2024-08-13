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

  public boolean isValidGame(int gameId) throws DataAccessException {
    return dataAccess.getGame(gameId) != null;
  }

  public boolean isAuthorized(String authToken) throws DataAccessException {
    return dataAccess.getAuth(authToken) != null;
  }

  public void resignGame(String authToken, int gameId) throws DataAccessException {
    AuthData auth = dataAccess.getAuth(authToken);
    if (auth == null) {
      throw new DataAccessException("Error: unauthorized");
    }

    GameData game = dataAccess.getGame(gameId);
    if (game == null) {
      throw new DataAccessException("Error: game not found");
    }

    String username = auth.username();
    if (!username.equals(game.whiteUsername()) && !username.equals(game.blackUsername())) {
      throw new DataAccessException("Error: not a player in this game");
    }

    ChessGame chessGame = game.game();
    if (chessGame.isGameOver()) {
      throw new DataAccessException("Error: game is already over");
    }

    chessGame.setGameOver(true);

    GameData updatedGame = new GameData(
            game.gameID(),
            game.whiteUsername(),
            game.blackUsername(),
            game.gameName(),
            chessGame
    );

    dataAccess.updateGame(updatedGame);
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
      if (game.whiteUsername() != null && !game.whiteUsername().isEmpty()) {
        throw new DataAccessException("Error: already taken");
      }
      game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
    } else if (playerColor == ChessGame.TeamColor.BLACK) {
      if (game.blackUsername() != null && !game.blackUsername().isEmpty()) {
        throw new DataAccessException("Error: already taken");
      }
      game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
    } else {
      throw new DataAccessException("Error: bad request");
    }

    dataAccess.updateGame(game);
  }

  public void makeMove(String authToken, int gameId, ChessMove move) throws DataAccessException {
    AuthData auth = dataAccess.getAuth(authToken);
    if (auth == null) {
      throw new DataAccessException("Error: unauthorized");
    }

    GameData game = dataAccess.getGame(gameId);
    if (game == null) {
      throw new DataAccessException("Error: game not found");
    }

    ChessGame chessGame = game.game();
    String currentPlayer = chessGame.getTeamTurn() == ChessGame.TeamColor.WHITE ? game.whiteUsername() : game.blackUsername();

    if (!currentPlayer.equals(auth.username())) {
      throw new DataAccessException("Error: not your turn");
    }

    if (chessGame.isGameOver()) {
      throw new DataAccessException("Error: game is already over");
    }

    try {
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
      throw new DataAccessException("Error: invalid move - " + e.getMessage());
    }
  }

  public GameData getGameState(String authToken, int gameId) throws DataAccessException {
    AuthData auth = dataAccess.getAuth(authToken);
    if (auth == null) {
      throw new DataAccessException("Error: unauthorized");
    }

    GameData game = dataAccess.getGame(gameId);
    if (game == null) {
      throw new DataAccessException("Error: game not found");
    }

    return game;
  }
}