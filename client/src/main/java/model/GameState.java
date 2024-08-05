package model;

import chess.ChessGame;

public class GameState {
  private final int gameId;
  private final String gameName;
  private final String whiteUsername;
  private final String blackUsername;
  private final ChessGame game;

  public GameState(int gameId, String gameName, String whiteUsername, String blackUsername, ChessGame game) {
    this.gameId = gameId;
    this.gameName = gameName;
    this.whiteUsername = whiteUsername;
    this.blackUsername = blackUsername;
    this.game = game;
  }

  public int getGameId() { return gameId; }
  public String getGameName() { return gameName; }
  public String getWhiteUsername() { return whiteUsername; }
  public String getBlackUsername() { return blackUsername; }
  public ChessGame getGame() { return game; }
}