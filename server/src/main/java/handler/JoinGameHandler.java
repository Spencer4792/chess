package handler;

import chess.ChessGame;
import service.GameService;
import spark.Request;
import spark.Response;

public class JoinGameHandler extends BaseHandler {
  private final GameService gameService;

  public JoinGameHandler(GameService gameService) {
    this.gameService = gameService;
  }

  @Override
  public Object handle(Request req, Response res) throws Exception {
    setResponseHeaders(res);
    String authToken = req.headers("Authorization");
    var joinGameRequest = deserialize(req.body(), JoinGameRequest.class);
    gameService.joinGame(authToken, joinGameRequest.gameID(), joinGameRequest.playerColor());
    res.status(200);
    return "{}";
  }

  private record JoinGameRequest(int gameID, ChessGame.TeamColor playerColor) {}
}