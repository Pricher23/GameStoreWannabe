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
import javafx.scene.Node;

public class AccountScene {
    private Stage stage;
    private DatabaseManager dbManager;
    private VBox contentArea;
    private FlowPane gamesContainer;
    private TextField searchField;
    private String currentView = "all"; // Can be "all", "store", or "steam"

    public AccountScene(Stage stage) {
        this.stage = stage;
        this.dbManager = new DatabaseManager();
        createAccountScene();
    }

    private void createAccountScene() {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #2b2b2b;");

        // Top section with navigation and filters
        VBox topSection = createTopSection();
        layout.setTop(topSection);

        // Main content area
        contentArea = new VBox(10);
        contentArea.setPadding(new Insets(10));
        
        // Games container
        gamesContainer = new FlowPane(10, 10);
        gamesContainer.setPadding(new Insets(10));
        
        ScrollPane scrollPane = new ScrollPane(gamesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2b2b2b; -fx-background-color: transparent;");
        
        contentArea.getChildren().add(scrollPane);
        layout.setCenter(contentArea);

        Scene scene = new Scene(layout, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Account - Game Library");

        // Load games initially
        loadGames("all");
    }

    private VBox createTopSection() {
        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(10));
        topSection.setStyle("-fx-background-color: #3c3f41;");

        // Navigation buttons
        HBox menuBox = new HBox(10);
        Button storeBtn = new Button("Store");
        Button accountBtn = new Button("Account");
        Button friendsBtn = new Button("Search Friends");
        Button logoutBtn = new Button("Logout");

        storeBtn.setOnAction(e -> new StoreScene(stage));
        accountBtn.setOnAction(e -> new AccountScene(stage));
        friendsBtn.setOnAction(e -> new FriendSearchScene(stage));
        logoutBtn.setOnAction(e -> handleLogout());

        menuBox.getChildren().addAll(storeBtn, accountBtn, friendsBtn, logoutBtn);

        // Library filter buttons
        HBox filterBox = new HBox(10);
        Button allGamesBtn = new Button("All Games");
        Button storeGamesBtn = new Button("Store Purchases");
        Button steamGamesBtn = new Button("Steam Library");

        allGamesBtn.setOnAction(e -> {
            currentView = "all";
            loadGames("all");
        });
        storeGamesBtn.setOnAction(e -> {
            currentView = "store";
            loadGames("store");
        });
        steamGamesBtn.setOnAction(e -> {
            currentView = "steam";
            loadGames("steam");
        });

        filterBox.getChildren().addAll(allGamesBtn, storeGamesBtn, steamGamesBtn);

        // Search section
        HBox searchBox = new HBox(10);
        searchField = new TextField();
        searchField.setPromptText("Search games...");
        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> handleSearch());

        // Sort options
        ComboBox<String> sortBox = new ComboBox<>();
        sortBox.getItems().addAll("Title", "Price", "Purchase Date");
        sortBox.setValue("Title");
        sortBox.setOnAction(e -> handleSort(sortBox.getValue()));

        searchBox.getChildren().addAll(searchField, searchBtn, sortBox);

        topSection.getChildren().addAll(menuBox, filterBox, searchBox);
        return topSection;
    }

    private void loadGames(String viewType) {
        gamesContainer.getChildren().clear();
        
        try {
            List<Game> games = new ArrayList<>();
            
            switch (viewType) {
                case "all":
                    games.addAll(dbManager.getUserGames(GameShopApp.getCurrentUser().getUserId())
                               .stream()
                               .map(UserGame::getGame)
                               .collect(Collectors.toList()));
                    games.addAll(dbManager.getUserSteamGames(GameShopApp.getCurrentUser().getUserId()));
                    break;
                    
                case "store":
                    games.addAll(dbManager.getUserGames(GameShopApp.getCurrentUser().getUserId())
                               .stream()
                               .map(UserGame::getGame)
                               .collect(Collectors.toList()));
                    break;
                    
                case "steam":
                    games.addAll(dbManager.getUserSteamGames(GameShopApp.getCurrentUser().getUserId()));
                    break;
            }
            
            for (Game game : games) {
                VBox gameBox = createGameBox(game);
                gamesContainer.getChildren().add(gameBox);
            }
            
        } catch (SQLException e) {
            showAlert("Error", "Failed to load games: " + e.getMessage());
        }
    }

    private VBox createGameBox(Game game) {
        VBox gameBox = new VBox(5);
        gameBox.setStyle("-fx-background-color: #3c3f41; -fx-padding: 10; -fx-background-radius: 5;");
        gameBox.setPrefWidth(200);
        gameBox.setOnMouseClicked(e -> showGameDetails(game));

        Label titleLabel = new Label(game.getTitle());
        titleLabel.setStyle("-fx-font-size: 14; -fx-text-fill: white;");
        titleLabel.setWrapText(true);

        Label priceLabel = new Label(String.format("$%.2f", game.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #90caf9;");

        gameBox.getChildren().addAll(titleLabel, priceLabel);
        return gameBox;
    }

    private void handleSearch() {
        String searchTerm = searchField.getText().toLowerCase();
        loadGames(currentView);
        
        gamesContainer.getChildren().removeIf(node -> {
            VBox gameBox = (VBox) node;
            Label titleLabel = (Label) gameBox.getChildren().get(0);
            return !titleLabel.getText().toLowerCase().contains(searchTerm);
        });
    }

    private void handleSort(String sortBy) {
        List<Node> games = new ArrayList<>(gamesContainer.getChildren());
        
        games.sort((a, b) -> {
            String titleA = ((Label) ((VBox) a).getChildren().get(0)).getText();
            String titleB = ((Label) ((VBox) b).getChildren().get(0)).getText();
            
            switch (sortBy) {
                case "Title":
                    return titleA.compareTo(titleB);
                case "Price":
                    double priceA = Double.parseDouble(((Label) ((VBox) a).getChildren().get(1)).getText().substring(1));
                    double priceB = Double.parseDouble(((Label) ((VBox) b).getChildren().get(1)).getText().substring(1));
                    return Double.compare(priceA, priceB);
                default:
                    return 0;
            }
        });
        
        gamesContainer.getChildren().clear();
        gamesContainer.getChildren().addAll(games);
    }

    private void showGameDetails(Game game) {
        new GamePreviewScene(stage, game);
    }

    private void handleLogout() {
        GameShopApp.setCurrentUser(null);
        new LoginScene(stage);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 