package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest16 {
    @Test
    void testB16() {
        double var_eba78d36 = 16 / 2.0;
        double var_7526651c = var_eba78d36 * 3.14;
        boolean var_befb8026 = var_7526651c < 100.0;
        while (var_befb8026 && var_7526651c < 200.0) {
            var_7526651c += 10.5;
            var_befb8026 = var_7526651c < 200.0;
        }
        assertTrue(var_7526651c >= 100.0);
        helpervar_eba78d36();
    }

    private void helpervar_eba78d36() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
