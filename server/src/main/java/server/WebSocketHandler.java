package server;

import chess.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import model.GameData;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@WebSocket
public class WebSocketHandler {
  private static final Logger LOGGER = Logger.getLogger(WebSocketHandler.class.getName());
  private static final Map<Integer, Map<Session, String>> gameSessions = new ConcurrentHashMap<>();
  private final GameService gameService;
  private final Gson gson;

  public WebSocketHandler(GameService gameService) {
    this.gameService = gameService;
    this.gson = new Gson();
  }

  @OnWebSocketConnect
  public void onConnect(Session session) throws Exception {
    LOGGER.info("WebSocket connection opened: " + session.getRemoteAddress().getAddress());
  }

  @OnWebSocketClose
  public void onClose(Session session, int statusCode, String reason) throws Exception {
    LOGGER.info("WebSocket connection closed: " + session.getRemoteAddress().getAddress());
    for (Map.Entry<Integer, Map<Session, String>> entry : gameSessions.entrySet()) {
      if (entry.getValue().containsKey(session)) {
        String authToken = entry.getValue().remove(session);
        notifyOtherPlayers(entry.getKey(), session, authToken + " has left the game.");
        break;
      }
    }
  }

  @OnWebSocketError
  public void onError(Session session, Throwable error) {
    LOGGER.severe("WebSocket error: " + error.getMessage());
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) throws Exception {
    try {
      JsonObject jsonCommand = gson.fromJson(message, JsonObject.class);
      UserGameCommand.CommandType commandType = UserGameCommand.CommandType.valueOf(jsonCommand.get("commandType").getAsString());
      String authToken = jsonCommand.get("authToken").getAsString();
      Integer gameID = jsonCommand.get("gameID").getAsInt();

      UserGameCommand command = new UserGameCommand(commandType, authToken, gameID);

      if (jsonCommand.has("move")) {
        command.setMove(jsonCommand.get("move").toString());
      }

      switch (command.getCommandType()) {
        case CONNECT:
          handleConnect(session, command);
          break;
        case MAKE_MOVE:
          handleMakeMove(session, command);
          break;
        case LEAVE:
          handleLeave(session, command);
          break;
        case RESIGN:
          handleResign(session, command);
          break;
        default:
          sendErrorMessage(session, "Unknown command type");
      }
    } catch (Exception e) {
      LOGGER.severe("Error processing message: " + e.getMessage());
      sendErrorMessage(session, "Error processing message: " + e.getMessage());
    }
  }

  private void handleConnect(Session session, UserGameCommand command) throws Exception {
    int gameId = command.getGameID();
    String authToken = command.getAuthToken();

    try {
      if (gameService.isValidGame(gameId) && gameService.isAuthorized(authToken)) {
        gameSessions.computeIfAbsent(gameId, k -> new ConcurrentHashMap<>()).put(session, authToken);
        notifyOtherPlayers(gameId, session, authToken + " has joined the game.");
        sendGameState(session, authToken, gameId);
      } else {
        sendErrorMessage(session, "Invalid game ID or unauthorized");
      }
    } catch (Exception e) {
      LOGGER.severe("Error in handleConnect: " + e.getMessage());
      sendErrorMessage(session, "Error connecting to game: " + e.getMessage());
    }
  }

  private void handleMakeMove(Session session, UserGameCommand command) throws Exception {
    int gameId = command.getGameID();
    String authToken = command.getAuthToken();
    ChessMove move = command.getChessMove();

    try {
      gameService.makeMove(authToken, gameId, move);
      notifyOtherPlayers(gameId, session, authToken + " made a move.");
      sendGameStateToAll(gameId);
    } catch (Exception e) {
      LOGGER.severe("Error in handleMakeMove: " + e.getMessage());
      sendErrorMessage(session, "Error making move: " + e.getMessage());
    }
  }

  private void handleLeave(Session session, UserGameCommand command) throws Exception {
    int gameId = command.getGameID();
    String authToken = command.getAuthToken();

    gameSessions.get(gameId).remove(session);
    notifyOtherPlayers(gameId, session, authToken + " has left the game.");
  }

  private void handleResign(Session session, UserGameCommand command) throws Exception {
    int gameId = command.getGameID();
    String authToken = command.getAuthToken();

    try {
      gameService.resignGame(authToken, gameId);
      notifyAllPlayers(gameId, authToken + " has resigned from the game.");
      // Remove sendGameStateToAll(gameId) from here
    } catch (Exception e) {
      LOGGER.severe("Error in handleResign: " + e.getMessage());
      sendErrorMessage(session, "Error resigning game: " + e.getMessage());
    }
  }

  private void sendGameStateToAll(int gameId) {
    for (Map.Entry<Session, String> entry : gameSessions.get(gameId).entrySet()) {
      try {
        sendGameState(entry.getKey(), entry.getValue(), gameId);
      } catch (Exception e) {
        LOGGER.severe("Error sending game state: " + e.getMessage());
      }
    }
  }

  private void sendGameState(Session session, String authToken, int gameId) throws Exception {
    ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
    GameData gameData = gameService.getGameState(authToken, gameId);
    message.setGame(gameData.game());
    session.getRemote().sendString(gson.toJson(message));
  }

  private void notifyOtherPlayers(int gameId, Session excludeSession, String notificationMessage) {
    ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
    message.setMessage(notificationMessage);
    String jsonMessage = gson.toJson(message);

    Map<Session, String> sessions = gameSessions.get(gameId);
    if (sessions != null) {
      for (Session session : sessions.keySet()) {
        if (session != excludeSession) {
          try {
            session.getRemote().sendString(jsonMessage);
          } catch (IOException e) {
            LOGGER.severe("Error notifying player: " + e.getMessage());
          }
        }
      }
    }
  }

  private void notifyAllPlayers(int gameId, String notificationMessage) {
    ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
    message.setMessage(notificationMessage);
    String jsonMessage = gson.toJson(message);

    Map<Session, String> sessions = gameSessions.get(gameId);
    if (sessions != null) {
      for (Session session : sessions.keySet()) {
        try {
          session.getRemote().sendString(jsonMessage);
        } catch (IOException e) {
          LOGGER.severe("Error notifying player: " + e.getMessage());
        }
      }
    }
  }

  private void sendErrorMessage(Session session, String errorMessage) throws IOException {
    ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
    message.setErrorMessage(errorMessage);
    session.getRemote().sendString(gson.toJson(message));
  }
}