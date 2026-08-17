package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest5 {
    @Test
    void testA() {
        int val = 5;
        String result = "Test" + val;
        assertTrue(result.equals("Test5"));
    }
}
