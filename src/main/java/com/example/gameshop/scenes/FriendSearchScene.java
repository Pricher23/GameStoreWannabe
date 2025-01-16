package com.example.gameshop.scenes;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.example.gameshop.utils.ThreadPool;
import com.example.gameshop.dao.DatabaseManager;
import com.example.gameshop.models.*;
import com.example.gameshop.GameShopApp;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FriendSearchScene {
    private Stage stage;
    private DatabaseManager dbManager;
    private VBox contentArea;
    private String searchUsername;

    public FriendSearchScene(Stage stage) {
        this(stage, null);
    }

    public FriendSearchScene(Stage stage, String username) {
        this.stage = stage;
        this.dbManager = new DatabaseManager();
        this.searchUsername = username;
        createFriendSearchScene();
        
        if (searchUsername != null) {
            searchUser(searchUsername);
        }
    }

    private void createFriendSearchScene() {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #2b2b2b;");

        // Back button
        Button backButton = new Button("← Back");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> new AccountScene(stage));
        
        // Search area
        VBox searchArea = new VBox(10);
        searchArea.setPadding(new Insets(20));
        searchArea.setStyle("-fx-background-color: #3c3f41;");

        Label titleLabel = new Label("Search Friends");
        titleLabel.setStyle("-fx-font-size: 24; -fx-text-fill: white;");

        TextField searchField = new TextField();
        searchField.setPromptText("Enter username");
        searchField.getStyleClass().add("dark-field");

        Button searchButton = new Button("Search");
        searchButton.getStyleClass().add("accent-button");

        searchArea.getChildren().addAll(backButton, titleLabel, searchField, searchButton);
        layout.setTop(searchArea);

        // Content area for search results
        contentArea = new VBox(10);
        contentArea.setPadding(new Insets(20));
        contentArea.setStyle("-fx-background-color: #2b2b2b;");
        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2b2b2b;");
        layout.setCenter(scrollPane);

        searchButton.setOnAction(e -> searchUser(searchField.getText()));

        Scene scene = new Scene(layout, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        stage.setScene(scene);
    }

    private void searchUser(String username) {
        if (username.trim().isEmpty()) {
            showAlert("Error", "Please enter a username");
            return;
        }

        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #4b6eaf;");
        contentArea.getChildren().setAll(loadingIndicator);

        ThreadPool.execute(() -> {
            try {
                User user = dbManager.getUserByUsername(username);
                if (user != null) {
                    List<UserGame> userGames = dbManager.getUserGames(user.getUserId());
                    Platform.runLater(() -> showUserProfile(user, userGames));
                } else {
                    Platform.runLater(() -> {
                        contentArea.getChildren().clear();
                        showAlert("Not Found", "User not found");
                    });
                }
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    contentArea.getChildren().clear();
                    showAlert("Error", "Failed to search user: " + e.getMessage());
                });
            }
        });
    }

    private void showUserProfile(User user, List<UserGame> userGames) {
        contentArea.getChildren().clear();

        VBox profileBox = new VBox(10);
        profileBox.setStyle("-fx-background-color: #3c3f41; -fx-padding: 20; -fx-background-radius: 5;");

        Label usernameLabel = new Label("Username: " + user.getUsername());
        usernameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");

        // Get current user's games
        try {
            List<UserGame> currentUserGames = dbManager.getUserGames(GameShopApp.getCurrentUser().getUserId());
            Set<Integer> currentUserGameIds = currentUserGames.stream()
                .map(ug -> ug.getGame().getGameId())
                .collect(Collectors.toSet());

            // Common Games Section
            Label commonGamesLabel = new Label("Games in Common:");
            commonGamesLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #90EE90;"); // Light green

            VBox commonGamesBox = new VBox(5);
            List<UserGame> commonGames = userGames.stream()
                .filter(ug -> currentUserGameIds.contains(ug.getGame().getGameId()))
                .collect(Collectors.toList());

            for (UserGame userGame : commonGames) {
                Label gameLabel = new Label("• " + userGame.getGame().getTitle());
                gameLabel.setStyle("-fx-text-fill: #90EE90;"); // Light green
                commonGamesBox.getChildren().add(gameLabel);
            }

            // Other Games Section
            Label otherGamesLabel = new Label("Other Games:");
            otherGamesLabel.setStyle("-fx-font-size: 16; -fx-text-fill: white;");

            VBox otherGamesBox = new VBox(5);
            List<UserGame> otherGames = userGames.stream()
                .filter(ug -> !currentUserGameIds.contains(ug.getGame().getGameId()))
                .collect(Collectors.toList());

            for (UserGame userGame : otherGames) {
                Label gameLabel = new Label("• " + userGame.getGame().getTitle());
                gameLabel.setStyle("-fx-text-fill: white;");
                otherGamesBox.getChildren().add(gameLabel);
            }

            // Add sections to profile box
            profileBox.getChildren().addAll(
                usernameLabel,
                new Separator(),
                commonGamesLabel,
                commonGamesBox,
                new Separator(),
                otherGamesLabel,
                otherGamesBox
            );

        } catch (SQLException e) {
            showAlert("Error", "Failed to load games comparison");
        }

        contentArea.getChildren().add(profileBox);
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu accountMenu = new Menu("Navigation");
        MenuItem storeItem = new MenuItem("Store");
        MenuItem accountItem = new MenuItem("My Account");
        MenuItem backItem = new MenuItem("Back");

        storeItem.setOnAction(e -> new StoreScene(stage));
        accountItem.setOnAction(e -> new AccountScene(stage));
        backItem.setOnAction(e -> new AccountScene(stage));

        accountMenu.getItems().addAll(storeItem, accountItem, backItem);
        menuBar.getMenus().add(accountMenu);
        return menuBar;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2b2b2b;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        alert.showAndWait();
    }
} 