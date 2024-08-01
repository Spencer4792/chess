package server;

import spark.*;
import com.google.gson.Gson;
import dataaccess.*;
import service.*;
import model.*;
import java.util.Collection;

public class Server {
    private final UserService userService;
    private final GameService gameService;
    private final Gson gson = new Gson();

    public Server() {
        DataAccess dataAccess = new MySqlDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        try {
            DatabaseManager.createDatabase();
            initializeDatabase();
        } catch (DataAccessException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            return -1;
        }

        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clear);

        Spark.exception(DataAccessException.class, this::exceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void initializeDatabase() throws DataAccessException {
        // Your database initialization code here
    }

    private Object register(Request req, Response res) {
        try {
            var user = gson.fromJson(req.body(), UserData.class);
            var result = userService.register(user);
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            res.status(403);
            return gson.toJson(new ErrorResult("Error: already taken"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResult("Error: " + e.getMessage()));
        }
    }

    private Object login(Request req, Response res) {
        try {
            var loginRequest = gson.fromJson(req.body(), LoginRequest.class);
            var result = userService.login(loginRequest.username(), loginRequest.password());
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorResult("Error: unauthorized"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResult("Error: " + e.getMessage()));
        }
    }

    private Object logout(Request req, Response res) {
        try {
            var authToken = req.headers("Authorization");
            userService.logout(authToken);
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorResult("Error: unauthorized"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResult("Error: " + e.getMessage()));
        }
    }

    private Object listGames(Request req, Response res) {
        try {
            var authToken = req.headers("Authorization");
            var games = gameService.listGames(authToken);
            res.status(200);
            return gson.toJson(new ListGamesResult(games));
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorResult("Error: unauthorized"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResult("Error: " + e.getMessage()));
        }
    }

    private Object createGame(Request req, Response res) {
        try {
            var authToken = req.headers("Authorization");
            var createGameRequest = gson.fromJson(req.body(), CreateGameRequest.class);
            var gameId = gameService.createGame(authToken, createGameRequest.gameName());
            res.status(200);
            return gson.toJson(new CreateGameResult(gameId));
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorResult("Error: unauthorized"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResult("Error: " + e.getMessage()));
        }
    }

    private Object joinGame(Request req, Response res) {
        try {
            var authToken = req.headers("Authorization");
            var joinGameRequest = gson.fromJson(req.body(), JoinGameRequest.class);
            gameService.joinGame(authToken, joinGameRequest.gameID(), joinGameRequest.playerColor());
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            if (e.getMessage().contains("already taken")) {
                res.status(403);
                return gson.toJson(new ErrorResult("Error: already taken"));
            } else {
                res.status(401);
                return gson.toJson(new ErrorResult("Error: unauthorized"));
            }
        } catch (IllegalArgumentException e) {
            res.status(400);
            return gson.toJson(new ErrorResult("Error: bad request"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResult("Error: " + e.getMessage()));
        }
    }

    private Object clear(Request req, Response res) {
        try {
            userService.clearAll();
            res.status(200);
            return "{}";
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResult("Error: " + e.getMessage()));
        }
    }

    private void exceptionHandler(Exception e, Request req, Response res) {
        res.status(500);
        res.body(gson.toJson(new ErrorResult(e.getMessage())));
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private record LoginRequest(String username, String password) {}
    private record CreateGameRequest(String gameName) {}
    private record JoinGameRequest(int gameID, chess.ChessGame.TeamColor playerColor) {}
    private record ListGamesResult(Collection<GameData> games) {}
    private record CreateGameResult(int gameID) {}
    private record ErrorResult(String message) {}
}