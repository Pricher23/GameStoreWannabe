import org.junit.Test;
import static org.junit.Assert.*;

public class UserTest {
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
    public void testBalanceUpdate() {
        User user = new User("testUser", "password123", "test@email.com");
        user.setBalance(100.0);
        assertEquals(100.0, user.getBalance(), 0.001);
    }

    @Test
    public void testRoleUpdate() {
        User user = new User("testUser", "password123", "test@email.com");
        user.setRole("ADMIN");
        assertEquals("ADMIN", user.getRole());
    }
} 