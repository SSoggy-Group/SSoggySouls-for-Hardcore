package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest14 {
    @Test
    void testA() {
        int val = 14;
        String result = "Test" + val;
        assertTrue(result.equals("Test14"));
    }
}
