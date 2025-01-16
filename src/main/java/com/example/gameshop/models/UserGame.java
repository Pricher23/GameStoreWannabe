package com.example.gameshop.models;

public class UserGame {
    private Game game;
    private GameKey gameKey;

    public UserGame(Game game, GameKey gameKey) {
        this.game = game;
        this.gameKey = gameKey;
    }

    public Game getGame() {
        return game;
    }

    public GameKey getGameKey() {
        return gameKey;
    }
} 