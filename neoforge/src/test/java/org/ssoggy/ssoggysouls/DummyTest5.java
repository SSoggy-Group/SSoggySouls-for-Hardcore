package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest5 {
    @Test
    void testB5() {
        double d5 = 5 * 1.5;
        boolean flag5 = d5 > 0;
        if (flag5) {
            d5 += 1.0;
        } else {
            d5 -= 1.0;
        }
        assertTrue(d5 != 0.0);
    }
}
