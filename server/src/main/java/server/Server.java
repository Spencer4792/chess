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
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (username VARCHAR(255) PRIMARY KEY, password VARCHAR(255) NOT NULL, email VARCHAR(255) NOT NULL)");
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS games (game_id INT PRIMARY KEY AUTO_INCREMENT, white_username VARCHAR(255), black_username VARCHAR(255), game_name VARCHAR(255) NOT NULL, game_state TEXT NOT NULL)");
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS auth_tokens (auth_token VARCHAR(255) PRIMARY KEY, username VARCHAR(255) NOT NULL)");
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to initialize database: " + e.getMessage());
        }
    }

    private Object register(Request req, Response res) throws DataAccessException {
        var user = gson.fromJson(req.body(), UserData.class);
        var result = userService.register(user);
        res.status(200);
        return gson.toJson(result);
    }

    private Object login(Request req, Response res) throws DataAccessException {
        var loginRequest = gson.fromJson(req.body(), LoginRequest.class);
        var result = userService.login(loginRequest.username(), loginRequest.password());
        res.status(200);
        return gson.toJson(result);
    }

    private Object logout(Request req, Response res) throws DataAccessException {
        var authToken = req.headers("Authorization");
        userService.logout(authToken);
        res.status(200);
        return "{}";
    }

    private Object listGames(Request req, Response res) throws DataAccessException {
        var authToken = req.headers("Authorization");
        var games = gameService.listGames(authToken);
        res.status(200);
        return gson.toJson(new ListGamesResult(games));
    }

    private Object createGame(Request req, Response res) throws DataAccessException {
        var authToken = req.headers("Authorization");
        var createGameRequest = gson.fromJson(req.body(), CreateGameRequest.class);
        var gameId = gameService.createGame(authToken, createGameRequest.gameName());
        res.status(200);
        return gson.toJson(new CreateGameResult(gameId));
    }

    private Object joinGame(Request req, Response res) throws DataAccessException {
        var authToken = req.headers("Authorization");
        var joinGameRequest = gson.fromJson(req.body(), JoinGameRequest.class);
        gameService.joinGame(authToken, joinGameRequest.gameID(), joinGameRequest.playerColor());
        res.status(200);
        return "{}";
    }

    private Object clear(Request req, Response res) throws DataAccessException {
        userService.clearAll();
        res.status(200);
        return "{}";
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