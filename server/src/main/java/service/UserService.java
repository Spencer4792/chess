package service;

import dataaccess.*;
import model.*;
import org.mindrot.jbcrypt.BCrypt;
import java.util.UUID;

public class UserService {
  private final DataAccess dataAccess;

  public UserService(DataAccess dataAccess) {
    this.dataAccess = dataAccess;
  }

  public AuthData register(UserData user) throws DataAccessException {
    if (user.username() == null || user.username().isEmpty() ||
            user.password() == null || user.password().isEmpty() ||
            user.email() == null || user.email().isEmpty()) {
      throw new DataAccessException("Error: bad request");
    }

    if (dataAccess.getUser(user.username()) != null) {
      throw new DataAccessException("Error: already taken");
    }

    String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
    UserData newUser = new UserData(user.username(), hashedPassword, user.email());
    dataAccess.createUser(newUser);

    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, user.username());
    dataAccess.createAuth(authData);

    return authData;
  }

  public AuthData login(String username, String password) throws DataAccessException {
    UserData user = dataAccess.getUser(username);
    if (user == null || !BCrypt.checkpw(password, user.password())) {
      throw new DataAccessException("Error: unauthorized");
    }

    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, username);
    dataAccess.createAuth(authData);

    return authData;
  }

  public void logout(String authToken) throws DataAccessException {
    if (dataAccess.getAuth(authToken) == null) {
      throw new DataAccessException("Error: unauthorized");
    }

    dataAccess.deleteAuth(authToken);
  }

  public void clearAll() throws DataAccessException {
    dataAccess.clear();
  }
}