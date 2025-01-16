package com.example.gameshop.scenes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.example.gameshop.dao.DatabaseManager;
import com.example.gameshop.models.User;
import com.example.gameshop.utils.InputValidator;
import java.sql.SQLException;

public class RegisterScene {
    private Stage stage;
    private DatabaseManager dbManager;

    public RegisterScene(Stage stage) {
        this.stage = stage;
        this.dbManager = new DatabaseManager();
        createRegisterScene();
    }

    private void createRegisterScene() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #2b2b2b;");

        Label titleLabel = new Label("Register New Account");
        titleLabel.setStyle("-fx-font-size: 24; -fx-text-fill: white;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(200);
        usernameField.getStyleClass().add("dark-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(200);
        passwordField.getStyleClass().add("dark-field");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setMaxWidth(200);
        confirmPasswordField.getStyleClass().add("dark-field");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setMaxWidth(200);
        emailField.getStyleClass().add("dark-field");

        Button registerButton = new Button("Register");
        registerButton.setMaxWidth(200);
        registerButton.getStyleClass().add("accent-button");

        Button backButton = new Button("Back to Login");
        backButton.setMaxWidth(200);
        backButton.getStyleClass().add("secondary-button");

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #ff6b6b;");

        registerButton.setOnAction(e -> handleRegister(
            usernameField.getText(),
            passwordField.getText(),
            confirmPasswordField.getText(),
            emailField.getText(),
            errorLabel
        ));

        backButton.setOnAction(e -> new LoginScene(stage));

        layout.getChildren().addAll(
            titleLabel,
            usernameField,
            emailField,
            passwordField,
            confirmPasswordField,
            registerButton,
            backButton,
            errorLabel
        );

        Scene scene = new Scene(layout, 300, 400);
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        stage.setScene(scene);
    }

    private void handleRegister(String username, String password, 
                              String confirmPassword, String email, 
                              Label errorLabel) {
        // Validate input
        if (!InputValidator.isValidUsername(username)) {
            errorLabel.setText("Invalid username format");
            return;
        }

        if (!InputValidator.isValidPassword(password)) {
            errorLabel.setText("Invalid password format");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match");
            return;
        }

        if (!InputValidator.isValidEmail(email)) {
            errorLabel.setText("Invalid email format");
            return;
        }

        try {
            if (dbManager.getUserByUsername(username) != null) {
                errorLabel.setText("Username already exists");
                return;
            }

            User newUser = new User(username, password, email);
            dbManager.createUser(newUser);

            showAlert("Success", "Registration successful! Please login.");
            new LoginScene(stage);

        } catch (SQLException ex) {
            errorLabel.setText("Registration failed: " + ex.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 