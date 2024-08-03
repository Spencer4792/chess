package service;

import chess.ChessGame;
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
    GameData newGame = new GameData(gameID, null, null, gameName, new ChessGame());
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
}