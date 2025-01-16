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
import com.example.gameshop.services.SteamAPI;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javafx.scene.image.ImageView;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;

public class AccountScene {
    private Stage stage;
    private FlowPane gamesContainer;
    private DatabaseManager dbManager;
    private User currentUser;

    public AccountScene(Stage stage) {
        this.stage = stage;
        this.dbManager = new DatabaseManager();
        this.gamesContainer = new FlowPane();
        this.currentUser = GameShopApp.getCurrentUser();
        createAccountScene();
    }

    private void createAccountScene() {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #2b2b2b;");
        
        // Configure gamesContainer
        gamesContainer.setPrefWrapLength(800);
        gamesContainer.setHgap(10);
        gamesContainer.setVgap(10);
        gamesContainer.setPadding(new Insets(10));
        gamesContainer.setStyle("-fx-background-color: #2b2b2b;");

        // Add ScrollPane for games
        ScrollPane scrollPane = new ScrollPane(gamesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b;");
        scrollPane.getStyleClass().add("dark-scroll-pane");

        // Top section with back button, menu and Steam import
        HBox topSection = new HBox(20);
        topSection.setAlignment(Pos.CENTER_RIGHT);
        topSection.setPadding(new Insets(10, 20, 10, 20));
        topSection.setStyle("-fx-background-color: #3c3f41;");

        // Back button on the left
        Button backButton = new Button("Back");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> new StoreScene(stage));
        
        // Create a spacer to push Steam controls to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Steam import controls
        TextField steamIdField = new TextField();
        steamIdField.setPromptText("Enter Steam ID");
        steamIdField.setPrefWidth(200);
        steamIdField.getStyleClass().add("dark-field");

        Button importButton = new Button("Import Steam Library");
        importButton.getStyleClass().add("accent-button");

        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #4b6eaf;");
        loadingIndicator.setVisible(false);

        importButton.setOnAction(e -> {
            if (steamIdField.getText().trim().isEmpty()) {
                showAlert("Error", "Please enter your Steam ID");
                return;
            }

            loadingIndicator.setVisible(true);
            importButton.setDisable(true);

            ThreadPool.execute(() -> {
                try {
                    SteamAPI steamAPI = new SteamAPI();
                    List<Game> steamGames = steamAPI.getOwnedGames(steamIdField.getText());
                    
                    // Save Steam ID to user profile
                    dbManager.updateUserSteamId(GameShopApp.getCurrentUser().getUserId(), 
                                              steamIdField.getText());
                    
                    // Save games to steam_games table
                    dbManager.saveSteamGames(GameShopApp.getCurrentUser().getUserId(), steamGames);

                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        importButton.setDisable(false);
                        showAlert("Success", "Steam library imported successfully!");
                        loadGames(); // Refresh the games display
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        importButton.setDisable(false);
                        showAlert("Error", "Failed to import Steam library: " + ex.getMessage());
                    });
                }
            });
        });

        topSection.getChildren().addAll(backButton, spacer, steamIdField, importButton, loadingIndicator);
        
        // Add sorting controls
        HBox sortingControls = new HBox(10);
        sortingControls.setAlignment(Pos.CENTER_LEFT);
        sortingControls.setPadding(new Insets(5, 20, 5, 20));
        sortingControls.setStyle("-fx-background-color: #3c3f41;");
        
        Label sortLabel = new Label("Sort by:");
        sortLabel.setStyle("-fx-text-fill: white;");
        
        ComboBox<String> sortBox = new ComboBox<>();
        sortBox.getItems().addAll("Title", "Hours Played", "Price", "Developer", "Genre");
        sortBox.setValue("Title");
        sortBox.getStyleClass().add("dark-combo-box");
        
        sortingControls.getChildren().addAll(sortLabel, sortBox);

        // Add search controls
        HBox searchControls = new HBox(10);
        searchControls.setAlignment(Pos.CENTER_LEFT);
        searchControls.setPadding(new Insets(5, 20, 5, 20));
        searchControls.setStyle("-fx-background-color: #3c3f41;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search games...");
        searchField.setPrefWidth(200);
        searchField.getStyleClass().add("dark-field");

        ProgressIndicator searchLoadingIndicator = new ProgressIndicator();
        searchLoadingIndicator.setVisible(false);
        searchLoadingIndicator.setStyle("-fx-progress-color: #4b6eaf;");

        Button searchButton = new Button("Search");
        searchButton.getStyleClass().add("accent-button");
        
        searchButton.setOnAction(e -> {
            String searchTerm = searchField.getText().trim().toLowerCase();
            searchLoadingIndicator.setVisible(true);
            
            ThreadPool.execute(() -> {
                try {
                    List<Game> allGames = dbManager.getUserSteamGames(GameShopApp.getCurrentUser().getUserId());
                    List<Game> filteredGames = allGames.stream()
                        .filter(game -> 
                            game.getTitle().toLowerCase().contains(searchTerm) ||
                            game.getDetails().getDeveloper().toLowerCase().contains(searchTerm) ||
                            game.getDetails().getGenre().toLowerCase().contains(searchTerm))
                        .collect(Collectors.toList());
                    
                    Platform.runLater(() -> {
                        searchLoadingIndicator.setVisible(false);
                        gamesContainer.getChildren().clear();
                        if (filteredGames.isEmpty()) {
                            Label noGamesLabel = new Label("No games found matching your search.");
                            noGamesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
                            gamesContainer.getChildren().add(noGamesLabel);
                        } else {
                            filteredGames.forEach(game -> gamesContainer.getChildren().add(createGameCard(game)));
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        searchLoadingIndicator.setVisible(false);
                        showAlert("Error", "Failed to search games: " + ex.getMessage());
                    });
                }
            });
        });

        // Add search on Enter key press
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                searchButton.fire();
            }
        });

        searchControls.getChildren().addAll(searchField, searchButton, searchLoadingIndicator);

        // Create VBox for all top controls
        VBox topControls = new VBox();
        topControls.getChildren().addAll(topSection, sortingControls, searchControls);
        
        layout.setTop(topControls);
        layout.setCenter(scrollPane);

        // Load games
        loadGames();

        Scene scene = new Scene(layout, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        stage.setScene(scene);

        // Update the ComboBox event handler
        sortBox.setOnAction(e -> {
            String sortBy = sortBox.getValue();
            sortGames(sortBy);
        });
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #3c3f41;");
        
        Menu accountMenu = new Menu("Account");
        MenuItem storeItem = new MenuItem("Store");
        MenuItem logoutItem = new MenuItem("Logout");
        
        storeItem.setOnAction(e -> new StoreScene(stage));
        logoutItem.setOnAction(e -> {
            GameShopApp.setCurrentUser(null);
            new LoginScene(stage);
        });

        accountMenu.getItems().addAll(storeItem, logoutItem);
        menuBar.getMenus().add(accountMenu);
        return menuBar;
    }

    private VBox createUserInfoSection() {
        VBox userInfo = new VBox(10);
        userInfo.setPadding(new Insets(20));
        userInfo.setPrefWidth(200);
        userInfo.setStyle("-fx-background-color: #3c3f41;");

        Label titleLabel = new Label("Account Info");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");

        Label usernameLabel = new Label("Username: " + currentUser.getUsername());
        usernameLabel.setStyle("-fx-text-fill: white;");
        
        Label emailLabel = new Label("Email: " + currentUser.getEmail());
        emailLabel.setStyle("-fx-text-fill: white;");
        
        Label balanceLabel = new Label(String.format("Balance: $%.2f", currentUser.getBalance()));
        balanceLabel.setStyle("-fx-text-fill: white;");

        Button addFundsBtn = new Button("Add Funds");
        addFundsBtn.getStyleClass().add("accent-button");
        addFundsBtn.setMaxWidth(Double.MAX_VALUE);
        addFundsBtn.setOnAction(e -> showAddFundsDialog());

        Button searchFriendsBtn = new Button("Search Friends");
        searchFriendsBtn.getStyleClass().add("accent-button");
        searchFriendsBtn.setMaxWidth(Double.MAX_VALUE);
        searchFriendsBtn.setOnAction(e -> new FriendSearchScene(stage));

        userInfo.getChildren().addAll(
            titleLabel,
            new Separator(),
            usernameLabel,
            emailLabel,
            balanceLabel,
            new Separator(),
            addFundsBtn,
            searchFriendsBtn
        );

        return userInfo;
    }

    private void loadGames() {
        gamesContainer.getChildren().clear();
        
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #4b6eaf;");
        gamesContainer.getChildren().add(loadingIndicator);

        ThreadPool.execute(() -> {
            try {
                List<Game> games = dbManager.getUserSteamGames(GameShopApp.getCurrentUser().getUserId());
                Platform.runLater(() -> {
                    gamesContainer.getChildren().clear();
                    if (games.isEmpty()) {
                        Label noGamesLabel = new Label("No games found. Import your Steam library!");
                        noGamesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
                        gamesContainer.getChildren().add(noGamesLabel);
                    } else {
                        for (Game game : games) {
                            gamesContainer.getChildren().add(createGameCard(game));
                        }
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
        card.setPrefHeight(300);
        card.setMaxWidth(200);
        card.setMaxHeight(300);
        card.setStyle("-fx-background-color: #3c3f41; -fx-padding: 10; -fx-background-radius: 5;");
        card.setUserData(game);
        
        ImageView gameImage = new ImageView(); // You can add game images later
        gameImage.setFitWidth(180);
        gameImage.setFitHeight(100);
        
        Label titleLabel = new Label(game.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");
        titleLabel.setWrapText(true);
        
        Label priceLabel = new Label(String.format("$%.2f", game.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #4b6eaf;");
        
        card.setOnMouseClicked(e -> openGamePreview(game));
        
        card.getChildren().addAll(gameImage, titleLabel, priceLabel);
        return card;
    }

    private void openGamePreview(Game game) {
        new GamePreviewScene(stage, game);
    }

    private void showGameKey(GameKey gameKey) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Key");
        alert.setHeaderText(null);
        alert.setContentText("Your game key is: " + gameKey.getKeyValue());
        
        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2b2b2b;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        
        alert.showAndWait();
    }

    private void showAddFundsDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Funds");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter amount to add ($):");

        // Style the dialog
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2b2b2b;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        
        dialog.showAndWait().ifPresent(amount -> {
            try {
                double value = Double.parseDouble(amount);
                if (value > 0) {
                    double newBalance = currentUser.getBalance() + value;
                    dbManager.updateUserBalance(currentUser.getUserId(), newBalance);
                    currentUser.setBalance(newBalance);
                    loadGames(); // Refresh the view
                } else {
                    showAlert("Error", "Please enter a positive amount");
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid amount");
            } catch (SQLException e) {
                showAlert("Error", "Failed to update balance");
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2b2b2b;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        
        alert.showAndWait();
    }

    private void sortGames(String sortBy) {
        List<Game> games = new ArrayList<>(gamesContainer.getChildren()
                .stream()
                .map(node -> (VBox) node)
                .map(card -> (Game) card.getUserData())
                .collect(Collectors.toList()));

        // Show loading indicator while sorting
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #4b6eaf;");
        gamesContainer.getChildren().add(loadingIndicator);

        ThreadPool.execute(() -> {
            try {
                switch(sortBy) {
                    case "Title":
                        games.sort((g1, g2) -> g1.getTitle().compareToIgnoreCase(g2.getTitle()));
                        break;
                    case "Hours Played":
                        games.sort((g1, g2) -> Double.compare(g2.getPlaytimeMinutes(), g1.getPlaytimeMinutes()));
                        break;
                    case "Price":
                        games.sort((g1, g2) -> Double.compare(g2.getPrice(), g1.getPrice()));
                        break;
                    case "Developer":
                        games.sort((g1, g2) -> g1.getDetails().getDeveloper().compareToIgnoreCase(g2.getDetails().getDeveloper()));
                        break;
                    case "Genre":
                        games.sort((g1, g2) -> g1.getDetails().getGenre().compareToIgnoreCase(g2.getDetails().getGenre()));
                        break;
                }

                Platform.runLater(() -> {
                    gamesContainer.getChildren().clear();
                    games.forEach(game -> gamesContainer.getChildren().add(createGameCard(game)));
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    showAlert("Error", "Failed to sort games: " + e.getMessage());
                });
            }
        });
    }

    private void refreshGamesDisplay(List<Game> games) {
        gamesContainer.getChildren().clear();
        if (games.isEmpty()) {
            Label noGamesLabel = new Label("No games found. Import your Steam library!");
            noGamesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            gamesContainer.getChildren().add(noGamesLabel);
        } else {
            for (Game game : games) {
                gamesContainer.getChildren().add(createGameCard(game));
            }
        }
    }
} 