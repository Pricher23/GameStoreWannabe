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
        loadingIndicator.setVisible(true);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(loadingIndicator);

        ThreadPool.execute(() -> {
            try {
                List<User> users = dbManager.searchUsers(username);
                Platform.runLater(() -> {
                    contentArea.getChildren().clear();
                    if (users.isEmpty()) {
                        Label noResults = new Label("No users found");
                        noResults.setStyle("-fx-text-fill: white;");
                        contentArea.getChildren().add(noResults);
                    } else {
                        for (User user : users) {
                            createUserProfileBox(user);
                        }
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    contentArea.getChildren().clear();
                    showAlert("Error", "Failed to search users: " + e.getMessage());
                });
            }
        });
    }

    private void createUserProfileBox(User user) {
        VBox profileBox = new VBox(10);
        profileBox.setStyle("-fx-background-color: #3c3f41; -fx-padding: 15; -fx-background-radius: 5;");
        profileBox.setPrefWidth(400);

        // Username label
        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.setStyle("-fx-font-size: 18; -fx-text-fill: white;");

        // Add friend button
        Button addFriendBtn = new Button("Add Friend");
        addFriendBtn.getStyleClass().add("accent-button");
        addFriendBtn.setOnAction(e -> addFriend(user));

        // View profile button
        Button viewProfileBtn = new Button("View Profile");
        viewProfileBtn.getStyleClass().add("secondary-button");
        viewProfileBtn.setOnAction(e -> viewProfile(user));

        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addFriendBtn, viewProfileBtn);

        profileBox.getChildren().addAll(usernameLabel, buttonBox);
        contentArea.getChildren().add(profileBox);
    }

    private void addFriend(User friend) {
        try {
            dbManager.addFriend(GameShopApp.getCurrentUser().getUserId(), friend.getUserId());
            showAlert("Success", "Friend request sent to " + friend.getUsername());
        } catch (SQLException e) {
            showAlert("Error", "Failed to add friend: " + e.getMessage());
        }
    }

    private void viewProfile(User user) {
        // Clear current content
        contentArea.getChildren().clear();

        VBox profileBox = new VBox(15);
        profileBox.setStyle("-fx-background-color: #3c3f41; -fx-padding: 20; -fx-background-radius: 5;");
        profileBox.setPrefWidth(400);

        // Username header
        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.setStyle("-fx-font-size: 24; -fx-text-fill: white; -fx-font-weight: bold;");

        try {
            // Convert UserGame to Game objects
            List<Game> currentUserGames = dbManager.getUserGames(GameShopApp.getCurrentUser().getUserId())
                .stream()
                .map(UserGame::getGame)
                .collect(Collectors.toList());
            
            List<Game> friendGames = dbManager.getUserGames(user.getUserId())
                .stream()
                .map(UserGame::getGame)
                .collect(Collectors.toList());

            Set<Integer> currentUserGameIds = currentUserGames.stream()
                .map(Game::getGameId)
                .collect(Collectors.toSet());

            // Common Games Section
            Label commonGamesLabel = new Label("Games in Common:");
            commonGamesLabel.setStyle("-fx-font-size: 16; -fx-text-fill: white;");

            VBox commonGamesBox = new VBox(5);
            List<Game> commonGames = friendGames.stream()
                .filter(g -> currentUserGameIds.contains(g.getGameId()))
                .collect(Collectors.toList());

            if (commonGames.isEmpty()) {
                Label noGamesLabel = new Label("No games in common");
                noGamesLabel.setStyle("-fx-text-fill: #808080;");
                commonGamesBox.getChildren().add(noGamesLabel);
            } else {
                for (Game game : commonGames) {
                    Label gameLabel = new Label("• " + game.getTitle());
                    gameLabel.setStyle("-fx-text-fill: #90EE90;"); // Light green
                    commonGamesBox.getChildren().add(gameLabel);
                }
            }

            profileBox.getChildren().addAll(usernameLabel, commonGamesLabel, commonGamesBox);
            contentArea.getChildren().add(profileBox);

        } catch (SQLException e) {
            showAlert("Error", "Failed to load profile: " + e.getMessage());
        }
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