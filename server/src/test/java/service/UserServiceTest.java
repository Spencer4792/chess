package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

  private UserService userService;
  private DataAccess dataAccess;

  @BeforeEach
  void setUp() {
    dataAccess = new MemoryDataAccess();
    userService = new UserService(dataAccess);
  }

  @Test
  void testRegisterSuccess() throws Exception {
    UserData userData = new UserData("newUser", "password", "email@example.com");
    AuthData result = userService.register(userData);
    assertNotNull(result);
    assertEquals("newUser", result.username());
    assertNotNull(result.authToken());
  }

  @Test
  void testRegisterDuplicateUser() {
    UserData userData = new UserData("newUser", "password", "email@example.com");
    assertThrows(Exception.class, () -> {
      userService.register(userData);
      userService.register(userData);
    });
  }

  @Test
  void testLoginSuccess() throws Exception {
    UserData userData = new UserData("testUser", "password", "email@example.com");
    userService.register(userData);
    AuthData result = userService.login("testUser", "password");
    assertNotNull(result);
    assertEquals("testUser", result.username());
    assertNotNull(result.authToken());
  }

  @Test
  void testLoginFailure() {
    assertThrows(Exception.class, () -> userService.login("nonexistentUser", "password"));
  }

  @Test
  void testLogout() throws Exception {
    UserData userData = new UserData("logoutUser", "password", "email@example.com");
    AuthData auth = userService.register(userData);
    assertDoesNotThrow(() -> userService.logout(auth.authToken()));
    // Attempt to use the same authToken should now fail
    assertThrows(Exception.class, () -> userService.logout(auth.authToken()));
  }
}