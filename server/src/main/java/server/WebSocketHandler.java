package server;

import chess.*;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

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
    // Remove the session from all games
    for (Map<Session, String> sessions : gameSessions.values()) {
      sessions.remove(session);
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

    // Validate the authToken and gameId
    if (gameService.isValidGame(gameId) && gameService.isAuthorized(authToken)) {
      // Add the session to the game
      gameSessions.computeIfAbsent(gameId, k -> new ConcurrentHashMap<>()).put(session, authToken);

      // Send the current game state to the connected client
      sendGameState(session, gameId);

      // Notify other players about the new connection
      notifyPlayers(gameId, authToken + " has joined the game.");
    } else {
      sendErrorMessage(session, "Invalid game ID or authorization.");
    }
  }

  private void handleMakeMove(Session session, UserGameCommand command) throws Exception {
    int gameId = command.getGameID();
    String authToken = command.getAuthToken();
    ChessMove move = gson.fromJson(command.getMove(), ChessMove.class);

    try {
      gameService.makeMove(authToken, gameId, move);
      sendGameState(gameId);
      notifyPlayers(gameId, authToken + " made a move.");
    } catch (Exception e) {
      sendErrorMessage(session, "Invalid move: " + e.getMessage());
    }
  }

  private void handleLeave(Session session, UserGameCommand command) throws Exception {
    int gameId = command.getGameID();
    String authToken = command.getAuthToken();

    gameSessions.get(gameId).remove(session);
    notifyPlayers(gameId, authToken + " has left the game.");
  }

  private void handleResign(Session session, UserGameCommand command) throws Exception {
    int gameId = command.getGameID();
    String authToken = command.getAuthToken();

    gameService.resignGame(authToken, gameId);
    notifyPlayers(gameId, authToken + " has resigned from the game.");
    sendGameState(gameId);
  }

  private void sendGameState(Session session, int gameId) throws Exception {
    String authToken = gameSessions.get(gameId).get(session);
    ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
    message.setGame(gameService.getGameState(authToken, gameId));
    session.getRemote().sendString(gson.toJson(message));
  }

  private void sendGameState(Session session, int gameId) throws Exception {
    ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
    message.setGame(gameService.getGameState(gameId));
    session.getRemote().sendString(gson.toJson(message));
  }

  private void notifyPlayers(int gameId, String notificationMessage) throws Exception {
    ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
    message.setMessage(notificationMessage);
    String jsonMessage = gson.toJson(message);

    for (Session session : gameSessions.get(gameId).keySet()) {
      session.getRemote().sendString(jsonMessage);
    }
  }

  private void sendErrorMessage(Session session, String errorMessage) throws IOException {
    ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
    message.setErrorMessage(errorMessage);
    session.getRemote().sendString(gson.toJson(message));
  }
}