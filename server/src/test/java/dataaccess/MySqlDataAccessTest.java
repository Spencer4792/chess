package dataaccess;

import model.UserData;
import model.GameData;
import model.AuthData;
import chess.ChessGame;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Collection;

public class MySqlDataAccessTest {
  private static MySqlDataAccess dataAccess;

  @BeforeAll
  static void setUp() throws DataAccessException {
    dataAccess = new MySqlDataAccess();
    dataAccess.clear();
  }

  @AfterEach
  void tearDown() throws DataAccessException {
    dataAccess.clear();
  }

  @Test
  void createUser_Positive() throws DataAccessException {
    UserData user = new UserData("testUser", "password", "test@example.com");
    dataAccess.createUser(user);
    UserData retrievedUser = dataAccess.getUser("testUser");
    assertNotNull(retrievedUser);
    assertEquals(user.username(), retrievedUser.username());
    assertEquals(user.email(), retrievedUser.email());
  }

  @Test
  void createUser_Negative_DuplicateUsername() {
    UserData user = new UserData("testUser", "password", "test@example.com");
    assertThrows(DataAccessException.class, () -> {
      dataAccess.createUser(user);
      dataAccess.createUser(user);
    });
  }

  @Test
  void createGame_Positive() throws DataAccessException {
    GameData game = new GameData(0, "white", "black", "TestGame", new ChessGame());
    dataAccess.createGame(game);
    Collection<GameData> games = dataAccess.listGames();
    assertEquals(1, games.size());
    GameData retrievedGame = games.iterator().next();
    assertEquals(game.gameName(), retrievedGame.gameName());
  }

  @Test
  void createAuth_Positive() throws DataAccessException {
    AuthData auth = new AuthData("testToken", "testUser");
    dataAccess.createAuth(auth);
    AuthData retrievedAuth = dataAccess.getAuth("testToken");
    assertNotNull(retrievedAuth);
    assertEquals(auth.authToken(), retrievedAuth.authToken());
    assertEquals(auth.username(), retrievedAuth.username());
  }

  @Test
  void deleteAuth_Positive() throws DataAccessException {
    AuthData auth = new AuthData("testToken", "testUser");
    dataAccess.createAuth(auth);
    dataAccess.deleteAuth("testToken");
    AuthData retrievedAuth = dataAccess.getAuth("testToken");
    assertNull(retrievedAuth);
  }

  @Test
  void updateGame_Positive() throws DataAccessException {
    GameData game = new GameData(0, "white", "black", "TestGame", new ChessGame());
    dataAccess.createGame(game);

    // Retrieve the game to get the correct ID
    Collection<GameData> games = dataAccess.listGames();
    assertFalse(games.isEmpty(), "No games found after creation");
    GameData createdGame = games.iterator().next();

    System.out.println("Created game: " + createdGame);

    GameData updatedGame = new GameData(createdGame.gameID(), "newWhite", "newBlack", "UpdatedGame", new ChessGame());
    dataAccess.updateGame(updatedGame);

    System.out.println("Updated game: " + updatedGame);

    GameData retrievedGame = dataAccess.getGame(createdGame.gameID());
    assertNotNull(retrievedGame, "Retrieved game is null");

    System.out.println("Retrieved game: " + retrievedGame);

    assertEquals(updatedGame.whiteUsername(), retrievedGame.whiteUsername());
    assertEquals(updatedGame.blackUsername(), retrievedGame.blackUsername());
    assertEquals(updatedGame.gameName(), retrievedGame.gameName());
  }
}