package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest19 {
    @Test
    void testA() {
        int val = 19;
        String result = "Test" + val;
        assertTrue(result.equals("Test19"));
    }
}
