package server;

import chess.*;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import model.GameData;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
  private static final Map<Integer, Map<Session, String>> gameSessions = new ConcurrentHashMap<>();
  private final GameService gameService;
  private final Gson gson;

  public WebSocketHandler(GameService gameService) {
    this.gameService = gameService;
    this.gson = new Gson();
  }

  @OnWebSocketConnect
  public void onConnect(Session session) throws Exception {
    // Connection established
  }

  @OnWebSocketClose
  public void onClose(Session session, int statusCode, String reason) {
    for (Map.Entry<Integer, Map<Session, String>> entry : gameSessions.entrySet()) {
      if (entry.getValue().containsKey(session)) {
        String authToken = entry.getValue().remove(session);
        notifyOtherPlayers(entry.getKey(), session, authToken + " has left the game.");
        break;
      }
    }
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) throws Exception {
    UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

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
    }
  }

  private void handleConnect(Session session, UserGameCommand command) throws Exception {
    int gameId = command.getGameID();
    String authToken = command.getAuthToken();

    try {
      if (gameService.isValidGame(gameId) && gameService.isAuthorized(authToken)) {
        gameSessions.computeIfAbsent(gameId, k -> new ConcurrentHashMap<>()).put(session, authToken);
        sendGameState(session, authToken, gameId);
        notifyOtherPlayers(gameId, session, authToken + " has joined the game.");
      } else {
        sendErrorMessage(session, "Error: invalid game ID or unauthorized");
      }
    } catch (Exception e) {
      sendErrorMessage(session, "Error: " + e.getMessage());
    }
  }

  private void handleMakeMove(Session session, UserGameCommand command) throws Exception {
    int gameId = command.getGameID();
    String authToken = command.getAuthToken();
    ChessMove move = command.getChessMove();

    try {
      gameService.makeMove(authToken, gameId, move);
      sendGameStateToAll(gameId);
      notifyOtherPlayers(gameId, session, authToken + " made a move.");
    } catch (Exception e) {
      sendErrorMessage(session, "Error: " + e.getMessage());
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
      sendGameStateToAll(gameId);
    } catch (Exception e) {
      sendErrorMessage(session, "Error: " + e.getMessage());
    }
  }

  private void sendGameStateToAll(int gameId) throws Exception {
    for (Map.Entry<Session, String> entry : gameSessions.get(gameId).entrySet()) {
      sendGameState(entry.getKey(), entry.getValue(), gameId);
    }
  }

  private void sendGameState(Session session, String authToken, int gameId) throws Exception {
    try {
      ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
      GameData gameData = gameService.getGameState(authToken, gameId);
      message.setGame(gameData.game());
      session.getRemote().sendString(gson.toJson(message));
    } catch (Exception e) {
      sendErrorMessage(session, "Error: " + e.getMessage());
    }
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
            e.printStackTrace();
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
          e.printStackTrace();
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