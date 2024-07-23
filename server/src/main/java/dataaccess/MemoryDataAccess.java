package dataaccess;

import model.UserData;
import model.GameData;
import model.AuthData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {
  private final Map<String, UserData> users = new HashMap<>();
  private final Map<Integer, GameData> games = new HashMap<>();
  private final Map<String, AuthData> auths = new HashMap<>();

  @Override
  public void clear() {
    users.clear();
    games.clear();
    auths.clear();
  }

  @Override
  public void addUser(UserData user) throws DataAccessException {
    if (users.containsKey(user.username())) {
      throw new DataAccessException("User already exists");
    }
    users.put(user.username(), user);
  }

  @Override
  public UserData getUser(String username) throws DataAccessException {
    return users.get(username);
  }

  @Override
  public void addGame(GameData game) throws DataAccessException {
    if (games.containsKey(game.gameID())) {
      throw new DataAccessException("Game already exists");
    }
    games.put(game.gameID(), game);
  }

  @Override
  public GameData getGame(int gameID) throws DataAccessException {
    return games.get(gameID);
  }

  @Override
  public List<GameData> listGames() {
    return new ArrayList<>(games.values());
  }

  @Override
  public void updateGame(GameData game) throws DataAccessException {
    if (!games.containsKey(game.gameID())) {
      throw new DataAccessException("Game does not exist");
    }
    games.put(game.gameID(), game);
  }

  @Override
  public void addAuth(AuthData auth) throws DataAccessException {
    if (auths.containsKey(auth.authToken())) {
      throw new DataAccessException("Auth token already exists");
    }
    auths.put(auth.authToken(), auth);
  }

  @Override
  public AuthData getAuth(String authToken) throws DataAccessException {
    return auths.get(authToken);
  }

  @Override
  public void deleteAuth(String authToken) throws DataAccessException {
    if (!auths.containsKey(authToken)) {
      throw new DataAccessException("Auth token does not exist");
    }
    auths.remove(authToken);
  }
}