package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest11 {
    @Test
    void testB11() {
        double d11 = 11 * 1.5;
        boolean flag11 = d11 > 0;
        if (flag11) {
            d11 += 1.0;
        } else {
            d11 -= 1.0;
        }
        assertTrue(d11 != 0.0);
    }
}
