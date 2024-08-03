package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlDataAccessTest {

  private MySqlDataAccess dataAccess;

  @BeforeEach
  void setUp() throws DataAccessException {
    dataAccess = new MySqlDataAccess();
    dataAccess.clear();
  }

  @Test
  void createUserPositive() throws DataAccessException {
    UserData user = new UserData("testUser", "password", "test@example.com");
    dataAccess.createUser(user);
    UserData retrievedUser = dataAccess.getUser("testUser");
    assertNotNull(retrievedUser);
    assertEquals("testUser", retrievedUser.username());
  }

  @Test
  void createUserNegativeDuplicateUsername() {
    UserData user = new UserData("testUser", "password", "test@example.com");
    assertDoesNotThrow(() -> dataAccess.createUser(user));
    assertThrows(DataAccessException.class, () -> dataAccess.createUser(user));
  }

  @Test
  void getUserPositive() throws DataAccessException {
    UserData user = new UserData("testUser", "password", "test@example.com");
    dataAccess.createUser(user);
    UserData retrievedUser = dataAccess.getUser("testUser");
    assertNotNull(retrievedUser);
    assertEquals("testUser", retrievedUser.username());
  }

  @Test
  void getUserNegativeNonexistentUser() throws DataAccessException {
    UserData retrievedUser = dataAccess.getUser("nonexistentUser");
    assertNull(retrievedUser);
  }

  @Test
  void createGamePositive() throws DataAccessException {
    GameData game = new GameData(0, "white", "black", "Test Game", new ChessGame());
    dataAccess.createGame(game);
    Collection<GameData> games = dataAccess.listGames();
    assertEquals(1, games.size());
  }

  @Test
  void getGamePositive() throws DataAccessException {
    GameData game = new GameData(1, "white", "black", "Test Game", new ChessGame());
    dataAccess.createGame(game);
    GameData retrievedGame = dataAccess.getGame(1);
    assertNotNull(retrievedGame);
    assertEquals("Test Game", retrievedGame.gameName());
  }

  @Test
  void getGameNegativeNonexistentGame() throws DataAccessException {
    GameData retrievedGame = dataAccess.getGame(999);
    assertNull(retrievedGame);
  }

  @Test
  void listGamesPositive() throws DataAccessException {
    dataAccess.createGame(new GameData(0, "white1", "black1", "Game1", new ChessGame()));
    dataAccess.createGame(new GameData(0, "white2", "black2", "Game2", new ChessGame()));
    Collection<GameData> games = dataAccess.listGames();
    assertEquals(2, games.size());
  }

  @Test
  void createAuthPositive() throws DataAccessException {
    AuthData auth = new AuthData("token123", "testUser");
    dataAccess.createAuth(auth);
    AuthData retrievedAuth = dataAccess.getAuth("token123");
    assertNotNull(retrievedAuth);
    assertEquals("testUser", retrievedAuth.username());
  }

  @Test
  void getAuthPositive() throws DataAccessException {
    AuthData auth = new AuthData("token123", "testUser");
    dataAccess.createAuth(auth);
    AuthData retrievedAuth = dataAccess.getAuth("token123");
    assertNotNull(retrievedAuth);
    assertEquals("testUser", retrievedAuth.username());
  }

  @Test
  void getAuthNegativeNonexistentToken() throws DataAccessException {
    AuthData retrievedAuth = dataAccess.getAuth("nonexistentToken");
    assertNull(retrievedAuth);
  }

  @Test
  void deleteAuthPositive() throws DataAccessException {
    AuthData auth = new AuthData("token123", "testUser");
    dataAccess.createAuth(auth);
    dataAccess.deleteAuth("token123");
    AuthData retrievedAuth = dataAccess.getAuth("token123");
    assertNull(retrievedAuth);
  }

  @Test
  void deleteAuthNegativeNonexistentToken() {
    assertThrows(DataAccessException.class, () -> dataAccess.deleteAuth("nonexistentToken"));
  }

  @Test
  void updateGamePositive() throws DataAccessException {
    GameData game = new GameData(0, "white", "black", "Test Game", new ChessGame());
    dataAccess.createGame(game);
    Collection<GameData> games = dataAccess.listGames();
    GameData createdGame = games.iterator().next();

    GameData updatedGame = new GameData(createdGame.gameID(), "newWhite", "newBlack", "Updated Game", new ChessGame());
    dataAccess.updateGame(updatedGame);

    GameData retrievedGame = dataAccess.getGame(createdGame.gameID());
    assertEquals("Updated Game", retrievedGame.gameName());
    assertEquals("newWhite", retrievedGame.whiteUsername());
    assertEquals("newBlack", retrievedGame.blackUsername());
  }

  @Test
  void updateGameNegativeNonexistentGame() {
    GameData nonexistentGame = new GameData(999, "white", "black", "Nonexistent Game", new ChessGame());
    assertThrows(DataAccessException.class, () -> dataAccess.updateGame(nonexistentGame));
  }

  @Test
  void clearPositive() throws DataAccessException {
    dataAccess.createUser(new UserData("user1", "password1", "user1@example.com"));
    dataAccess.createGame(new GameData(0, "white", "black", "Game1", new ChessGame()));
    dataAccess.createAuth(new AuthData("token1", "user1"));

    dataAccess.clear();

    assertNull(dataAccess.getUser("user1"));
    assertTrue(dataAccess.listGames().isEmpty());
    assertNull(dataAccess.getAuth("token1"));
  }
}