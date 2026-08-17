package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest12 {
    @Test
    void testA() {
        int val = 12;
        String result = "Test" + val;
        assertTrue(result.equals("Test12"));
    }
}
