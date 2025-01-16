package com.example.gameshop.scenes;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.example.gameshop.GameShopApp;
import com.example.gameshop.dao.DatabaseManager;
import com.example.gameshop.models.*;
import java.sql.SQLException;
import java.util.List;
import com.example.gameshop.utils.ThreadPool;
import java.util.stream.Collectors;
import javafx.geometry.Pos;

public class StoreScene {
    private Stage stage;
    private DatabaseManager dbManager;
    private FlowPane gamesContainer;

    public StoreScene(Stage stage) {
        this.stage = stage;
        this.dbManager = new DatabaseManager();
        createStoreScene();
    }

    private void createStoreScene() {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #2b2b2b;");
        
        // Top section with menu and search
        VBox topSection = new VBox(10);
        topSection.setStyle("-fx-background-color: #3c3f41;");
        topSection.setPadding(new Insets(10));
        
        // Menu bar with rounded buttons
        HBox menuBox = new HBox(10);
        menuBox.setPadding(new Insets(5, 15, 5, 15));
        
        Button storeBtn = new Button("Store");
        Button accountBtn = new Button("Account");
        Button logoutBtn = new Button("Logout");
        
        storeBtn.getStyleClass().add("menu-button");
        accountBtn.getStyleClass().add("menu-button");
        logoutBtn.getStyleClass().add("menu-button");
        
        storeBtn.setOnAction(e -> new StoreScene(stage));
        accountBtn.setOnAction(e -> new AccountScene(stage));
        logoutBtn.setOnAction(e -> handleLogout());
        
        menuBox.getChildren().addAll(storeBtn, accountBtn, logoutBtn);
        
        // Search bar section
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_RIGHT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search games...");
        searchField.setPrefWidth(200);
        searchField.getStyleClass().add("dark-field");
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchGames(newValue);
        });
        
        searchBox.getChildren().add(searchField);
        
        topSection.getChildren().addAll(menuBox, searchBox);
        layout.setTop(topSection);

        // Friends panel on the left
        VBox friendsPanel = createFriendsPanel();
        layout.setLeft(friendsPanel);

        // Games container
        gamesContainer = new FlowPane();
        gamesContainer.setHgap(10);
        gamesContainer.setVgap(10);
        gamesContainer.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(gamesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b;");
        layout.setCenter(scrollPane);

        loadGames();

        Scene scene = new Scene(layout, 1000, 600); // Made window wider for friends panel
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        stage.setScene(scene);
    }

    private VBox createFriendsPanel() {
        VBox friendsPanel = new VBox(10);
        friendsPanel.setPadding(new Insets(10));
        friendsPanel.setStyle("-fx-background-color: #3c3f41; -fx-min-width: 200;");
        
        Label titleLabel = new Label("Friends");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Button addFriendBtn = new Button("+ Add Friend");
        addFriendBtn.getStyleClass().add("accent-button");
        addFriendBtn.setMaxWidth(Double.MAX_VALUE);
        addFriendBtn.setOnAction(e -> showAddFriendDialog());
        
        VBox friendsList = new VBox(5);
        friendsList.setPadding(new Insets(5, 0, 5, 0));
        
        // Loading indicator for friends list
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #4b6eaf;");
        
        friendsPanel.getChildren().addAll(titleLabel, addFriendBtn, loadingIndicator);
        
        // Load friends list
        ThreadPool.execute(() -> {
            try {
                List<User> friends = dbManager.getUserFriends(GameShopApp.getCurrentUser().getUserId());
                Platform.runLater(() -> {
                    friendsPanel.getChildren().remove(loadingIndicator);
                    if (friends.isEmpty()) {
                        Label noFriendsLabel = new Label("No friends yet");
                        noFriendsLabel.setStyle("-fx-text-fill: #808080;");
                        friendsList.getChildren().add(noFriendsLabel);
                    } else {
                        friends.forEach(friend -> {
                            Button friendBtn = new Button(friend.getUsername());
                            friendBtn.getStyleClass().add("friend-button");
                            friendBtn.setMaxWidth(Double.MAX_VALUE);
                            friendBtn.setOnAction(e -> new FriendSearchScene(stage, friend.getUsername()));
                            friendsList.getChildren().add(friendBtn);
                        });
                    }
                    friendsPanel.getChildren().add(friendsList);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    friendsPanel.getChildren().remove(loadingIndicator);
                    showAlert("Error", "Failed to load friends list");
                });
            }
        });
        
        return friendsPanel;
    }

    private void showAddFriendDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Friend");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter username:");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2b2b2b;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        
        dialog.showAndWait().ifPresent(username -> {
            try {
                User friend = dbManager.getUserByUsername(username);
                if (friend != null && friend.getUserId() != GameShopApp.getCurrentUser().getUserId()) {
                    dbManager.addFriend(GameShopApp.getCurrentUser().getUserId(), friend.getUserId());
                    showAlert("Success", "Friend added successfully!");
                    // Refresh friends list
                    createFriendsPanel();
                } else if (friend != null) {
                    showAlert("Error", "You cannot add yourself as a friend");
                } else {
                    showAlert("Error", "User not found");
                }
            } catch (SQLException e) {
                showAlert("Error", "Failed to add friend: " + e.getMessage());
            }
        });
    }

    private void loadGames() {
        // Create loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #4b6eaf;");
        
        // Center the loading indicator
        StackPane loadingPane = new StackPane(loadingIndicator);
        loadingPane.setPadding(new Insets(20));
        gamesContainer.getChildren().setAll(loadingPane);

        ThreadPool.execute(() -> {
            try {
                List<Game> games = dbManager.getAllGames();
                Platform.runLater(() -> {
                    gamesContainer.getChildren().clear();
                    for (Game game : games) {
                        gamesContainer.getChildren().add(createGameCard(game));
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    gamesContainer.getChildren().clear();
                    showAlert("Error", "Failed to load games: " + e.getMessage());
                });
            }
        });
    }

    private VBox createGameCard(Game game) {
        VBox card = new VBox(5);
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: #3c3f41; " +
                     "-fx-border-color: #555555; " +
                     "-fx-border-radius: 5; " +
                     "-fx-padding: 10; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0, 0, 0);");

        Label titleLabel = new Label(game.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: white;");
        
        Label priceLabel = new Label(String.format("$%.2f", game.getPrice()));
        priceLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #cccccc;");

        Button actionButton = new Button("Buy");
        actionButton.setPrefWidth(120);
        actionButton.getStyleClass().add("game-button");

        try {
            if (dbManager.userOwnsGame(GameShopApp.getCurrentUser().getUserId(), game.getGameId())) {
                actionButton.setText("Owned");
                actionButton.setDisable(true);
                actionButton.setStyle("-fx-background-color: #2d5a27; -fx-text-fill: #90EE90;");
            } else {
                actionButton.setOnAction(e -> handlePurchase(game));
                actionButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            }
        } catch (SQLException e) {
            System.err.println("Error checking game ownership: " + e.getMessage());
            actionButton.setOnAction(event -> handlePurchase(game));
        }

        card.getChildren().addAll(titleLabel, priceLabel, actionButton);
        return card;
    }

    private void handlePurchase(Game game) {
        User currentUser = GameShopApp.getCurrentUser();
        
        if (currentUser.getBalance() < game.getPrice()) {
            showAlert("Insufficient Funds", 
                     "You don't have enough balance to purchase this game.\n" +
                     "Current balance: $" + String.format("%.2f", currentUser.getBalance()) + 
                     "\nGame price: $" + String.format("%.2f", game.getPrice()));
            return;
        }

        try {
            GameKey gameKey = dbManager.getAvailableGameKey(game.getGameId());
            if (gameKey == null) {
                showAlert("Out of Stock", "Sorry, this game is currently out of stock.");
                return;
            }

            // Update user balance
            double newBalance = currentUser.getBalance() - game.getPrice();
            dbManager.updateUserBalance(currentUser.getUserId(), newBalance);
            currentUser.setBalance(newBalance);

            // Assign key to user
            dbManager.assignGameKeyToUser(gameKey.getKeyId(), currentUser.getUserId());

            // Show success message with the key
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Purchase Successful");
            alert.setHeaderText(null);
            alert.setContentText("You have successfully purchased " + game.getTitle() + 
                               "\nYour game key is: " + gameKey.getKeyValue() +
                               "\nYou can view this key again in your account.");
            alert.showAndWait();

            // Refresh the store view
            loadGames();
        } catch (SQLException e) {
            showAlert("Error", "Failed to complete purchase: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void searchGames(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadGames(); // Show all games if search is empty
            return;
        }

        // Show loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #4b6eaf;");
        StackPane loadingPane = new StackPane(loadingIndicator);
        loadingPane.setPadding(new Insets(20));
        gamesContainer.getChildren().setAll(loadingPane);

        ThreadPool.execute(() -> {
            try {
                List<Game> allGames = dbManager.getAllGames();
                // Filter games based on search text
                List<Game> filteredGames = allGames.stream()
                    .filter(game -> game.getTitle().toLowerCase()
                        .contains(searchText.toLowerCase()))
                    .collect(Collectors.toList());

                Platform.runLater(() -> {
                    gamesContainer.getChildren().clear();
                    if (filteredGames.isEmpty()) {
                        Label noResults = new Label("No games found matching '" + searchText + "'");
                        noResults.setStyle("-fx-text-fill: white;");
                        gamesContainer.getChildren().add(noResults);
                    } else {
                        filteredGames.forEach(game -> 
                            gamesContainer.getChildren().add(createGameCard(game)));
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    gamesContainer.getChildren().clear();
                    showAlert("Error", "Failed to search games: " + e.getMessage());
                });
            }
        });
    }

    private void handleLogout() {
        GameShopApp.setCurrentUser(null);
        new LoginScene(stage);
    }
} 