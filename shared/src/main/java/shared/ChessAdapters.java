package shared;

import chess.*;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Map;

public class ChessAdapters {
  public static class ChessBoardAdapter implements JsonSerializer<ChessBoard>, JsonDeserializer<ChessBoard> {
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

  public static class ChessPieceAdapter implements JsonSerializer<ChessPiece>, JsonDeserializer<ChessPiece> {
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

  public static class ChessPositionAdapter implements JsonSerializer<ChessPosition>, JsonDeserializer<ChessPosition> {
    @Override
    public JsonElement serialize(ChessPosition src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toString());
    }

    @Override
    public ChessPosition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      String positionString = json.getAsString();
      if (positionString.startsWith("ChessPosition{")) {
        String[] parts = positionString.substring(14, positionString.length() - 1).split(", ");
        int row = Integer.parseInt(parts[0].split("=")[1]);
        int col = Integer.parseInt(parts[1].split("=")[1]);
        return new ChessPosition(row, col);
      } else {
        String[] parts = positionString.split(",");
        return new ChessPosition(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
      }
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

  public static class ChessGameAdapter implements JsonSerializer<ChessGame>, JsonDeserializer<ChessGame> {
    @Override
    public JsonElement serialize(ChessGame src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("board", context.serialize(src.getBoard()));
      jsonObject.addProperty("teamTurn", src.getTeamTurn().toString());
      jsonObject.add("lastMove", context.serialize(src.getLastMove()));
      jsonObject.addProperty("isGameOver", src.isGameOver());
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
      if (jsonObject.has("isGameOver")) {
        game.setGameOver(jsonObject.get("isGameOver").getAsBoolean());
      }
      return game;
    }
  }
}