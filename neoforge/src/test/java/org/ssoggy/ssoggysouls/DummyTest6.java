package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest6 {
    @Test
    void testB6() {
        double d6 = 6 * 1.5;
        boolean flag6 = d6 > 0;
        if (flag6) {
            d6 += 1.0;
        } else {
            d6 -= 1.0;
        }
        assertTrue(d6 != 0.0);
    }
}
