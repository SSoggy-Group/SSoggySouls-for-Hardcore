package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest4 {
    @Test
    void testB4() {
        double d4 = 4 * 1.5;
        boolean flag4 = d4 > 0;
        if (flag4) {
            d4 += 1.0;
        } else {
            d4 -= 1.0;
        }
        assertTrue(d4 != 0.0);
    }
}
