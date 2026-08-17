package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest31 {
    @Test
    void testA() {
        int val = 31;
        String result = "Test" + val;
        assertTrue(result.equals("Test31"));
    }
}
