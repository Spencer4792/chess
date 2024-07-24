package handler;

import service.UserService;
import spark.Request;
import spark.Response;
import model.AuthData;

public class RegisterHandler extends BaseHandler {
  private final UserService userService;

  public RegisterHandler(UserService userService) {
    this.userService = userService;
  }

  @Override
  public Object handle(Request req, Response res) throws Exception {
    setResponseHeaders(res);
    var registerRequest = deserialize(req.body(), RegisterRequest.class);
    try {
      AuthData authData = userService.register(registerRequest.username, registerRequest.password, registerRequest.email);
      res.status(200);
      return serialize(new RegisterResult(authData.username(), authData.authToken()));
    } catch (Exception e) {
      res.status(400);
      return serialize(new ErrorResult(e.getMessage()));
    }
  }

  private record RegisterRequest(String username, String password, String email) {}
  private record RegisterResult(String username, String authToken) {}
  private record ErrorResult(String message) {}
}