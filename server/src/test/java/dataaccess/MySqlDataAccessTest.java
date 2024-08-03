package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlDataAccessTest {

  private MySqlDataAccess dataAccess;

  @BeforeEach
  void setUp() throws DataAccessException {
    dataAccess = new MySqlDataAccess();
    dataAccess.clear();
  }

  private UserData createTestUser(String username) throws DataAccessException {
    UserData user = new UserData(username, "password", username + "@example.com");
    dataAccess.createUser(user);
    return user;
  }

  private AuthData createTestAuth(String token, String username) throws DataAccessException {
    AuthData auth = new AuthData(token, username);
    dataAccess.createAuth(auth);
    return auth;
  }

  @ParameterizedTest
  @ValueSource(strings = {"testUser", "anotherUser"})
  void createUserPositive(String username) throws DataAccessException {
    createTestUser(username);
    UserData retrievedUser = dataAccess.getUser(username);
    assertNotNull(retrievedUser);
    assertEquals(username, retrievedUser.username());
  }

  @Test
  void createUserNegativeDuplicateUsername() {
    assertDoesNotThrow(() -> createTestUser("testUser"));
    assertThrows(DataAccessException.class, () -> createTestUser("testUser"));
  }

  @Test
  void getUserPositive() throws DataAccessException {
    createTestUser("testUser");
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

  @ParameterizedTest
  @ValueSource(strings = {"token123", "anotherToken"})
  void createAuthPositive(String token) throws DataAccessException {
    createTestAuth(token, "testUser");
    AuthData retrievedAuth = dataAccess.getAuth(token);
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
    createTestAuth("token123", "testUser");
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
    createTestUser("user1");
    dataAccess.createGame(new GameData(0, "white", "black", "Game1", new ChessGame()));
    createTestAuth("token1", "user1");

    dataAccess.clear();

    assertNull(dataAccess.getUser("user1"));
    assertTrue(dataAccess.listGames().isEmpty());
    assertNull(dataAccess.getAuth("token1"));
  }

  @Test
  void listGamesNegativeEmptyDatabase() throws DataAccessException {
    Collection<GameData> games = dataAccess.listGames();
    assertTrue(games.isEmpty());
  }

  @Test
  void createAuthNegativeDuplicateToken() throws DataAccessException {
    createTestAuth("duplicateToken", "user1");
    assertThrows(DataAccessException.class, () -> createTestAuth("duplicateToken", "user2"));
  }

  @Test
  void createMultipleGamesPositive() throws DataAccessException {
    GameData game1 = new GameData(0, "white1", "black1", "Test Game 1", new ChessGame());
    GameData game2 = new GameData(0, "white2", "black2", "Test Game 2", new ChessGame());

    assertDoesNotThrow(() -> dataAccess.createGame(game1));
    assertDoesNotThrow(() -> dataAccess.createGame(game2));

    Collection<GameData> games = dataAccess.listGames();
    assertEquals(2, games.size());
  }

  @Test
  void createGameWithSameNamePositive() throws DataAccessException {
    GameData game1 = new GameData(0, "white1", "black1", "Duplicate Name", new ChessGame());
    GameData game2 = new GameData(0, "white2", "black2", "Duplicate Name", new ChessGame());

    assertDoesNotThrow(() -> dataAccess.createGame(game1));
    assertDoesNotThrow(() -> dataAccess.createGame(game2));

    Collection<GameData> games = dataAccess.listGames();
    assertEquals(2, games.size());
  }

  @Test
  void createGameNegativeNullGameName() {
    GameData invalidGame = new GameData(0, "white", "black", null, new ChessGame());
    assertThrows(DataAccessException.class, () -> dataAccess.createGame(invalidGame));
  }

  @Test
  void deleteAuthNegativeNullToken() {
    assertThrows(DataAccessException.class, () -> dataAccess.deleteAuth(null));
  }

  @Test
  void listGamesAfterClear() throws DataAccessException {
    dataAccess.createGame(new GameData(0, "white", "black", "Game1", new ChessGame()));
    dataAccess.clear();
    Collection<GameData> games = dataAccess.listGames();
    assertTrue(games.isEmpty());
  }

  @Test
  void createMultipleUsersPositive() throws DataAccessException {
    createTestUser("user1");
    createTestUser("user2");

    assertNotNull(dataAccess.getUser("user1"));
    assertNotNull(dataAccess.getUser("user2"));
  }
}