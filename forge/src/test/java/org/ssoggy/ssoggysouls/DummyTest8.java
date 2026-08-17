package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest8 {
    @Test
    void testA() {
        int val = 8;
        String result = "Test" + val;
        assertTrue(result.equals("Test8"));
    }
}
