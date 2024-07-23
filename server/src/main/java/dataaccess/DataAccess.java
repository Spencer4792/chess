package dataaccess;

import model.UserData;
import model.GameData;
import model.AuthData;

import java.util.List;

public interface DataAccess {
  void clear() throws DataAccessException;
  void addUser(UserData user) throws DataAccessException;
  UserData getUser(String username) throws DataAccessException;
  void addGame(GameData game) throws DataAccessException;
  GameData getGame(int gameID) throws DataAccessException;
  List<GameData> listGames() throws DataAccessException;
  void updateGame(GameData game) throws DataAccessException;
  void addAuth(AuthData auth) throws DataAccessException;
  AuthData getAuth(String authToken) throws DataAccessException;
  void deleteAuth(String authToken) throws DataAccessException;
}