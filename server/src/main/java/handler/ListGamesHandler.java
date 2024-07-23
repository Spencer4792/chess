package handler;

import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;

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
    List<GameData> games = gameService.listGames(authToken);
    res.status(200);
    return serialize(new ListGamesResult(games));
  }

  private record ListGamesResult(List<GameData> games) {}
}