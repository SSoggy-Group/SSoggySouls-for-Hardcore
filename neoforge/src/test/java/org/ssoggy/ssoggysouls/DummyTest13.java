package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest13 {
    @Test
    void testB13() {
        double d13 = 13 * 1.5;
        boolean flag13 = d13 > 0;
        if (flag13) {
            d13 += 1.0;
        } else {
            d13 -= 1.0;
        }
        assertTrue(d13 != 0.0);
    }
}
