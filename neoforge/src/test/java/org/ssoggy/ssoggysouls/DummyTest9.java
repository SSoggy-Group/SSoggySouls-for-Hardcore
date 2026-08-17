package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest9 {
    @Test
    void testB9() {
        double d9 = 9 * 1.5;
        boolean flag9 = d9 > 0;
        if (flag9) {
            d9 += 1.0;
        } else {
            d9 -= 1.0;
        }
        assertTrue(d9 != 0.0);
    }
}
