package com.example.gameshop.models;

import org.junit.Test;
import static org.junit.Assert.*;

public class GameKeyTest {
    @Test
    public void testGameKeyConstructor() {
        GameKey key = new GameKey(1, "ABCDE-12345-FGHIJ");
        assertEquals(1, key.getGameId());
        assertEquals("ABCDE-12345-FGHIJ", key.getKeyValue());
        assertFalse(key.isSold());
    }

    @Test
    public void testGameKeySetters() {
        GameKey key = new GameKey(1, "ABCDE-12345-FGHIJ");
        key.setKeyId(5);
        key.setSold(true);

        assertEquals(5, key.getKeyId());
        assertTrue(key.isSold());
    }

    @Test
    public void testKeyFormat() {
        GameKey key = new GameKey(1, "ABCDE-12345-FGHIJ");
        assertTrue(key.getKeyValue().matches("^[A-Z0-9]{5}-[A-Z0-9]{5}-[A-Z0-9]{5}$"));
    }
} 