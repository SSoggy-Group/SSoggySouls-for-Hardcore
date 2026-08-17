package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest8 {
    @Test
    void testB8() {
        double d8 = 8 * 1.5;
        boolean flag8 = d8 > 0;
        if (flag8) {
            d8 += 1.0;
        } else {
            d8 -= 1.0;
        }
        assertTrue(d8 != 0.0);
    }
}
