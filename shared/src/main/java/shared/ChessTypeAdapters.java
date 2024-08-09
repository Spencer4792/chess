package shared;

import com.google.gson.*;
import chess.*;

import java.lang.reflect.Type;
import java.util.Map;

public class ChessTypeAdapters {
  public static class ChessBoardAdapter implements JsonSerializer<ChessBoard>, JsonDeserializer<ChessBoard> {
    @Override
    public JsonElement serialize(ChessBoard src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      for (int row = 1; row <= 8; row++) {
        for (int col = 1; col <= 8; col++) {
          ChessPosition position = new ChessPosition(row, col);
          ChessPiece piece = src.getPiece(position);
          if (piece != null) {
            jsonObject.add(position.toString(), context.serialize(piece));
          }
        }
      }
      return jsonObject;
    }

    @Override
    public ChessBoard deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      ChessBoard board = new ChessBoard();
      JsonObject jsonObject = json.getAsJsonObject();
      for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
        ChessPosition position = context.deserialize(new JsonPrimitive(entry.getKey()), ChessPosition.class);
        ChessPiece piece = context.deserialize(entry.getValue(), ChessPiece.class);
        board.addPiece(position, piece);
      }
      return board;
    }
  }

  public static class ChessPieceAdapter implements JsonSerializer<ChessPiece>, JsonDeserializer<ChessPiece> {
    @Override
    public JsonElement serialize(ChessPiece src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("pieceColor", src.getTeamColor().toString());
      jsonObject.addProperty("pieceType", src.getPieceType().toString());
      return jsonObject;
    }

    @Override
    public ChessPiece deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      JsonObject jsonObject = json.getAsJsonObject();
      ChessGame.TeamColor pieceColor = ChessGame.TeamColor.valueOf(jsonObject.get("pieceColor").getAsString());
      ChessPiece.PieceType pieceType = ChessPiece.PieceType.valueOf(jsonObject.get("pieceType").getAsString());
      return new ChessPiece(pieceColor, pieceType);
    }
  }

  public static class ChessGameAdapter implements JsonSerializer<ChessGame>, JsonDeserializer<ChessGame> {
    @Override
    public JsonElement serialize(ChessGame src, Type typeOfSrc, JsonSerializationContext context) {
      if (src == null) return JsonNull.INSTANCE;
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("board", context.serialize(src.getBoard()));
      jsonObject.addProperty("teamTurn", src.getTeamTurn().toString());
      return jsonObject;
    }

    @Override
    public ChessGame deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      if (json.isJsonNull()) return null;
      JsonObject jsonObject = json.getAsJsonObject();
      ChessGame game = new ChessGame();
      if (jsonObject.has("board")) {
        game.setBoard(context.deserialize(jsonObject.get("board"), ChessBoard.class));
      }
      if (jsonObject.has("teamTurn")) {
        game.setTeamTurn(ChessGame.TeamColor.valueOf(jsonObject.get("teamTurn").getAsString()));
      }
      return game;
    }
  }

  public static class ChessPositionAdapter implements JsonSerializer<ChessPosition>, JsonDeserializer<ChessPosition> {
    @Override
    public JsonElement serialize(ChessPosition src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toString());
    }

    @Override
    public ChessPosition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      String posStr = json.getAsString();
      String[] parts = posStr.split(",");
      return new ChessPosition(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
  }

  public static class ChessMoveAdapter implements JsonSerializer<ChessMove>, JsonDeserializer<ChessMove> {
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