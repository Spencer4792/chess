package handler;

import service.ClearService;
import spark.Request;
import spark.Response;

public class ClearApplicationHandler extends BaseHandler {
  private final ClearService clearService;

  public ClearApplicationHandler(ClearService clearService) {
    this.clearService = clearService;
  }

  @Override
  public Object handle(Request req, Response res) throws Exception {
    setResponseHeaders(res);
    try {
      clearService.clearApplication();
      res.status(200);
      return "{}";
    } catch (Exception e) {
      res.status(500);
      return serialize(new ErrorResult(e.getMessage()));
    }
  }

  private record ErrorResult(String message) {}
}