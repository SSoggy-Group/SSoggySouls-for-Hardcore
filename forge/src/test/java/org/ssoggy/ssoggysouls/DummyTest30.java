package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest30 {
    @Test
    void testA() {
        int val = 30;
        String result = "Test" + val;
        assertTrue(result.equals("Test30"));
    }
}
