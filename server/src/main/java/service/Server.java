package server;

import spark.*;
import com.google.gson.Gson;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import service.UserService;
import service.GameService;
import service.ClearService;
import handler.*;

public class Server {

  private final UserService userService;
  private final GameService gameService;
  private final ClearService clearService;

  public Server() {
    DataAccess dataAccess = new MemoryDataAccess();
    userService = new UserService(dataAccess);
    gameService = new GameService(dataAccess);
    clearService = new ClearService(dataAccess);
  }

  public int run(int desiredPort) {
    Spark.port(desiredPort);

    Spark.staticFiles.location("web");

    // Register your endpoints and handle exceptions here.
    Spark.post("/user", new RegisterHandler(userService));
    Spark.post("/session", new LoginHandler(userService));
    Spark.delete("/session", new LogoutHandler(userService));
    Spark.get("/game", new ListGamesHandler(gameService));
    Spark.post("/game", new CreateGameHandler(gameService));
    Spark.put("/game", new JoinGameHandler(gameService));
    Spark.delete("/db", new ClearApplicationHandler(clearService));

    Spark.exception(Exception.class, this::exceptionHandler);

    Spark.awaitInitialization();
    return Spark.port();
  }

  private void exceptionHandler(Exception e, Request req, Response res) {
    res.type("application/json");
    if (e instanceof dataaccess.DataAccessException) {
      res.status(500);
    } else {
      res.status(400);
    }
    res.body(new Gson().toJson(new ErrorResponse(e.getMessage())));
  }

  public void stop() {
    Spark.stop();
    Spark.awaitStop();
  }

  private static class ErrorResponse {
    private final String message;

    public ErrorResponse(String message) {
      this.message = message;
    }
  }
}