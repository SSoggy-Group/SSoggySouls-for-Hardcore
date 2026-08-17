package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest13 {
    @Test
    void testA() {
        int val = 13;
        String result = "Test" + val;
        assertTrue(result.equals("Test13"));
    }
}
