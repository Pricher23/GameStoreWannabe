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

public class AccountScene {
    private Stage stage;
    private DatabaseManager dbManager;
    private FlowPane gamesContainer;
    private User currentUser;

    public AccountScene(Stage stage) {
        this.stage = stage;
        this.dbManager = new DatabaseManager();
        this.currentUser = GameShopApp.getCurrentUser();
        createAccountScene();
    }

    private void createAccountScene() {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #2b2b2b;");
        
        // Top menu
        MenuBar menuBar = createMenuBar();
        layout.setTop(menuBar);

        // User info section
        VBox userInfo = createUserInfoSection();
        layout.setLeft(userInfo);

        // Games list
        gamesContainer = new FlowPane();
        gamesContainer.setHgap(10);
        gamesContainer.setVgap(10);
        gamesContainer.setPadding(new Insets(10));
        gamesContainer.setStyle("-fx-background-color: #2b2b2b;");

        ScrollPane scrollPane = new ScrollPane(gamesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b;");
        layout.setCenter(scrollPane);

        loadOwnedGames();

        Scene scene = new Scene(layout, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        stage.setScene(scene);
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

        userInfo.getChildren().addAll(
            titleLabel,
            new Separator(),
            usernameLabel,
            emailLabel,
            balanceLabel,
            new Separator(),
            addFundsBtn
        );

        return userInfo;
    }

    private void loadOwnedGames() {
        Thread loadingThread = new Thread(() -> {
            try {
                List<UserGame> ownedGames = dbManager.getUserGames(currentUser.getUserId());
                Platform.runLater(() -> {
                    gamesContainer.getChildren().clear();
                    for (UserGame userGame : ownedGames) {
                        gamesContainer.getChildren().add(createGameCard(userGame));
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load games"));
            }
        });
        loadingThread.start();
    }

    private VBox createGameCard(UserGame userGame) {
        VBox card = new VBox(5);
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: #3c3f41; " +
                     "-fx-border-color: #555555; " +
                     "-fx-border-radius: 5; " +
                     "-fx-padding: 10; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0, 0, 0);");

        Label titleLabel = new Label(userGame.getGame().getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: white;");

        Button showKeyBtn = new Button("Show Key");
        showKeyBtn.getStyleClass().add("accent-button");
        showKeyBtn.setMaxWidth(Double.MAX_VALUE);
        showKeyBtn.setOnAction(e -> showGameKey(userGame.getGameKey()));

        card.getChildren().addAll(titleLabel, showKeyBtn);
        return card;
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
                    loadOwnedGames(); // Refresh the view
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
} 