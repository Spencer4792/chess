package server;

import spark.*;
import com.google.gson.Gson;
import dataaccess.*;
import service.*;
import model.*;
import chess.*;
import java.util.Collection;

public class Server {
    private final UserService userService;
    private final GameService gameService;
    private final Gson gson = new Gson();
    private final WebSocketHandler webSocketHandler;

    public Server() {
        DataAccess dataAccess = new MySqlDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
        webSocketHandler = new WebSocketHandler(gameService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        Spark.webSocket("/ws", new WebSocketHandler(gameService));

        try {
            DatabaseManager.createDatabase();
            DatabaseManager.createTables();
            initializeDatabase();
        } catch (DataAccessException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            return -1;
        }

        // Define routes after WebSocket
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
    }

    private String validateAuthToken(Request req, Response res) throws DataAccessException {
        var authToken = req.headers("Authorization");
        if (authToken == null || authToken.isEmpty()) {
            res.status(401);
            throw new DataAccessException("Error: unauthorized");
        }
        return authToken;
    }

    private Object register(Request req, Response res) {
        try {
            var user = gson.fromJson(req.body(), UserData.class);
            if (user.username() == null || user.password() == null || user.email() == null) {
                res.status(400);
                return gson.toJson(new ErrorResult("Error: bad request"));
            }
            var result = userService.register(user);
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            if (e.getMessage().contains("already taken")) {
                res.status(403);
                return gson.toJson(new ErrorResult("Error: already taken"));
            } else {
                res.status(500);
                return gson.toJson(new ErrorResult("Error: " + e.getMessage()));
            }
        }
    }

    private Object login(Request req, Response res) {
        try {
            var loginRequest = gson.fromJson(req.body(), LoginRequest.class);
            if (loginRequest.username() == null || loginRequest.password() == null) {
                res.status(400);
                return gson.toJson(new ErrorResult("Error: bad request"));
            }
            var result = userService.login(loginRequest.username(), loginRequest.password());
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized")) {
                res.status(401);
                return gson.toJson(new ErrorResult("Error: unauthorized"));
            } else {
                res.status(500);
                return gson.toJson(new ErrorResult("Error: " + e.getMessage()));
            }
        }
    }

    private Object logout(Request req, Response res) {
        try {
            String authToken = validateAuthToken(req, res);
            userService.logout(authToken);
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorResult("Error: unauthorized"));
        }
    }

    private Object listGames(Request req, Response res) {
        try {
            String authToken = validateAuthToken(req, res);
            Collection<GameData> games = gameService.listGames(authToken);
            res.status(200);
            return gson.toJson(new ListGamesResult(games));
        } catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized")) {
                res.status(401);
                return gson.toJson(new ErrorResult("Error: unauthorized"));
            } else {
                res.status(500);
                return gson.toJson(new ErrorResult("Error: " + e.getMessage()));
            }
        }
    }

    private Object createGame(Request req, Response res) {
        try {
            String authToken = validateAuthToken(req, res);
            var createGameRequest = gson.fromJson(req.body(), CreateGameRequest.class);
            if (createGameRequest.gameName() == null || createGameRequest.gameName().isEmpty()) {
                res.status(400);
                return gson.toJson(new ErrorResult("Error: bad request"));
            }
            var gameId = gameService.createGame(authToken, createGameRequest.gameName());
            res.status(200);
            return gson.toJson(new CreateGameResult(gameId));
        } catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized")) {
                res.status(401);
                return gson.toJson(new ErrorResult("Error: unauthorized"));
            } else {
                res.status(500);
                return gson.toJson(new ErrorResult("Error: " + e.getMessage()));
            }
        }
    }

    private Object joinGame(Request req, Response res) {
        try {
            String authToken = validateAuthToken(req, res);
            var joinGameRequest = gson.fromJson(req.body(), JoinGameRequest.class);
            if (joinGameRequest.gameID() <= 0 || joinGameRequest.playerColor() == null) {
                res.status(400);
                return gson.toJson(new ErrorResult("Error: bad request"));
            }
            gameService.joinGame(authToken, joinGameRequest.gameID(), joinGameRequest.playerColor());
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            if (e.getMessage().contains("game not found")) {
                res.status(400);
                return gson.toJson(new ErrorResult("Error: bad request"));
            } else if (e.getMessage().contains("already taken")) {
                res.status(403);
                return gson.toJson(new ErrorResult("Error: already taken"));
            } else if (e.getMessage().contains("unauthorized")) {
                res.status(401);
                return gson.toJson(new ErrorResult("Error: unauthorized"));
            } else {
                res.status(500);
                return gson.toJson(new ErrorResult("Error: " + e.getMessage()));
            }
        }
    }

    private Object getGameState(Request req, Response res) {
        String authToken = req.headers("Authorization");
        int gameId = Integer.parseInt(req.params(":id"));
        try {
            GameData gameData = gameService.getGameState(authToken, gameId);
            res.status(200);
            return gson.toJson(gameData);
        } catch (Exception e) {
            res.status(401);
            return gson.toJson(new ErrorResult(e.getMessage()));
        }
    }

    private Object makeMove(Request req, Response res) {
        String authToken = req.headers("Authorization");
        int gameId = Integer.parseInt(req.params(":id"));
        ChessMove move = gson.fromJson(req.body(), ChessMove.class);
        try {
            gameService.makeMove(authToken, gameId, move);
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            res.status(400);
            return gson.toJson(new ErrorResult(e.getMessage()));
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
    private record JoinGameRequest(int gameID, ChessGame.TeamColor playerColor) {}
    private record ListGamesResult(Collection<GameData> games) {}
    private record CreateGameResult(int gameID) {}
    private record ErrorResult(String message) {}
}