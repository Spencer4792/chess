package handler;

import service.GameService;
import spark.Request;
import spark.Response;
import model.GameData;
import java.util.List;

public class ListGamesHandler extends BaseHandler {
  private final GameService gameService;

  public ListGamesHandler(GameService gameService) {
    this.gameService = gameService;
  }

  @Override
  public Object handle(Request req, Response res) throws Exception {
    setResponseHeaders(res);
    String authToken = req.headers("Authorization");
    try {
      List<GameData> games = gameService.listGames(authToken);
      res.status(200);
      return serialize(new ListGamesResult(games));
    } catch (Exception e) {
      res.status(401);
      return serialize(new ErrorResult(e.getMessage()));
    }
  }

  private record ListGamesResult(List<GameData> games) {}
  private record ErrorResult(String message) {}
}