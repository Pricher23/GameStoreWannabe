package com.example.gameshop.models;

public class Game {
    private int gameId;
    private String title;
    private String description;
    private double price;
    
    public Game(String title, String description, double price) {
        this.title = title;
        this.description = description;
        this.price = price;
    }

    // Getters and Setters
    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
} 