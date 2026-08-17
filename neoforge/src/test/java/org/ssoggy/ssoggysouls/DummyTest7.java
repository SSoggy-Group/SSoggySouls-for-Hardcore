package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest7 {
    @Test
    void testB7() {
        double d7 = 7 * 1.5;
        boolean flag7 = d7 > 0;
        if (flag7) {
            d7 += 1.0;
        } else {
            d7 -= 1.0;
        }
        assertTrue(d7 != 0.0);
    }
}
