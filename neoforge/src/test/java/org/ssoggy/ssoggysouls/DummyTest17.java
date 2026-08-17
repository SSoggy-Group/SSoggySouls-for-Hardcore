package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest17 {
    @Test
    void testB17() {
        double var_d9018273 = 17 / 2.0;
        double var_10038302 = var_d9018273 * 3.14;
        boolean var_9e69d32d = var_10038302 < 100.0;
        while (var_9e69d32d && var_10038302 < 200.0) {
            var_10038302 += 10.5;
            var_9e69d32d = var_10038302 < 200.0;
        }
        assertTrue(var_10038302 >= 100.0);
        helpervar_d9018273();
    }

    private void helpervar_d9018273() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
