package com.example.gameshop.scenes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.example.gameshop.GameShopApp;
import com.example.gameshop.dao.DatabaseManager;
import com.example.gameshop.models.User;
import java.sql.SQLException;

public class LoginScene {
    private Stage stage;
    private DatabaseManager dbManager;

    public LoginScene(Stage stage) {
        this.stage = stage;
        this.dbManager = new DatabaseManager();
        createLoginScene();
    }

    private void createLoginScene() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #2b2b2b;");

        Label titleLabel = new Label("Game Shop Login");
        titleLabel.setStyle("-fx-font-size: 24; -fx-text-fill: white;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(200);
        usernameField.getStyleClass().add("dark-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(200);
        passwordField.getStyleClass().add("dark-field");

        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(200);
        loginButton.getStyleClass().add("accent-button");

        Button registerButton = new Button("Register");
        registerButton.setMaxWidth(200);
        registerButton.getStyleClass().add("secondary-button");

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #ff6b6b;");

        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), 
                                               passwordField.getText(), 
                                               errorLabel));

        registerButton.setOnAction(e -> showRegisterScene());

        layout.getChildren().addAll(
            titleLabel,
            usernameField,
            passwordField,
            loginButton,
            registerButton,
            errorLabel
        );

        Scene scene = new Scene(layout, 300, 400);
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        stage.setScene(scene);
    }

    private void handleLogin(String username, String password, Label errorLabel) {
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            return;
        }

        try {
            User user = dbManager.getUserByUsername(username);
            if (user != null && user.getPassword().equals(password)) {
                GameShopApp.setCurrentUser(user);
                
                // Debug print to check role
                System.out.println("User role: " + user.getRole());
                
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    new AdminScene(stage);
                } else {
                    new StoreScene(stage);
                }
            } else {
                errorLabel.setText("Invalid username or password");
            }
        } catch (SQLException ex) {
            errorLabel.setText("Database error: " + ex.getMessage());
        }
    }

    private void showRegisterScene() {
        new RegisterScene(stage);
    }
} 