package client;

import org.junit.jupiter.api.*;
import server.Server;
import model.AuthData;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clear() throws Exception {
        facade.clear();
    }

    @Test
    void registerPositive() throws Exception {
        var authData = facade.register("player1", "password", "p1@email.com");
        assertNotNull(authData);
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void registerNegative() {
        assertThrows(Exception.class, () -> facade.register("", "password", "p1@email.com"));
    }

    @Test
    void loginPositive() throws Exception {
        facade.register("player1", "password", "p1@email.com");
        var authData = facade.login("player1", "password");
        assertNotNull(authData);
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void loginNegative() {
        assertThrows(Exception.class, () -> facade.login("nonexistent", "wrongpassword"));
    }

    @Test
    void logoutPositive() throws Exception {
        var authData = facade.register("player1", "password", "p1@email.com");
        assertDoesNotThrow(() -> facade.logout(authData.authToken()));
    }

    @Test
    void logoutNegative() {
        assertThrows(Exception.class, () -> facade.logout("invalidauthtoken"));
    }

    @Test
    void createGamePositive() throws Exception {
        var authData = facade.register("player1", "password", "p1@email.com");
        var gameId = facade.createGame(authData.authToken(), "Test Game");
        assertTrue(gameId > 0);
    }

    @Test
    void createGameNegative() {
        assertThrows(Exception.class, () -> facade.createGame("invalidauthtoken", "Test Game"));
    }

    @Test
    void listGamesPositive() throws Exception {
        var authData = facade.register("player1", "password", "p1@email.com");
        facade.createGame(authData.authToken(), "Test Game 1");
        facade.createGame(authData.authToken(), "Test Game 2");
        var games = facade.listGames(authData.authToken());
        assertEquals(2, games.size());
    }

    @Test
    void listGamesNegative() {
        assertThrows(Exception.class, () -> facade.listGames("invalidauthtoken"));
    }

    @Test
    void joinGamePositive() throws Exception {
        var authData = facade.register("player1", "password", "p1@email.com");
        var gameId = facade.createGame(authData.authToken(), "Test Game");
        assertDoesNotThrow(() -> facade.joinGame(authData.authToken(), gameId, "WHITE"));
    }

    @Test
    void joinGameNegative() {
        assertThrows(Exception.class, () -> facade.joinGame("invalidauthtoken", 1, "WHITE"));
    }
}