package com.example.gameshop.scenes;

import com.example.gameshop.dao.DatabaseManager;
import com.example.gameshop.models.Game;
import com.example.gameshop.models.User;
import com.example.gameshop.GameShopApp;
import com.example.gameshop.utils.ThreadPool;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;

public class AdminScene {
    private Stage stage;
    private DatabaseManager dbManager;
    private TableView<User> userTable;
    private TableView<Game> gameTable;
    private VBox contentArea;

    public AdminScene(Stage stage) {
        this.stage = stage;
        this.dbManager = new DatabaseManager();
        createAdminScene();
    }

    private void createAdminScene() {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #2b2b2b;");

        // Top menu
        HBox menuBar = createMenuBar();
        layout.setTop(menuBar);

        // Content area
        contentArea = new VBox(10);
        contentArea.setPadding(new Insets(10));
        layout.setCenter(contentArea);

        // Initial view - User Management
        showUserManagement();

        Scene scene = new Scene(layout, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Admin Panel");
    }

    private HBox createMenuBar() {
        HBox menuBar = new HBox(10);
        menuBar.setPadding(new Insets(10));
        menuBar.setStyle("-fx-background-color: #3c3f41;");

        Button usersBtn = new Button("Manage Users");
        Button gamesBtn = new Button("Manage Games");
        Button keysBtn = new Button("Manage Keys");
        Button logoutBtn = new Button("Logout");

        usersBtn.setOnAction(e -> showUserManagement());
        gamesBtn.setOnAction(e -> showGameManagement());
        keysBtn.setOnAction(e -> showKeyManagement());
        logoutBtn.setOnAction(e -> handleLogout());

        menuBar.getChildren().addAll(usersBtn, gamesBtn, keysBtn, logoutBtn);
        return menuBar;
    }

    private void showUserManagement() {
        contentArea.getChildren().clear();

        // Create user table
        userTable = new TableView<>();
        setupUserTable();

        Button deleteUserBtn = new Button("Delete Selected User");
        deleteUserBtn.setOnAction(e -> deleteSelectedUser());

        contentArea.getChildren().addAll(new Label("User Management"), userTable, deleteUserBtn);
        loadUsers();
    }

    private void showGameManagement() {
        contentArea.getChildren().clear();

        // Game management controls
        VBox addGameBox = createAddGameForm();
        gameTable = new TableView<>();
        setupGameTable();

        Button deleteGameBtn = new Button("Delete Selected Game");
        deleteGameBtn.setOnAction(e -> deleteSelectedGame());

        Button editGameBtn = new Button("Edit Selected Game");
        editGameBtn.setOnAction(e -> editSelectedGame());

        HBox buttonBox = new HBox(10, deleteGameBtn, editGameBtn);
        
        contentArea.getChildren().addAll(
            new Label("Game Management"),
            addGameBox,
            new Separator(),
            gameTable,
            buttonBox
        );
        
        loadGames();
    }

    private void showKeyManagement() {
        contentArea.getChildren().clear();

        // Game selection
        ComboBox<Game> gameSelect = new ComboBox<>();
        loadGamesIntoComboBox(gameSelect);

        // Key input
        TextField keyField = new TextField();
        keyField.setPromptText("Enter game key");

        Button addKeyBtn = new Button("Add Key");
        addKeyBtn.setOnAction(e -> addGameKey(gameSelect.getValue(), keyField.getText()));

        VBox keyManagementBox = new VBox(10,
            new Label("Add Keys to Games"),
            gameSelect,
            keyField,
            addKeyBtn
        );

        contentArea.getChildren().add(keyManagementBox);
    }

    // Helper methods for user management
    private void setupUserTable() {
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        TableColumn<User, Double> balanceCol = new TableColumn<>("Balance");

        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));

