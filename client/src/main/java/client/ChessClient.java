package client;

import model.AuthData;
import model.GameState;
import chess.ChessMove;
import ui.PreloginUI;
import ui.PostloginUI;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import com.google.gson.Gson;

import javax.websocket.*;
import java.net.URI;
import java.io.IOException;

@ClientEndpoint
public class ChessClient {
  private final ServerFacade server;
  private final PreloginUI preloginUI;
  private final PostloginUI postloginUI;
  private String authToken;
  private Session websocketSession;
  private final Gson gson = new Gson();

  public ChessClient(String serverUrl) {
    this.server = new ServerFacade(serverUrl);
    this.preloginUI = new PreloginUI(this);
    this.postloginUI = new PostloginUI(this);
  }

  public void run() {
    System.out.println("Welcome to the Chess Game!");
    preloginUI.display();
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  public String getAuthToken() {
    return authToken;
  }

  public ServerFacade getServer() {
    return server;
  }

  public void switchToPostlogin() {
    postloginUI.display();
  }

  public void logout() {
    try {
      server.logout(authToken);
    } catch (ClientException e) {
      System.out.println("Error during logout: " + e.getMessage());
    }
    authToken = null;
    disconnectWebSocket();
    preloginUI.display();
  }

  public void makeMove(int gameId, ChessMove move) throws Exception {
    UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameId);
    command.setMove(gson.toJson(move));
    sendWebSocketMessage(command);
  }

  public void joinGame(int gameId) throws Exception {
    connectWebSocket(gameId);
  }

  public void leaveGame(int gameId) throws Exception {
    UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameId);
    sendWebSocketMessage(command);
    disconnectWebSocket();
  }

  public void resignGame(int gameId) throws Exception {
    UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameId);
    sendWebSocketMessage(command);
  }

  private void connectWebSocket(int gameId) throws Exception {
    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    String uri = server.getServerUrl().replace("http", "ws") + "/ws";
    websocketSession = container.connectToServer(this, URI.create(uri));

    UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameId);
    sendWebSocketMessage(command);
  }

  private void disconnectWebSocket() {
    if (websocketSession != null && websocketSession.isOpen()) {
      try {
        websocketSession.close();
      } catch (IOException e) {
        System.out.println("Error closing WebSocket: " + e.getMessage());
      }
    }
  }

  private void sendWebSocketMessage(UserGameCommand command) throws Exception {
    if (websocketSession != null && websocketSession.isOpen()) {
      websocketSession.getBasicRemote().sendText(gson.toJson(command));
    } else {
      throw new Exception("WebSocket is not connected");
    }
  }

  @OnMessage
  public void onMessage(String message) {
    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
    switch (serverMessage.getServerMessageType()) {
      case LOAD_GAME:
        handleLoadGame(serverMessage);
        break;
      case NOTIFICATION:
        handleNotification(serverMessage);
        break;
      case ERROR:
        handleError(serverMessage);
        break;
    }
  }

  private void handleLoadGame(ServerMessage message) {
    chess.ChessGame chessGame = message.getGame();
    // Convert ChessGame to GameState or update UI directly with ChessGame
    postloginUI.updateGameState(new GameState(chessGame)); // Assuming GameState has a constructor that takes ChessGame
  }


  private void handleNotification(ServerMessage message) {
    postloginUI.showNotification(message.getMessage());
  }

  private void handleError(ServerMessage message) {
    postloginUI.showError(message.getErrorMessage());
  }

  public GameState getGameState(int gameId) throws ClientException {
    return server.getGameState(authToken, gameId);
  }

  public static void main(String[] args) {
    String serverUrl = "http://localhost:8080";
    if (args.length == 1) {
      serverUrl = args[0];
    }
    new ChessClient(serverUrl).run();
  }
}