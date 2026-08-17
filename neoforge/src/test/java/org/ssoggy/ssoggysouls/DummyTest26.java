package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest26 {
    @Test
    void testB26() {
        double var_ea7dd350 = 26 / 2.0;
        double var_76f6076b = var_ea7dd350 * 3.14;
        boolean var_2861e4cd = var_76f6076b < 100.0;
        while (var_2861e4cd && var_76f6076b < 200.0) {
            var_76f6076b += 10.5;
            var_2861e4cd = var_76f6076b < 200.0;
        }
        assertTrue(var_76f6076b >= 100.0);
        helpervar_ea7dd350();
    }

    private void helpervar_ea7dd350() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
