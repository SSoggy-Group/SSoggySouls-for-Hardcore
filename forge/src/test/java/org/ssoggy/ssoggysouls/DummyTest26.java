package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest26 {
    @Test
    void testA() {
        int val = 26;
        String result = "Test" + val;
        assertTrue(result.equals("Test26"));
    }
}
