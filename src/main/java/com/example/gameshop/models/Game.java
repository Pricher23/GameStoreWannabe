package com.example.gameshop.models;

public class Game {
    private int gameId;
    private String title;
    private String description;
    private double price;
    
    // New fields for Steam integration
    private int appId;
    private GameDetails details;
    private int playtimeMinutes;
    private int playtime;
    private String developer;
    private String publisher;
    private String genre;

    // Original constructor
    public Game(String title, String description, double price) {
        this.title = title;
        this.description = description;
        this.price = price;
    }

    // New constructor for Steam games
    public Game(String name, GameDetails details, int playtimeMinutes) {
        this.title = name;
        this.details = details;
        this.playtimeMinutes = playtimeMinutes;
        this.description = details.getDescription();
        this.price = details.getPrice();
    }

    // Original getters and setters
    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    // New getters and setters for Steam fields
    public int getAppId() { return appId; }
    public void setAppId(int appId) { this.appId = appId; }
    public GameDetails getDetails() { return details; }
    public void setDetails(GameDetails details) { this.details = details; }
    public int getPlaytimeMinutes() { return playtimeMinutes; }
    public void setPlaytimeMinutes(int playtimeMinutes) { this.playtimeMinutes = playtimeMinutes; }
    public String getName() { return title; } // Alias for getTitle() to maintain compatibility
    public int getPlaytime() { return playtime; }
    public void setPlaytime(int playtime) { this.playtime = playtime; }

    // Add getters and setters
    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    @Override
    public String toString() {
        return String.format("%s - $%.2f", this.getTitle(), this.getPrice());
    }
} 