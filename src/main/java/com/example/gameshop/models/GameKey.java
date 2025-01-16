package com.example.gameshop.models;

public class GameKey {
    private int keyId;
    private int gameId;
    private String keyValue;
    private boolean isSold;

    public GameKey(int gameId, String keyValue) {
        this.gameId = gameId;
        this.keyValue = keyValue;
        this.isSold = false;
    }

    // Getters and Setters
    public int getKeyId() { return keyId; }
    public void setKeyId(int keyId) { this.keyId = keyId; }

    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }

    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }

    public boolean isSold() { return isSold; }
    public void setSold(boolean sold) { isSold = sold; }
} 