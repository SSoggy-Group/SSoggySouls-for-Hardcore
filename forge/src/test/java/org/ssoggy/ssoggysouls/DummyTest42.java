package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest42 {
    @Test
    void testA() {
        int val = 42;
        String result = "Test" + val;
        assertTrue(result.equals("Test42"));
    }
}
