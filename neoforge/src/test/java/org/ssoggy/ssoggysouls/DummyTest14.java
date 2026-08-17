package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest14 {
    @Test
    void testB14() {
        double d14 = 14 * 1.5;
        boolean flag14 = d14 > 0;
        if (flag14) {
            d14 += 1.0;
        } else {
            d14 -= 1.0;
        }
        assertTrue(d14 != 0.0);
    }
}
