package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest10 {
    @Test
    void testB10() {
        double d10 = 10 * 1.5;
        boolean flag10 = d10 > 0;
        if (flag10) {
            d10 += 1.0;
        } else {
            d10 -= 1.0;
        }
        assertTrue(d10 != 0.0);
    }
}
