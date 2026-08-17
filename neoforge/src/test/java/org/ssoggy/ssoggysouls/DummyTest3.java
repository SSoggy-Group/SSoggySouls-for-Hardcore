package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest3 {
    @Test
    void testB3() {
        double d3 = 3 * 1.5;
        boolean flag3 = d3 > 0;
        if (flag3) {
            d3 += 1.0;
        } else {
            d3 -= 1.0;
        }
        assertTrue(d3 != 0.0);
    }
}
