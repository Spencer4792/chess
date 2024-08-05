package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collection;

public class ServerFacade {

  private final String serverUrl;
  private final HttpClient client;

  public ServerFacade(String serverUrl) {
    this.serverUrl = serverUrl;
    this.client = HttpClient.newHttpClient();
  }

  public AuthData register(String username, String password, String email) throws Exception {
    var path = "/user";
    var request = new RegisterRequest(username, password, email);
    return makeRequest("POST", path, request, AuthData.class);
  }

  public AuthData login(String username, String password) throws Exception {
    var path = "/session";
    var request = new LoginRequest(username, password);
    return makeRequest("POST", path, request, AuthData.class);
  }

  public void logout(String authToken) throws Exception {
    var path = "/session";
    makeRequest("DELETE", path, null, null, authToken);
  }

  public int createGame(String authToken, String gameName) throws Exception {
    var path = "/game";
    var request = new CreateGameRequest(gameName);
    var response = makeRequest("POST", path, request, CreateGameResponse.class, authToken);
    return response.gameID();
  }

  public Collection<GameData> listGames(String authToken) throws Exception {
    var path = "/game";
    var response = makeRequest("GET", path, null, ListGamesResponse.class, authToken);
    return Arrays.asList(response.games());
  }

  private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws Exception {
    URI uri = URI.create(serverUrl + path);
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri);

    if (authToken != null) {
      requestBuilder.header("Authorization", authToken);
    }

    if (request != null) {
      var requestBody = new Gson().toJson(request);
      requestBuilder.header("Content-Type", "application/json");
      requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(requestBody));
    } else {
      requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
    }

    HttpResponse<String> response = client.send(requestBuilder.build(),
            HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() >= 300) {
      throw new Exception(response.body());
    }

    if (responseClass != null) {
      return new Gson().fromJson(response.body(), responseClass);
    }
    return null;
  }

  private record RegisterRequest(String username, String password, String email) {}
  private record LoginRequest(String username, String password) {}
  private record CreateGameRequest(String gameName) {}
  private record CreateGameResponse(int gameID) {}
  private record ListGamesResponse(GameData[] games) {}
  private record JoinGameRequest(String playerColor, int gameID) {}
}