package handler;

import service.UserService;
import spark.Request;
import spark.Response;
import model.AuthData;
import model.UserData;
import dataaccess.DataAccessException;

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
      if (registerRequest.username == null || registerRequest.password == null || registerRequest.email == null) {
        res.status(400);
        return serialize(new ErrorResult("Error: bad request"));
      }

      UserData userData = new UserData(registerRequest.username, registerRequest.password, registerRequest.email);
      AuthData authData = userService.register(userData);
      res.status(200);
      return serialize(new RegisterResult(authData.username(), authData.authToken()));
    } catch (DataAccessException e) {
      if (e.getMessage().contains("already taken")) {
        res.status(403);
        return serialize(new ErrorResult("Error: already taken"));
      } else {
        res.status(500);
        return serialize(new ErrorResult("Error: " + e.getMessage()));
      }
    } catch (Exception e) {
      res.status(500);
      return serialize(new ErrorResult("Error: " + e.getMessage()));
    }
  }

  private record RegisterRequest(String username, String password, String email) {}
  private record RegisterResult(String username, String authToken) {}
  private record ErrorResult(String message) {}
}