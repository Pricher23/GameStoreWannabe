package com.example.gameshop.scenes;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.stage.Stage;
import com.example.gameshop.models.User;
import com.example.gameshop.GameShopApp;
import com.example.gameshop.utils.ThreadPool;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminScene {
    private Scene scene;
    private TableView<User> userTable;
    private ProgressIndicator loadingIndicator;
    private Stage stage;

    public AdminScene(Stage stage) {
        this.stage = stage;
        createScene();
    }

    public Scene getScene() {
        return scene;
    }

    private void createScene() {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 20;");

        // Admin header
        Label adminLabel = new Label("Admin");
        adminLabel.setStyle("-fx-text-fill: #4b6eaf; -fx-font-size: 24px;");

        // Buttons
        Button addGameBtn = new Button("Add Game");
        Button addGameKeyBtn = new Button("Add Game Key");
        Button viewUsersBtn = new Button("View Users");
        Button viewGamesBtn = new Button("View Games");

        // Style buttons
        String buttonStyle = "-fx-background-color: transparent; -fx-text-fill: #4b6eaf; " +
                           "-fx-border-color: #4b6eaf; -fx-border-radius: 3; " +
                           "-fx-font-size: 14px; -fx-padding: 5 10;";
        addGameBtn.setStyle(buttonStyle);
        addGameKeyBtn.setStyle(buttonStyle);
        viewUsersBtn.setStyle(buttonStyle);
        viewGamesBtn.setStyle(buttonStyle);

        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.getChildren().addAll(addGameBtn, addGameKeyBtn, viewUsersBtn, viewGamesBtn);

        // User table
        userTable = new TableView<>();
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        TableColumn<User, Double> balanceCol = new TableColumn<>("Balance");

        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));

        userTable.getColumns().addAll(usernameCol, emailCol, roleCol, balanceCol);

        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #4b6eaf;");
        loadingIndicator.setVisible(false);

        layout.getChildren().addAll(adminLabel, buttonBox, userTable, loadingIndicator);
        scene = new Scene(layout, 800, 600);

        // Add event handlers
        viewUsersBtn.setOnAction(e -> loadUsers());
    }

    private void loadUsers() {
        loadingIndicator.setVisible(true);
        ThreadPool.execute(() -> {
            try {
                // Load users from database
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    // Update table with users
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    showAlert("Error", e.getMessage());
                });
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 