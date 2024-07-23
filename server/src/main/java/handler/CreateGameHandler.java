package handler;

import service.GameService;
import spark.Request;
import spark.Response;

public class CreateGameHandler extends BaseHandler {
  private final GameService gameService;

  public CreateGameHandler(GameService gameService) {
    this.gameService = gameService;
  }

  @Override
  public Object handle(Request req, Response res) throws Exception {
    setResponseHeaders(res);
    String authToken = req.headers("Authorization");
    var createGameRequest = deserialize(req.body(), CreateGameRequest.class);
    int gameID = gameService.createGame(authToken, createGameRequest.gameName());
    res.status(200);
    return serialize(new CreateGameResult(gameID));
  }

  private record CreateGameRequest(String gameName) {}
  private record CreateGameResult(int gameID) {}
}