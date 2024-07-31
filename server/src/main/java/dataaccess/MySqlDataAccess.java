package dataaccess;

import model.UserData;
import model.GameData;
import model.AuthData;
import chess.ChessGame;
import com.google.gson.Gson;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class MySqlDataAccess implements DataAccess {
  private final Gson gson = new Gson();

  @Override
  public void clear() throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      try (Statement stmt = conn.createStatement()) {
        stmt.executeUpdate("DELETE FROM auth_tokens");
        stmt.executeUpdate("DELETE FROM games");
        stmt.executeUpdate("DELETE FROM users");
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error clearing database: " + e.getMessage());
    }
  }

  @Override
  public void createUser(UserData user) throws DataAccessException {
    String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
    try (Connection conn = DatabaseManager.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, password, email) VALUES (?, ?, ?)")) {
        stmt.setString(1, user.username());
        stmt.setString(2, hashedPassword);
        stmt.setString(3, user.email());
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error creating user: " + e.getMessage());
    }
  }