@Test
public void testUserConstructor() {
    User user = new User("testUser", "password123", "test@email.com");
    assertEquals("testUser", user.getUsername());
    assertEquals("password123", user.getPassword());
    assertEquals("test@email.com", user.getEmail());
    assertEquals("CUSTOMER", user.getRole());
    assertEquals(0.0, user.getBalance(), 0.001);
}

@Test
public void testPurchaseGame() {
    User user = new User("testUser", "password123", "test@email.com");
    user.setBalance(100.0);
    Game game = new Game("TestGame", "Description", 50.0);
    
    boolean result = user.purchaseGame(game);
    
    assertTrue(result);
    assertEquals(50.0, user.getBalance(), 0.001);
}

@Test
public void testInsufficientFunds() {
    User user = new User("testUser", "password123", "test@email.com");
    user.setBalance(30.0);
    Game game = new Game("TestGame", "Description", 50.0);
    
    boolean result = user.purchaseGame(game);
    
    assertFalse(result);
    assertEquals(30.0, user.getBalance(), 0.001);
} 