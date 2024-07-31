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

  @Override
  public UserData getUser(String username) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
        stmt.setString(1, username);
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
          }
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error getting user: " + e.getMessage());
    }
    return null;
  }

  @Override
  public void createGame(GameData game) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO games (white_username, black_username, game_name, game_state) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, game.whiteUsername());
        stmt.setString(2, game.blackUsername());
        stmt.setString(3, game.gameName());
        stmt.setString(4, gson.toJson(game.game()));
        stmt.executeUpdate();
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            game = new GameData(generatedKeys.getInt(1), game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
          }
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error creating game: " + e.getMessage());
    }
  }

  @Override
  public GameData getGame(int gameId) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM games WHERE game_id = ?")) {
        stmt.setInt(1, gameId);
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            return new GameData(
                    rs.getInt("game_id"),
                    rs.getString("white_username"),
                    rs.getString("black_username"),
                    rs.getString("game_name"),
                    gson.fromJson(rs.getString("game_state"), ChessGame.class)
            );
          }
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error getting game: " + e.getMessage());
    }
    return null;
  }

  @Override
  public Collection<GameData> listGames() throws DataAccessException {
    Collection<GameData> games = new ArrayList<>();
    try (Connection conn = DatabaseManager.getConnection()) {
      try (Statement stmt = conn.createStatement()) {
        try (ResultSet rs = stmt.executeQuery("SELECT * FROM games")) {
          while (rs.next()) {
            games.add(new GameData(
                    rs.getInt("game_id"),
                    rs.getString("white_username"),
                    rs.getString("black_username"),
                    rs.getString("game_name"),
                    gson.fromJson(rs.getString("game_state"), ChessGame.class)
            ));
          }
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error listing games: " + e.getMessage());
    }
    return games;
  }

  @Override
  public void updateGame(GameData game) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement("UPDATE games SET white_username = ?, black_username = ?, game_name = ?, game_state = ? WHERE game_id = ?")) {
        stmt.setString(1, game.whiteUsername());
        stmt.setString(2, game.blackUsername());
        stmt.setString(3, game.gameName());
        stmt.setString(4, gson.toJson(game.game()));
        stmt.setInt(5, game.gameID());
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error updating game: " + e.getMessage());
    }
  }

  @Override
  public void createAuth(AuthData auth) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO auth_tokens (auth_token, username) VALUES (?, ?)")) {
        stmt.setString(1, auth.authToken());
        stmt.setString(2, auth.username());
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error creating auth token: " + e.getMessage());
    }
  }

  @Override
  public AuthData getAuth(String authToken) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM auth_tokens WHERE auth_token = ?")) {
        stmt.setString(1, authToken);
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            return new AuthData(rs.getString("auth_token"), rs.getString("username"));
          }
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error getting auth token: " + e.getMessage());
    }
    return null;
  }

  @Override
  public void deleteAuth(String authToken) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM auth_tokens WHERE auth_token = ?")) {
        stmt.setString(1, authToken);
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error deleting auth token: " + e.getMessage());
    }
  }
}