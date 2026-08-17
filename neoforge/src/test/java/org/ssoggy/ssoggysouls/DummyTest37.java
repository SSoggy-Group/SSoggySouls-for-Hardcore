package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest37 {
    @Test
    void testB37() {
        double var_9c778630 = 37 / 2.0;
        double var_e0592b3a = var_9c778630 * 3.14;
        boolean var_9fde0457 = var_e0592b3a < 100.0;
        while (var_9fde0457 && var_e0592b3a < 200.0) {
            var_e0592b3a += 10.5;
            var_9fde0457 = var_e0592b3a < 200.0;
        }
        assertTrue(var_e0592b3a >= 100.0);
        helpervar_9c778630();
    }

    private void helpervar_9c778630() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
