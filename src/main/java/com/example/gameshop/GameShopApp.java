package com.example.gameshop;

import javafx.application.Application;
import javafx.stage.Stage;
import com.example.gameshop.scenes.LoginScene;
import com.example.gameshop.models.User;

public class GameShopApp extends Application {
    private Stage primaryStage;
    private static User currentUser;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Game Shop");
        
        // Show login scene first
        new LoginScene(primaryStage);
        primaryStage.show();
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void main(String[] args) {
        launch(args);
    }
} 