package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest45 {
    @Test
    void testA() {
        int val = 45;
        String result = "Test" + val;
        assertTrue(result.equals("Test45"));
    }
}
