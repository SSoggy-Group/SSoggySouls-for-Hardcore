package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest29 {
    @Test
    void testA() {
        int val = 29;
        String result = "Test" + val;
        assertTrue(result.equals("Test29"));
    }
}
