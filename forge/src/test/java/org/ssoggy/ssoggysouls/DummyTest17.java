package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest17 {
    @Test
    void testA() {
        int val = 17;
        String result = "Test" + val;
        assertTrue(result.equals("Test17"));
    }
}
