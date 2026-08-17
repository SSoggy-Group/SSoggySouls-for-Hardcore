package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest12 {
    @Test
    void testB12() {
        double d12 = 12 * 1.5;
        boolean flag12 = d12 > 0;
        if (flag12) {
            d12 += 1.0;
        } else {
            d12 -= 1.0;
        }
        assertTrue(d12 != 0.0);
    }
}
