package service;

import dataaccess.*;
import model.*;
import java.util.UUID;

public class UserService {
  private final DataAccess dataAccess;

  public UserService(DataAccess dataAccess) {
    this.dataAccess = dataAccess;
  }

  public AuthData register(String username, String password, String email) throws DataAccessException {
    if (username == null || username.isEmpty() || password == null || password.isEmpty() || email == null || email.isEmpty()) {
      throw new DataAccessException("Error: bad request");
    }

    if (dataAccess.getUser(username) != null) {
      throw new DataAccessException("Error: already taken");
    }

    UserData newUser = new UserData(username, password, email);
    dataAccess.addUser(newUser);

    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, username);
    dataAccess.addAuth(authData);

    return authData;
  }

  public AuthData login(String username, String password) throws DataAccessException {
    UserData user = dataAccess.getUser(username);
    if (user == null || !user.password().equals(password)) {
      throw new DataAccessException("Error: unauthorized");
    }

    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, username);
    dataAccess.addAuth(authData);

    return authData;
  }

  public void logout(String authToken) throws DataAccessException {
    if (dataAccess.getAuth(authToken) == null) {
      throw new DataAccessException("Error: unauthorized");
    }

    dataAccess.deleteAuth(authToken);
  }
}