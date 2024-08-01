package dataaccess;

import model.*;
import java.util.*;

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
  public void createUser(UserData user) throws DataAccessException {
    if (users.containsKey(user.username())) {
      throw new DataAccessException("Username already exists");
    }
    users.put(user.username(), user);
  }

  @Override
  public UserData getUser(String username) throws DataAccessException {
    return users.get(username);
  }

  @Override
  public void createGame(GameData game) throws DataAccessException {
    if (games.containsKey(game.gameID())) {
      throw new DataAccessException("Game ID already exists");
    }
    games.put(game.gameID(), game);
  }

  @Override
  public GameData getGame(int gameID) throws DataAccessException {
    return games.get(gameID);
  }

  @Override
  public Collection<GameData> listGames() throws DataAccessException {
    return new ArrayList<>(games.values());
  }

  @Override
  public void updateGame(GameData game) throws DataAccessException {
    if (!games.containsKey(game.gameID())) {
      throw new DataAccessException("Game not found");
    }
    games.put(game.gameID(), game);
  }

  @Override
  public void createAuth(AuthData auth) throws DataAccessException {
    auths.put(auth.authToken(), auth);
  }

  @Override
  public AuthData getAuth(String authToken) throws DataAccessException {
    return auths.get(authToken);
  }

  @Override
  public void deleteAuth(String authToken) throws DataAccessException {
    auths.remove(authToken);
  }
}