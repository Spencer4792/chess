package handler;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

public abstract class BaseHandler implements Route {
  protected static final Gson GSON = new Gson();

  protected String serialize(Object obj) {
    return GSON.toJson(obj);
  }

  protected <T> T deserialize(String json, Class<T> classOfT) {
    return GSON.fromJson(json, classOfT);
  }

  protected void setResponseHeaders(Response res) {
    res.type("application/json");
  }
}