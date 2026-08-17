package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest4 {
    @Test
    void testA() {
        int val = 4;
        String result = "Test" + val;
        assertTrue(result.equals("Test4"));
    }
}
