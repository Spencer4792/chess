package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static final String DATABASE_NAME;
    private static final String USER;
    private static final String PASSWORD;
    private static final String CONNECTION_URL;

    static {
        try (var propStream = DatabaseManager.class.getResourceAsStream("/db.properties")) {
            if (propStream == null) {
                throw new Exception("Unable to load db.properties");
            }
            Properties props = new Properties();
            props.load(propStream);
            DATABASE_NAME = props.getProperty("db.name");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");
            String host = props.getProperty("db.host");
            String port = props.getProperty("db.port");
            CONNECTION_URL = String.format("jdbc:mysql://%s:%s", host, port);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to process db.properties: " + ex.getMessage());
        }
    }

    public static void createDatabase() throws DataAccessException {
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create database: " + e.getMessage());
        }
    }

    public static void createTables() throws DataAccessException {
        try (Connection conn = getConnection()) {
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(255) PRIMARY KEY, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL)";

            String createAuthTable = "CREATE TABLE IF NOT EXISTS auth_tokens (" +
                    "auth_token VARCHAR(255) PRIMARY KEY, " +
                    "username VARCHAR(255) NOT NULL)";

            String createGamesTable = "CREATE TABLE IF NOT EXISTS games (" +
                    "game_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "white_username VARCHAR(255), " +
                    "black_username VARCHAR(255), " +
                    "game_name VARCHAR(255) NOT NULL, " +
                    "game_state TEXT NOT NULL)";

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(createUsersTable);
                stmt.executeUpdate(createAuthTable);
                stmt.executeUpdate(createGamesTable);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating tables: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws DataAccessException {
        try {
            Connection conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            conn.setCatalog(DATABASE_NAME);
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException("Unable to connect to database: " + e.getMessage());
        }
    }
}