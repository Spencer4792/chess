package model;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game, String winner) {
  public GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game, String winner) {
    this.gameID = gameID;
    this.whiteUsername = whiteUsername;
    this.blackUsername = blackUsername;
    this.gameName = gameName;
    this.game = game;
    this.winner = winner;
  }

  public GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    this(gameID, whiteUsername, blackUsername, gameName, game, null);
  }
}