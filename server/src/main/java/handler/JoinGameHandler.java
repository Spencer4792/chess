package handler;

import chess.ChessGame;
import service.GameService;
import spark.Request;
import spark.Response;
import dataaccess.DataAccessException;

public class JoinGameHandler extends BaseHandler {
  private final GameService gameService;

  public JoinGameHandler(GameService gameService) {
    this.gameService = gameService;
  }

  @Override
  public Object handle(Request req, Response res) throws Exception {
    setResponseHeaders(res);
    var joinRequest = deserialize(req.body(), JoinGameRequest.class);
    String authToken = req.headers("Authorization");

    try {
      if (joinRequest.playerColor() == null ||
              (joinRequest.playerColor() != ChessGame.TeamColor.WHITE &&
                      joinRequest.playerColor() != ChessGame.TeamColor.BLACK)) {
        res.status(400);
        return serialize(new ErrorResult("Error: bad request"));
      }

      gameService.joinGame(authToken, joinRequest.gameID(), joinRequest.playerColor());
      res.status(200);
      return "{}";
    } catch (DataAccessException e) {
      if (e.getMessage().contains("unauthorized")) {
        res.status(401);
      } else if (e.getMessage().contains("already taken")) {
        res.status(403);
      } else if (e.getMessage().contains("bad request")) {
        res.status(400);
      } else {
        res.status(500);
      }
      return serialize(new ErrorResult("Error: " + e.getMessage()));
    }
  }

  private record JoinGameRequest(int gameID, ChessGame.TeamColor playerColor) {}
  private record ErrorResult(String message) {}
}