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
import com.example.gameshop.utils.ThreadPool;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

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
        layout.setStyle("-fx-background-color: #2b2b2b;");

        // Top menu
        MenuBar menuBar = createMenuBar();
        layout.setTop(menuBar);

        // Left sidebar with buttons
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle("-fx-background-color: #3c3f41;");

        Button addGameBtn = new Button("Add Game");
        Button addKeyBtn = new Button("Add Game Key");
        Button viewUsersBtn = new Button("View Users");
        Button viewGamesBtn = new Button("View Games"); // New button

        // Style buttons
        addGameBtn.getStyleClass().add("accent-button");
        addKeyBtn.getStyleClass().add("accent-button");
        viewUsersBtn.getStyleClass().add("accent-button");
        viewGamesBtn.getStyleClass().add("accent-button");

        sidebar.getChildren().addAll(addGameBtn, addKeyBtn, viewUsersBtn, viewGamesBtn);
        layout.setLeft(sidebar);

        // Content area
        contentArea = new VBox(10);
        contentArea.setPadding(new Insets(10));
        contentArea.setStyle("-fx-background-color: #2b2b2b;");
        layout.setCenter(contentArea);

        addGameBtn.setOnAction(e -> showAddGameForm());
        addKeyBtn.setOnAction(e -> showAddKeyForm());
        viewUsersBtn.setOnAction(e -> showUserList());
        viewGamesBtn.setOnAction(e -> showGameList());

        Scene scene = new Scene(layout, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
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

        Label titleLabel = new Label("Add New Game Key");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");

        // Create game selection dropdown with actual game names
        ComboBox<Game> gameComboBox = new ComboBox<>();
        gameComboBox.setPromptText("Select Game");
        gameComboBox.setPrefWidth(300);
        gameComboBox.setCellFactory(lv -> new ListCell<Game>() {
            @Override
            protected void updateItem(Game game, boolean empty) {
                super.updateItem(game, empty);
                if (empty || game == null) {
                    setText(null);
                } else {
                    setText(game.getTitle());
                }
            }
        });
        gameComboBox.setButtonCell(gameComboBox.getCellFactory().call(null));

        // Load games into combo box
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #4b6eaf;");
        contentArea.getChildren().add(loadingIndicator);

        ThreadPool.execute(() -> {
            try {
                List<Game> games = dbManager.getAllGames();
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    gameComboBox.getItems().addAll(games);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    showAlert("Error", "Failed to load games");
                });
            }
        });

        TextField keyField = new TextField();
        keyField.setPromptText("Game Key (Format: XXXXX-XXXXX-XXXXX)");
        keyField.getStyleClass().add("dark-field");

        Button generateKeyBtn = new Button("Generate Random Key");
        generateKeyBtn.getStyleClass().add("accent-button");
        generateKeyBtn.setOnAction(e -> keyField.setText(generateGameKey()));

        Button submitBtn = new Button("Add Key");
        submitBtn.getStyleClass().add("accent-button");
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

        VBox form = new VBox(10);
        form.getChildren().addAll(
            titleLabel,
            new Separator(),
            gameComboBox,
            keyField,
            generateKeyBtn,
            submitBtn
        );

        contentArea.getChildren().add(form);
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

    private void showGameList() {
        contentArea.getChildren().clear();
        
        Label titleLabel = new Label("Game List");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // Create loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #4b6eaf;");
        
        // Center the loading indicator
        StackPane loadingPane = new StackPane(loadingIndicator);
        loadingPane.setPadding(new Insets(20));
        contentArea.getChildren().addAll(titleLabel, new Separator(), loadingPane);

        ThreadPool.execute(() -> {
            try {
                List<Game> games = dbManager.getAllGames();
                Platform.runLater(() -> {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().addAll(titleLabel, new Separator());
                    
                    TableView<Game> gameTable = new TableView<>();
                    gameTable.setStyle("-fx-background-color: #3c3f41;");

                    TableColumn<Game, String> titleCol = new TableColumn<>("Title");
                    TableColumn<Game, String> descCol = new TableColumn<>("Description");
                    TableColumn<Game, Double> priceCol = new TableColumn<>("Price");

                    titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
                    descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
                    priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

                    gameTable.getColumns().addAll(titleCol, descCol, priceCol);
                    gameTable.getItems().addAll(games);

                    contentArea.getChildren().add(gameTable);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().addAll(titleLabel, new Separator());
                    showAlert("Error", "Failed to load games: " + e.getMessage());
                });
            }
        });
    }

    private void showUserList() {
        contentArea.getChildren().clear();

        Label titleLabel = new Label("User List");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");

        TableView<User> userTable = new TableView<>();
        userTable.setStyle("-fx-background-color: #3c3f41;");

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        TableColumn<User, Double> balanceCol = new TableColumn<>("Balance");

        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));

        userTable.getColumns().addAll(usernameCol, emailCol, roleCol, balanceCol);

        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #4b6eaf;");
        contentArea.getChildren().add(loadingIndicator);

        ThreadPool.execute(() -> {
            try {
                List<User> users = dbManager.getAllUsers();
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    userTable.getItems().addAll(users);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    showAlert("Error", "Failed to load users");
                });
            }
        });

        contentArea.getChildren().addAll(titleLabel, new Separator(), userTable);
    }

    // Helper method to create game table
    private TableView<Game> createGameTable(List<Game> games) {
        TableView<Game> gameTable = new TableView<>();
        gameTable.setStyle("-fx-background-color: #3c3f41;");

        TableColumn<Game, String> titleCol = new TableColumn<>("Title");
        TableColumn<Game, String> descCol = new TableColumn<>("Description");
        TableColumn<Game, Double> priceCol = new TableColumn<>("Price");

        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        gameTable.getColumns().addAll(titleCol, descCol, priceCol);
        gameTable.getItems().addAll(games);

        return gameTable;
    }

    // Helper method to create user table
    private TableView<User> createUserTable(List<User> users) {
        TableView<User> userTable = new TableView<>();
        userTable.setStyle("-fx-background-color: #3c3f41;");

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        TableColumn<User, Double> balanceCol = new TableColumn<>("Balance");

        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));

        userTable.getColumns().addAll(usernameCol, emailCol, roleCol, balanceCol);
        userTable.getItems().addAll(users);

        return userTable;
    }
} 