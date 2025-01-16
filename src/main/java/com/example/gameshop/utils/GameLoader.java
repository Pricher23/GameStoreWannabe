package com.example.gameshop.utils;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import com.example.gameshop.models.Game;
import com.example.gameshop.dao.DatabaseManager;
import java.sql.SQLException;
import java.util.List;

public class GameLoader {
    private DatabaseManager dbManager;

    public GameLoader() {
        this.dbManager = new DatabaseManager();
    }

    public void loadGamesIntoList(ListView<Game> gameList) {
        Thread loadingThread = new Thread(() -> {
            try {
                List<Game> games = dbManager.getAllGames();
                Platform.runLater(() -> {
                    gameList.getItems().clear();
                    gameList.getItems().addAll(games);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    // Handle error, maybe show an alert
                    System.err.println("Failed to load games: " + e.getMessage());
                });
            }
        });
        loadingThread.start();
    }

    public void refreshGameList(ListView<Game> gameList) {
        loadGamesIntoList(gameList);
    }
}