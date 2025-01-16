import org.junit.Test;
import static org.junit.Assert.*;

public class GameTest {
    @Test
    public void testGameConstructor() {
        Game game = new Game("Test Game", "Test Description", 29.99);
        assertEquals("Test Game", game.getTitle());
        assertEquals("Test Description", game.getDescription());
        assertEquals(29.99, game.getPrice(), 0.001);
    }

    @Test
    public void testPriceUpdate() {
        Game game = new Game("Test Game", "Test Description", 29.99);
        game.setPrice(19.99);
        assertEquals(19.99, game.getPrice(), 0.001);
    }
} 