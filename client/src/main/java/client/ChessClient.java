package client;

import ui.PreloginUI;
import ui.PostloginUI;

public class ChessClient {
  private final ServerFacade server;
  private final PreloginUI preloginUI;
  private final PostloginUI postloginUI;
  private String authToken;

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
    authToken = null;
    preloginUI.display();
  }

  public static void main(String[] args) {
    String serverUrl = "http://localhost:8080";
    if (args.length == 1) {
      serverUrl = args[0];
    }
    new ChessClient(serverUrl).run();
  }
}