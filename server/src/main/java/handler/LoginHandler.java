package handler;

import service.UserService;
import spark.Request;
import spark.Response;
import model.AuthData;

public class LoginHandler extends BaseHandler {
  private final UserService userService;

  public LoginHandler(UserService userService) {
    this.userService = userService;
  }

  @Override
  public Object handle(Request req, Response res) throws Exception {
    setResponseHeaders(res);
    var loginRequest = deserialize(req.body(), LoginRequest.class);
    try {
      AuthData authData = userService.login(loginRequest.username, loginRequest.password);
      res.status(200);
      return serialize(new LoginResult(authData.username(), authData.authToken()));
    } catch (Exception e) {
      res.status(401);
      return serialize(new ErrorResult(e.getMessage()));
    }
  }

  private record LoginRequest(String username, String password) {}
  private record LoginResult(String username, String authToken) {}
  private record ErrorResult(String message) {}
}