package com.example.gameshop.scenes;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.example.gameshop.GameShopApp;
import com.example.gameshop.dao.DatabaseManager;
import com.example.gameshop.models.Game;
import com.example.gameshop.models.GameKey;
import com.example.gameshop.models.User;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

public class AdminScene {
    private Stage stage;
    private DatabaseManager dbManager;
    private VBox contentArea;

    public AdminScene(Stage stage) {
        this.stage = stage;
        this.dbManager = new DatabaseManager();
        createAdminScene();
    }

    private void createAdminScene() {
        BorderPane layout = new BorderPane();
        
        MenuBar menuBar = createMenuBar();
        layout.setTop(menuBar);

        VBox sideMenu = createSideMenu();
        layout.setLeft(sideMenu);

        contentArea = new VBox(10);
        contentArea.setPadding(new Insets(10));
        layout.setCenter(contentArea);

        Scene scene = new Scene(layout, 1000, 600);
        stage.setScene(scene);
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu accountMenu = new Menu("Admin");
        MenuItem logoutItem = new MenuItem("Logout");
        
        logoutItem.setOnAction(e -> {
            GameShopApp.setCurrentUser(null);
            new LoginScene(stage);
        });

        accountMenu.getItems().add(logoutItem);
        menuBar.getMenus().add(accountMenu);
        return menuBar;
    }

    private VBox createSideMenu() {
        VBox sideMenu = new VBox(10);
        sideMenu.setPadding(new Insets(10));
        sideMenu.setStyle("-fx-background-color: #f0f0f0;");

        Button addGameBtn = new Button("Add Game");
        Button addKeyBtn = new Button("Add Game Key");
        Button viewUsersBtn = new Button("View Users");

        addGameBtn.setOnAction(e -> showAddGameForm());
        addKeyBtn.setOnAction(e -> showAddKeyForm());
        viewUsersBtn.setOnAction(e -> showUsersList());

        sideMenu.getChildren().addAll(addGameBtn, addKeyBtn, viewUsersBtn);
        return sideMenu;
    }

    private void showAddGameForm() {
        contentArea.getChildren().clear();

        TextField titleField = new TextField();
        titleField.setPromptText("Game Title");
        
        TextArea descField = new TextArea();
        descField.setPromptText("Game Description");
        descField.setPrefRowCount(3);
        
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        
        Button submitBtn = new Button("Add Game");

        submitBtn.setOnAction(e -> {
            try {
                Game game = new Game(
                    titleField.getText(),
                    descField.getText(),
                    Double.parseDouble(priceField.getText())
                );
                dbManager.createGame(game);
                showAlert("Success", "Game added successfully!");
                titleField.clear();
                descField.clear();
                priceField.clear();
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid price format");
            } catch (SQLException ex) {
                showAlert("Error", "Failed to add game: " + ex.getMessage());
            }
        });

        contentArea.getChildren().addAll(
            new Label("Add New Game"),
            titleField, descField, priceField, submitBtn
        );
    }

    private void showUsersList() {
        contentArea.getChildren().clear();

        TableView<User> userTable = new TableView<>();
        
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        userTable.getColumns().addAll(usernameCol, emailCol, roleCol);

        Button changeRoleBtn = new Button("Toggle Admin Role");
        changeRoleBtn.setOnAction(e -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                try {
                    String newRole = selectedUser.getRole().equals("ADMIN") ? "CUSTOMER" : "ADMIN";
                    dbManager.updateUserRole(selectedUser.getUserId(), newRole);
                    loadUsers(userTable);
                } catch (SQLException ex) {
                    showAlert("Error", "Failed to update role: " + ex.getMessage());
                }
            }
        });

        loadUsers(userTable);
        contentArea.getChildren().addAll(userTable, changeRoleBtn);
    }

    private void loadUsers(TableView<User> userTable) {
        Thread loadingThread = new Thread(() -> {
            try {
                List<User> users = dbManager.getAllUsers();
                Platform.runLater(() -> {
                    userTable.getItems().clear();
                    userTable.getItems().addAll(users);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> 
                    showAlert("Error", "Failed to load users: " + e.getMessage())
                );
            }
        });
        loadingThread.start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAddKeyForm() {
        contentArea.getChildren().clear();

        // Create game selection dropdown
        ComboBox<Game> gameComboBox = new ComboBox<>();
        try {
            List<Game> games = dbManager.getAllGames();
            gameComboBox.getItems().addAll(games);
            gameComboBox.setPromptText("Select Game");
        } catch (SQLException e) {
            showAlert("Error", "Failed to load games");
            return;
        }

        // Create key input field
        TextField keyField = new TextField();
        keyField.setPromptText("Game Key (Format: XXXXX-XXXXX-XXXXX)");

        // Add generate key button
        Button generateKeyBtn = new Button("Generate Random Key");
        generateKeyBtn.setOnAction(e -> {
            String randomKey = generateGameKey();
            keyField.setText(randomKey);
        });

        // Add submit button
        Button submitBtn = new Button("Add Key");
        submitBtn.setOnAction(e -> {
            if (validateGameKey(keyField.getText()) && gameComboBox.getValue() != null) {
                try {
                    GameKey gameKey = new GameKey(
                        gameComboBox.getValue().getGameId(),
                        keyField.getText()
                    );
                    dbManager.addGameKey(gameKey);
                    showAlert("Success", "Game key added successfully!");
                    keyField.clear();
                    gameComboBox.setValue(null);
                } catch (SQLException ex) {
                    showAlert("Error", "Failed to add key: " + ex.getMessage());
                }
            } else {
                showAlert("Error", "Invalid key format or no game selected");
            }
        });

        contentArea.getChildren().addAll(
            new Label("Add New Game Key"),
            gameComboBox,
            keyField,
            generateKeyBtn,
            submitBtn
        );
    }

    private String generateGameKey() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder key = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 5; j++) {
                key.append(chars.charAt(random.nextInt(chars.length())));
            }
            if (i < 2) key.append("-");
        }
        
        return key.toString();
    }

    private boolean validateGameKey(String key) {
        return key.matches("^[A-Z0-9]{5}-[A-Z0-9]{5}-[A-Z0-9]{5}$");
    }
} 