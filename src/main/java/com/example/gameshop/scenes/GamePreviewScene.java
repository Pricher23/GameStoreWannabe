package com.example.gameshop.scenes;

import com.example.gameshop.models.Game;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GamePreviewScene {
    private Stage stage;
    private Game game;
    
    public GamePreviewScene(Stage stage, Game game) {
        this.stage = stage;
        this.game = game;
        createScene();
    }
    
    private void createScene() {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #2b2b2b;");
        
        // Top section with back button
        HBox topSection = new HBox(10);
        topSection.setStyle("-fx-background-color: #3c3f41; -fx-padding: 10;");
        
        Button backButton = new Button("Back");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> new AccountScene(stage));
        
        topSection.getChildren().add(backButton);
        layout.setTop(topSection);
        
        // Game details section
        VBox detailsBox = new VBox(15);
        detailsBox.setPadding(new Insets(20));
        
        Label titleLabel = new Label(game.getTitle());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label developerLabel = new Label("Developer: " + game.getDetails().getDeveloper());
        developerLabel.setStyle("-fx-text-fill: white;");
        
        Label genreLabel = new Label("Genre: " + game.getDetails().getGenre());
        genreLabel.setStyle("-fx-text-fill: white;");
        
        Label priceLabel = new Label(String.format("Price: $%.2f", game.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #4b6eaf; -fx-font-size: 18px;");
        
        Label playtimeLabel = new Label(
            String.format("Hours played: %.1f", game.getPlaytimeMinutes() / 60.0)
        );
        playtimeLabel.setStyle("-fx-text-fill: white;");
        
        TextArea descriptionArea = new TextArea(game.getDetails().getDescription());
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        descriptionArea.setPrefRowCount(10);
        descriptionArea.getStyleClass().add("dark-textarea");
        
        detailsBox.getChildren().addAll(
            titleLabel, developerLabel, genreLabel, 
            priceLabel, playtimeLabel, descriptionArea
        );
        
        layout.setCenter(detailsBox);
        
        Scene scene = new Scene(layout, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        stage.setScene(scene);
    }
} 