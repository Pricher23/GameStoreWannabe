package com.example.gameshop.dao;

import com.example.gameshop.models.User;
import com.example.gameshop.models.Game;
import com.example.gameshop.models.GameKey;
import com.example.gameshop.models.UserGame;
import com.example.gameshop.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
} 