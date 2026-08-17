package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest36 {
    @Test
    void testB36() {
        double var_a3581b2c = 36 / 2.0;
        double var_02738f8b = var_a3581b2c * 3.14;
        boolean var_b352aa6f = var_02738f8b < 100.0;
        while (var_b352aa6f && var_02738f8b < 200.0) {
            var_02738f8b += 10.5;
            var_b352aa6f = var_02738f8b < 200.0;
        }
        assertTrue(var_02738f8b >= 100.0);
        helpervar_a3581b2c();
    }

    private void helpervar_a3581b2c() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
