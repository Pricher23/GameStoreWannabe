package com.example.gameshop.dao;

import com.example.gameshop.models.User;
import com.example.gameshop.models.Game;
import com.example.gameshop.models.GameKey;
import com.example.gameshop.models.UserGame;
import com.example.gameshop.utils.DatabaseConnection;
import com.example.gameshop.models.GameDetails;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashSet;
import com.example.gameshop.utils.DatabaseValidator;

public class DatabaseManager {
    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email")
                );
                user.setUserId(rs.getInt("user_id"));
                user.setRole(rs.getString("role"));
                user.setBalance(rs.getDouble("balance"));
                return user;
            }
        }
        return null;
    }

    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, email, role, balance) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getRole());
            pstmt.setDouble(5, user.getBalance());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email")
                );
                user.setUserId(rs.getInt("user_id"));
                user.setRole(rs.getString("role"));
                user.setBalance(rs.getDouble("balance"));
                users.add(user);
            }
        }
        return users;
    }

    public void updateUserRole(int userId, String newRole) throws SQLException {
        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newRole);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public void updateUserBalance(int userId, double newBalance) throws SQLException {
        String sql = "UPDATE users SET balance = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newBalance);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public List<Game> getAllGames() throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Game game = new Game(
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getDouble("price")
                );
                game.setGameId(rs.getInt("game_id"));
                games.add(game);
            }
        }
        return games;
    }

    public void createGame(Game game) throws SQLException {
        String sql = "INSERT INTO games (title, description, price) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, game.getTitle());
            pstmt.setString(2, game.getDescription());
            pstmt.setDouble(3, game.getPrice());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    game.setGameId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public GameKey getAvailableGameKey(int gameId) throws SQLException {
        String sql = "SELECT * FROM game_keys WHERE game_id = ? AND is_sold = false LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                GameKey key = new GameKey(
                    rs.getInt("game_id"),
                    rs.getString("key_value")
                );
                key.setKeyId(rs.getInt("key_id"));
                return key;
            }
        }
        return null;
    }

    public void assignGameKeyToUser(int keyId, int userId) throws SQLException {
        String sql = "UPDATE game_keys SET is_sold = true, user_id = ? WHERE key_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, keyId);
            stmt.executeUpdate();
        }
    }

    public void addGameKey(GameKey gameKey) throws SQLException {
        String sql = "INSERT INTO game_keys (game_id, key_value, is_sold) VALUES (?, ?, false)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, gameKey.getGameId());
            pstmt.setString(2, gameKey.getKeyValue());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    gameKey.setKeyId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<UserGame> getUserGames(int userId) throws SQLException {
        List<UserGame> userGames = new ArrayList<>();
        String sql = """
            SELECT g.*, gk.* 
            FROM games g 
            JOIN game_keys gk ON g.game_id = gk.game_id 
            WHERE gk.user_id = ?
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Game game = new Game(
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getDouble("price")
                );
                game.setGameId(rs.getInt("game_id"));
                
                GameKey key = new GameKey(
                    rs.getInt("game_id"),
                    rs.getString("key_value")
                );
                key.setKeyId(rs.getInt("key_id"));
                
                userGames.add(new UserGame(game, key));
            }
        }
        return userGames;
    }

    public boolean userOwnsGame(int userId, int gameId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM game_keys WHERE user_id = ? AND game_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, gameId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public double getUserBalance(int userId) throws SQLException {
        String sql = "SELECT balance FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        }
        return 0.0;
    }

    public List<User> getUserFriends(int userId) throws SQLException {
        List<User> friends = new ArrayList<>();
        String sql = "SELECT u.* FROM users u " +
                     "JOIN friends f ON u.user_id = f.friend_id " +
                     "WHERE f.user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                friends.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getDouble("balance")
                ));
            }
        }
        return friends;
    }

    public void addFriend(int userId, int friendId) throws SQLException {
        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, friendId);
            pstmt.executeUpdate();
        }
    }

    public void saveSteamGames(int userId, List<Game> games) throws SQLException {
        String sql = "INSERT INTO steam_games (user_id, app_id, name, developer, publisher, genre, description, price, playtime_minutes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "playtime_minutes = VALUES(playtime_minutes)";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (Game game : games) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, game.getAppId());
                pstmt.setString(3, game.getName());
                pstmt.setString(4, game.getDetails().getDeveloper());
                pstmt.setString(5, game.getDetails().getPublisher());
                pstmt.setString(6, game.getDetails().getGenre());
                pstmt.setString(7, game.getDetails().getDescription());
                pstmt.setDouble(8, game.getDetails().getPrice());
                pstmt.setInt(9, game.getPlaytimeMinutes());
                
                pstmt.executeUpdate();
            }
        }
    }

    public void updateUserSteamId(int userId, String steamId) throws SQLException {
        String sql = "UPDATE users SET steam_id = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, steamId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    public List<Game> getUserSteamGames(int userId) throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM steam_games WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                GameDetails details = new GameDetails(
                    rs.getString("developer"),
                    rs.getString("publisher"),
                    rs.getString("genre"),
                    rs.getString("description"),
                    rs.getDouble("price")
                );
                
                Game game = new Game(rs.getString("name"), details, rs.getInt("playtime_minutes"));
                game.setAppId(rs.getInt("app_id"));
                games.add(game);
            }
        }
        return games;
    }

    public void importSteamGames(int userId, List<Game> steamGames) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // First, delete all existing Steam games for this user
                String deleteSql = "DELETE FROM steam_games WHERE user_id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, userId);
                    deleteStmt.executeUpdate();
                }

                // Create a Set to track unique game names
                Set<String> addedGames = new HashSet<>();

                String insertSql = "INSERT INTO steam_games (user_id, app_id, name, developer, publisher, " +
                                 "genre, description, price, playtime_minutes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    for (Game game : steamGames) {
                        // Validate game name and skip duplicates
                        String validatedName = DatabaseValidator.validateString(game.getTitle(), "Game Title", 100);
                        if (validatedName.equals("Unknown Game Title") || !addedGames.add(validatedName)) {
                            continue;
                        }

                        // Validate and set each field
                        insertStmt.setInt(1, userId);
                        insertStmt.setInt(2, DatabaseValidator.validateInteger(game.getAppId(), "App ID"));
                        insertStmt.setString(3, validatedName);
                        insertStmt.setString(4, DatabaseValidator.validateString(
                            game.getDetails().getDeveloper(), "Developer", 50));
                        insertStmt.setString(5, DatabaseValidator.validateString(
                            game.getDetails().getPublisher(), "Publisher", 50));
                        insertStmt.setString(6, DatabaseValidator.validateString(
                            game.getDetails().getGenre(), "Genre", 30));
                        insertStmt.setString(7, DatabaseValidator.validateString(
                            game.getDetails().getDescription(), "Description", 500));
                        insertStmt.setDouble(8, DatabaseValidator.validatePrice(game.getPrice()));
                        insertStmt.setInt(9, DatabaseValidator.validateInteger(
                            game.getPlaytimeMinutes(), "Playtime"));

                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
} 