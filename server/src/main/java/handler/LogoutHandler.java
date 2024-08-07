package handler;

import service.UserService;
import spark.Request;
import spark.Response;

public class LogoutHandler extends BaseHandler {
  private final UserService userService;

  public LogoutHandler(UserService userService) {
    this.userService = userService;
  }

  @Override
  public Object handle(Request req, Response res) throws Exception {
    setResponseHeaders(res);
    String authToken = req.headers("Authorization");
    try {
      userService.logout(authToken);
      res.status(200);
      return "{}";
    } catch (Exception e) {
      res.status(401);
      return serialize(new ErrorResult(e.getMessage()));
    }
  }

  private record ErrorResult(String message) {}
}