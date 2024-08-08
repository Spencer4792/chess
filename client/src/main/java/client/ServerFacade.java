package client;

import chess.*;
import com.google.gson.*;
import model.AuthData;
import model.GameData;
import model.GameState;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class ServerFacade {

  private final String serverUrl;
  private final HttpClient client;
  private final Gson gson;

  public ServerFacade(String serverUrl) {
    this.serverUrl = serverUrl;
    this.client = HttpClient.newHttpClient();
    this.gson = new GsonBuilder()
            .registerTypeAdapter(ChessBoard.class, new ChessBoardAdapter())
            .registerTypeAdapter(ChessGame.class, new ChessGameAdapter())
            .registerTypeAdapter(ChessPiece.class, new ChessPieceAdapter())
            .registerTypeAdapter(ChessPosition.class, new ChessPositionAdapter())
            .create();
  }

  public AuthData register(String username, String password, String email) throws ClientException {
    var path = "/user";
    var request = new RegisterRequest(username, password, email);
    return makeRequest("POST", path, request, AuthData.class);
  }

  public AuthData login(String username, String password) throws ClientException {
    var path = "/session";
    var request = new LoginRequest(username, password);
    return makeRequest("POST", path, request, AuthData.class);
  }

  public void logout(String authToken) throws ClientException {
    var path = "/session";
    makeRequest("DELETE", path, null, null, authToken);
  }

  public int createGame(String authToken, String gameName) throws ClientException {
    var path = "/game";
    var request = new CreateGameRequest(gameName);
    var response = makeRequest("POST", path, request, CreateGameResponse.class, authToken);
    return response.gameID();
  }

  public Collection<GameData> listGames(String authToken) throws ClientException {
    var path = "/game";
    var response = makeRequest("GET", path, null, ListGamesResponse.class, authToken);
    return Arrays.asList(response.games());
  }

  public void joinGame(String authToken, int gameID, String playerColor) throws ClientException {
    var path = "/game";
    var request = new JoinGameRequest(playerColor, gameID);
    makeRequest("PUT", path, request, null, authToken);
  }

  public void makeMove(String authToken, int gameId, ChessMove move) throws ClientException {
    var path = "/game/" + gameId + "/move";
    makeRequest("PUT", path, move, null, authToken);
  }

  public GameState getGameState(String authToken, int gameId) throws ClientException {
    var path = "/game/" + gameId;
    GameData gameData = makeRequest("GET", path, null, GameData.class, authToken);
    return new GameState(gameData.gameID(), gameData.gameName(), gameData.whiteUsername(), gameData.blackUsername(), gameData.game());
  }

  public void clear() throws ClientException {
    var path = "/db";
    makeRequest("DELETE", path, null, null);
  }

  private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ClientException {
    return makeRequest(method, path, request, responseClass, null);
  }

  private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws ClientException {
    try {
      URI uri = URI.create(serverUrl + path);
      HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri);

      if (authToken != null) {
        requestBuilder.header("Authorization", authToken);
      }

      if (request != null) {
        var requestBody = gson.toJson(request);
        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(requestBody));
      } else {
        requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
      }

      HttpResponse<String> response = client.send(requestBuilder.build(),
              HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() >= 300) {
        throw new ClientException(extractErrorMessage(response.body()));
      }

      if (responseClass != null) {
        return gson.fromJson(response.body(), responseClass);
      }
      return null;
    } catch (ClientException e) {
      throw e;
    } catch (Exception e) {
      throw new ClientException(e.getMessage());
    }
  }

  private String extractErrorMessage(String errorBody) {
    try {
      JsonObject jsonObject = JsonParser.parseString(errorBody).getAsJsonObject();
      return jsonObject.get("message").getAsString().replace("Error: ", "");
    } catch (Exception e) {
      return errorBody.replace("Error: ", "");
    }
  }

  private record RegisterRequest(String username, String password, String email) {}
  private record LoginRequest(String username, String password) {}
  private record CreateGameRequest(String gameName) {}
  private record CreateGameResponse(int gameID) {}
  private record ListGamesResponse(GameData[] games) {}
  private record JoinGameRequest(String playerColor, int gameID) {}

  private static class ChessBoardAdapter implements JsonSerializer<ChessBoard>, JsonDeserializer<ChessBoard> {
    @Override
    public JsonElement serialize(ChessBoard src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      JsonObject boardObject = new JsonObject();
      for (Map.Entry<ChessPosition, ChessPiece> entry : src.getBoard().entrySet()) {
        boardObject.add(entry.getKey().toString(), context.serialize(entry.getValue()));
      }
      jsonObject.add("board", boardObject);
      return jsonObject;
    }

    @Override
    public ChessBoard deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      ChessBoard chessBoard = new ChessBoard();
      JsonObject jsonObject = json.getAsJsonObject();
      JsonObject boardObject = jsonObject.getAsJsonObject("board");
      for (Map.Entry<String, JsonElement> entry : boardObject.entrySet()) {
        ChessPosition position = context.deserialize(new JsonPrimitive(entry.getKey()), ChessPosition.class);
        ChessPiece piece = context.deserialize(entry.getValue(), ChessPiece.class);
        chessBoard.addPiece(position, piece);
      }
      return chessBoard;
    }
  }

  private static class ChessGameAdapter implements JsonSerializer<ChessGame>, JsonDeserializer<ChessGame> {
    @Override
    public JsonElement serialize(ChessGame src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("board", context.serialize(src.getBoard()));
      jsonObject.addProperty("teamTurn", src.getTeamTurn().toString());
      return jsonObject;
    }

    @Override
    public ChessGame deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      JsonObject jsonObject = json.getAsJsonObject();
      ChessGame game = new ChessGame();
      game.setBoard(context.deserialize(jsonObject.get("board"), ChessBoard.class));
      game.setTeamTurn(ChessGame.TeamColor.valueOf(jsonObject.get("teamTurn").getAsString()));
      return game;
    }
  }

  private static class ChessPieceAdapter implements JsonSerializer<ChessPiece>, JsonDeserializer<ChessPiece> {
    @Override
    public JsonElement serialize(ChessPiece src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("pieceColor", src.getTeamColor().toString());
      jsonObject.addProperty("pieceType", src.getPieceType().toString());
      return jsonObject;
    }

    @Override
    public ChessPiece deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      if (json.isJsonNull()) {
        return null;
      }
      JsonObject jsonObject = json.getAsJsonObject();
      JsonElement colorElement = jsonObject.get("pieceColor");
      JsonElement typeElement = jsonObject.get("pieceType");

      if (colorElement == null || typeElement == null) {
        return null;
      }

      ChessGame.TeamColor pieceColor = ChessGame.TeamColor.valueOf(colorElement.getAsString());
      ChessPiece.PieceType pieceType = ChessPiece.PieceType.valueOf(typeElement.getAsString());
      return new ChessPiece(pieceColor, pieceType);
    }
  }

  private static class ChessPositionAdapter implements JsonSerializer<ChessPosition>, JsonDeserializer<ChessPosition> {
    @Override
    public JsonElement serialize(ChessPosition src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toString());
    }

    @Override
    public ChessPosition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      String posStr = json.getAsString();
      if (posStr.startsWith("ChessPosition{")) {
        // Parse the string representation
        String[] parts = posStr.substring(14, posStr.length() - 1).split(", ");
        int row = Integer.parseInt(parts[0].split("=")[1]);
        int col = Integer.parseInt(parts[1].split("=")[1]);
        return new ChessPosition(row, col);
      } else {
        // Fallback to the old format if needed
        String[] parts = posStr.split(",");
        return new ChessPosition(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
      }
    }
  }
}