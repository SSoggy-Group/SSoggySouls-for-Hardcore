package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest32 {
    @Test
    void testB32() {
        double var_d0d6afac = 32 / 2.0;
        double var_52982aac = var_d0d6afac * 3.14;
        boolean var_5cb3ac1e = var_52982aac < 100.0;
        while (var_5cb3ac1e && var_52982aac < 200.0) {
            var_52982aac += 10.5;
            var_5cb3ac1e = var_52982aac < 200.0;
        }
        assertTrue(var_52982aac >= 100.0);
        helpervar_d0d6afac();
    }

    private void helpervar_d0d6afac() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
