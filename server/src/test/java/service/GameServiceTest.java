package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

  private GameService gameService;
  private DataAccess dataAccess;
  private String authToken;

  @BeforeEach
  void setUp() throws Exception {
    dataAccess = new MemoryDataAccess();
    gameService = new GameService(dataAccess);
    UserService userService = new UserService(dataAccess);
    UserData userData = new UserData("testUser", "password", "email@example.com");
    authToken = userService.register(userData).authToken();
  }

  @Test
  void testCreateGame() throws Exception {
    int gameId = gameService.createGame(authToken, "Test Game");
    assertTrue(gameId > 0);
  }

  @Test
  void testListGames() throws Exception {
    gameService.createGame(authToken, "Game 1");
    gameService.createGame(authToken, "Game 2");
    var games = gameService.listGames(authToken);
    assertEquals(2, games.size());
  }

  @Test
  void testJoinGame() throws Exception {
    int gameId = gameService.createGame(authToken, "Join Test");
    assertDoesNotThrow(() -> gameService.joinGame(authToken, gameId, chess.ChessGame.TeamColor.WHITE));
  }

  @Test
  void testJoinGameTwice() throws Exception {
    int gameId = gameService.createGame(authToken, "Double Join Test");
    gameService.joinGame(authToken, gameId, chess.ChessGame.TeamColor.WHITE);
    assertThrows(Exception.class, () -> gameService.joinGame(authToken, gameId, chess.ChessGame.TeamColor.WHITE));
  }
}