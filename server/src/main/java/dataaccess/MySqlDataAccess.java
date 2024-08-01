package dataaccess;

import model.UserData;
import model.GameData;
import model.AuthData;
import chess.*;
import com.google.gson.*;
import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;

public class MySqlDataAccess implements DataAccess {
  private final Gson gson;

  public MySqlDataAccess() {
    this.gson = new GsonBuilder()
            .registerTypeAdapter(ChessGame.class, new ChessGameAdapter())
            .registerTypeAdapter(ChessBoard.class, new ChessBoardAdapter())
            .registerTypeAdapter(ChessPosition.class, new ChessPositionAdapter())
            .registerTypeAdapter(ChessPiece.class, new ChessPieceAdapter())
            .registerTypeAdapter(ChessMove.class, new ChessMoveAdapter())
            .create();
  }

  @Override
  public void clear() throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      try (Statement stmt = conn.createStatement()) {
        stmt.executeUpdate("DELETE FROM auth_tokens");
        stmt.executeUpdate("DELETE FROM games");
        stmt.executeUpdate("DELETE FROM users");
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error clearing database: " + e.getMessage());
    }
  }

  @Override
  public void createUser(UserData user) throws DataAccessException {
    String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, user.username());
        stmt.setString(2, hashedPassword);
        stmt.setString(3, user.email());
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error creating user: " + e.getMessage());
    }
  }

  @Override
  public UserData getUser(String username) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
        stmt.setString(1, username);
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
          }
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error getting user: " + e.getMessage());
    }
    return null;
  }

  @Override
  public void createGame(GameData game) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO games (white_username, black_username, game_name, game_state) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, game.whiteUsername());
        stmt.setString(2, game.blackUsername());
        stmt.setString(3, game.gameName());
        stmt.setString(4, gson.toJson(game.game()));
        stmt.executeUpdate();
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            game = new GameData(generatedKeys.getInt(1), game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
          }
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error creating game: " + e.getMessage());
    }
  }

  @Override
  public GameData getGame(int gameId) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM games WHERE game_id = ?")) {
        stmt.setInt(1, gameId);
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            return new GameData(
                    rs.getInt("game_id"),
                    rs.getString("white_username"),
                    rs.getString("black_username"),
                    rs.getString("game_name"),
                    gson.fromJson(rs.getString("game_state"), ChessGame.class)
            );
          }
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error getting game: " + e.getMessage());
    }
    return null;
  }

  @Override
  public void updateGame(GameData game) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement("UPDATE games SET white_username = ?, black_username = ?, game_name = ?, game_state = ? WHERE game_id = ?")) {
        stmt.setString(1, game.whiteUsername());
        stmt.setString(2, game.blackUsername());
        stmt.setString(3, game.gameName());
        stmt.setString(4, gson.toJson(game.game()));
        stmt.setInt(5, game.gameID());
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error updating game: " + e.getMessage());
    }
  }

  @Override
  public void createAuth(AuthData auth) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO auth_tokens (auth_token, username) VALUES (?, ?)")) {
        stmt.setString(1, auth.authToken());
        stmt.setString(2, auth.username());
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error creating auth token: " + e.getMessage());
    }
  }

  @Override
  public AuthData getAuth(String authToken) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM auth_tokens WHERE auth_token = ?")) {
        stmt.setString(1, authToken);
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            return new AuthData(rs.getString("auth_token"), rs.getString("username"));
          }
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error getting auth token: " + e.getMessage());
    }
    return null;
  }

  @Override
  public void deleteAuth(String authToken) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM auth_tokens WHERE auth_token = ?")) {
        stmt.setString(1, authToken);
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error deleting auth token: " + e.getMessage());
    }
  }

  @Override
  public Collection<GameData> listGames() throws DataAccessException {
    Collection<GameData> games = new ArrayList<>();
    try (Connection conn = DatabaseManager.getConnection()) {
      try (Statement stmt = conn.createStatement()) {
        try (ResultSet rs = stmt.executeQuery("SELECT * FROM games")) {
          while (rs.next()) {
            ChessGame game = gson.fromJson(rs.getString("game_state"), ChessGame.class);
            games.add(new GameData(
                    rs.getInt("game_id"),
                    rs.getString("white_username"),
                    rs.getString("black_username"),
                    rs.getString("game_name"),
                    game
            ));
          }
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error listing games: " + e.getMessage());
    }
    return games;
  }

  private static class ChessGameAdapter implements JsonSerializer<ChessGame>, JsonDeserializer<ChessGame> {
    @Override
    public JsonElement serialize(ChessGame src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("board", context.serialize(src.getBoard()));
      jsonObject.addProperty("teamTurn", src.getTeamTurn().toString());
      jsonObject.add("lastMove", context.serialize(src.getLastMove()));
      return jsonObject;
    }

    @Override
    public ChessGame deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      JsonObject jsonObject = json.getAsJsonObject();
      ChessGame game = new ChessGame();
      game.setBoard(context.deserialize(jsonObject.get("board"), ChessBoard.class));
      game.setTeamTurn(ChessGame.TeamColor.valueOf(jsonObject.get("teamTurn").getAsString()));
      if (jsonObject.has("lastMove") && !jsonObject.get("lastMove").isJsonNull()) {
        ChessMove lastMove = context.deserialize(jsonObject.get("lastMove"), ChessMove.class);
        game.setLastMove(lastMove);
      }
      return game;
    }
  }

  private static class ChessBoardAdapter implements JsonSerializer<ChessBoard>, JsonDeserializer<ChessBoard> {
    @Override
    public JsonElement serialize(ChessBoard src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      JsonObject boardState = new JsonObject();
      for (Map.Entry<ChessPosition, ChessPiece> entry : src.getBoard().entrySet()) {
        boardState.add(entry.getKey().toString(), context.serialize(entry.getValue()));
      }
      jsonObject.add("board", boardState);
      return jsonObject;
    }

    @Override
    public ChessBoard deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      JsonObject jsonObject = json.getAsJsonObject();
      ChessBoard board = new ChessBoard();
      JsonObject boardState = jsonObject.getAsJsonObject("board");
      for (Map.Entry<String, JsonElement> entry : boardState.entrySet()) {
        ChessPosition position = context.deserialize(new JsonPrimitive(entry.getKey()), ChessPosition.class);
        ChessPiece piece = context.deserialize(entry.getValue(), ChessPiece.class);
        board.addPiece(position, piece);
      }
      return board;
    }
  }
  private static class ChessPositionAdapter implements JsonSerializer<ChessPosition>, JsonDeserializer<ChessPosition> {
    @Override
    public JsonElement serialize(ChessPosition src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toString());
    }

    @Override
    public ChessPosition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      String positionString = json.getAsString();
      if (positionString.startsWith("ChessPosition{")) {
        // Parse the string representation
        String[] parts = positionString.substring(14, positionString.length() - 1).split(", ");
        int row = Integer.parseInt(parts[0].split("=")[1]);
        int col = Integer.parseInt(parts[1].split("=")[1]);
        return new ChessPosition(row, col);
      } else {
        // If it's a JSON object, parse it as before
        JsonObject jsonObject = json.getAsJsonObject();
        int row = jsonObject.get("row").getAsInt();
        int col = jsonObject.get("col").getAsInt();
        return new ChessPosition(row, col);
      }
    }
  }

  private static class ChessPieceAdapter implements JsonSerializer<ChessPiece>, JsonDeserializer<ChessPiece> {
    @Override
    public JsonElement serialize(ChessPiece src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("pieceType", src.getPieceType().toString());
      jsonObject.addProperty("teamColor", src.getTeamColor().toString());
      jsonObject.addProperty("hasMoved", src.hasMoved());
      return jsonObject;
    }

    @Override
    public ChessPiece deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      JsonObject jsonObject = json.getAsJsonObject();
      ChessPiece.PieceType pieceType = ChessPiece.PieceType.valueOf(jsonObject.get("pieceType").getAsString());
      ChessGame.TeamColor teamColor = ChessGame.TeamColor.valueOf(jsonObject.get("teamColor").getAsString());
      ChessPiece piece = new ChessPiece(teamColor, pieceType);
      piece.setHasMoved(jsonObject.get("hasMoved").getAsBoolean());
      return piece;
    }
  }

  private static class ChessMoveAdapter implements JsonSerializer<ChessMove>, JsonDeserializer<ChessMove> {
    @Override
    public JsonElement serialize(ChessMove src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("startPosition", context.serialize(src.getStartPosition()));
      jsonObject.add("endPosition", context.serialize(src.getEndPosition()));
      if (src.getPromotionPiece() != null) {
        jsonObject.addProperty("promotionPiece", src.getPromotionPiece().toString());
      }
      return jsonObject;
    }

    @Override
    public ChessMove deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      JsonObject jsonObject = json.getAsJsonObject();
      ChessPosition startPosition = context.deserialize(jsonObject.get("startPosition"), ChessPosition.class);
      ChessPosition endPosition = context.deserialize(jsonObject.get("endPosition"), ChessPosition.class);
      ChessPiece.PieceType promotionPiece = null;
      if (jsonObject.has("promotionPiece")) {
        promotionPiece = ChessPiece.PieceType.valueOf(jsonObject.get("promotionPiece").getAsString());
      }
      return new ChessMove(startPosition, endPosition, promotionPiece);
    }
  }
}