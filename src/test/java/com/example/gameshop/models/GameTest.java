package com.example.gameshop.models;

import org.junit.Test;
import static org.junit.Assert.*;

public class GameTest {
    @Test
    public void testGameConstructor() {
        Game game = new Game("Minecraft", "A sandbox game", 29.99);
        assertEquals("Minecraft", game.getTitle());
        assertEquals("A sandbox game", game.getDescription());
        assertEquals(29.99, game.getPrice(), 0.001);
    }

    @Test
    public void testGameSetters() {
        Game game = new Game("Test", "Description", 19.99);
        game.setGameId(1);
        game.setTitle("New Title");
        game.setDescription("New Description");
        game.setPrice(24.99);

        assertEquals(1, game.getGameId());
        assertEquals("New Title", game.getTitle());
        assertEquals("New Description", game.getDescription());
        assertEquals(24.99, game.getPrice(), 0.001);
    }

    @Test
    public void testInvalidPrice() {
        Game game = new Game("Test", "Description", 19.99);
        assertThrows(IllegalArgumentException.class, () -> game.setPrice(-10.0));
    }
} 