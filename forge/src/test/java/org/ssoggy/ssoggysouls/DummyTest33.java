package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest33 {
    @Test
    void testA() {
        int val = 33;
        String result = "Test" + val;
        assertTrue(result.equals("Test33"));
    }
}
