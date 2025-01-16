package com.example.gameshop.models;

public class GameDetails {
    private String developer;
    private String publisher;
    private String genre;
    private String description;
    private double price;
    
    public GameDetails(String developer, String publisher, String genre, String description, double price) {
        this.developer = developer;
        this.publisher = publisher;
        this.genre = genre;
        this.description = description;
        this.price = price;
    }
    
    // Getters
    public String getDeveloper() { return developer; }
    public String getPublisher() { return publisher; }
    public String getGenre() { return genre; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }

    // Setters
    public void setDeveloper(String developer) { this.developer = developer; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
} 