        userTable.getColumns().addAll(usernameCol, emailCol, roleCol, balanceCol);
    }

    private void deleteSelectedUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                dbManager.deleteUser(selectedUser.getUserId());
                loadUsers();
                showAlert("Success", "User deleted successfully");
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete user: " + e.getMessage());
            }
        }
    }

    // Helper methods for game management
    private VBox createAddGameForm() {
        TextField titleField = new TextField();
        titleField.setPromptText("Game Title");

        TextArea descField = new TextArea();
        descField.setPromptText("Game Description");

        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        Button addGameBtn = new Button("Add Game");
        addGameBtn.setOnAction(e -> addGame(titleField.getText(), 
                                          descField.getText(), 
                                          Double.parseDouble(priceField.getText())));

        return new VBox(10, 
            new Label("Add New Game"),
            titleField,
            descField,
            priceField,
            addGameBtn
        );
    }

    private void setupGameTable() {
        TableColumn<Game, String> titleCol = new TableColumn<>("Title");
        TableColumn<Game, String> descCol = new TableColumn<>("Description");
        TableColumn<Game, Double> priceCol = new TableColumn<>("Price");

        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        gameTable.getColumns().addAll(titleCol, descCol, priceCol);
    }

    // Database operations
    private void loadUsers() {
        try {
            List<User> users = dbManager.getAllUsers();
            userTable.getItems().setAll(users);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void loadGames() {
        try {
            List<Game> games = dbManager.getAllGames();
            gameTable.getItems().setAll(games);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load games: " + e.getMessage());
        }
    }

    private void loadGamesIntoComboBox(ComboBox<Game> gameSelect) {
        try {
            List<Game> games = dbManager.getAllGames();
            gameSelect.getItems().setAll(games);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load games: " + e.getMessage());
        }
    }

    private void addGame(String title, String description, double price) {
        try {
            Game game = new Game(title, description, price);
            dbManager.addGame(game);
            loadGames();
            showAlert("Success", "Game added successfully");
        } catch (SQLException e) {
            showAlert("Error", "Failed to add game: " + e.getMessage());
        }
    }

    private void deleteSelectedGame() {
        Game selectedGame = gameTable.getSelectionModel().getSelectedItem();
        if (selectedGame != null) {
            try {
                dbManager.deleteGame(selectedGame.getGameId());
                loadGames();
                showAlert("Success", "Game deleted successfully");
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete game: " + e.getMessage());
            }
        }
    }

    private void editSelectedGame() {
        Game selectedGame = gameTable.getSelectionModel().getSelectedItem();
        if (selectedGame != null) {
            // Create edit dialog
            Dialog<Game> dialog = new Dialog<>();
            dialog.setTitle("Edit Game");

            // Create form fields
            TextField titleField = new TextField(selectedGame.getTitle());
            TextArea descField = new TextArea(selectedGame.getDescription());
            TextField priceField = new TextField(String.valueOf(selectedGame.getPrice()));

            VBox content = new VBox(10,
                new Label("Title:"), titleField,
                new Label("Description:"), descField,
                new Label("Price:"), priceField
            );

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    selectedGame.setTitle(titleField.getText());
                    selectedGame.setDescription(descField.getText());
                    selectedGame.setPrice(Double.parseDouble(priceField.getText()));
                    return selectedGame;
                }
                return null;
            });

            dialog.showAndWait().ifPresent(game -> {
                try {
                    dbManager.updateGame(game);
                    loadGames();
                    showAlert("Success", "Game updated successfully");
                } catch (SQLException e) {
                    showAlert("Error", "Failed to update game: " + e.getMessage());
                }
            });
        }
    }

    private void addGameKey(Game game, String keyValue) {
        if (game != null && !keyValue.isEmpty()) {
            try {
                dbManager.addGameKey(game.getGameId(), keyValue);
                showAlert("Success", "Key added successfully");
            } catch (SQLException e) {
                showAlert("Error", "Failed to add key: " + e.getMessage());
            }
        }
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