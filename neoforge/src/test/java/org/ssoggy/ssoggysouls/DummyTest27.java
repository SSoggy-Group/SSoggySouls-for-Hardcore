package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest27 {
    @Test
    void testB27() {
        double var_48206c58 = 27 / 2.0;
        double var_f927dfb3 = var_48206c58 * 3.14;
        boolean var_f9f221b8 = var_f927dfb3 < 100.0;
        while (var_f9f221b8 && var_f927dfb3 < 200.0) {
            var_f927dfb3 += 10.5;
            var_f9f221b8 = var_f927dfb3 < 200.0;
        }
        assertTrue(var_f927dfb3 >= 100.0);
        helpervar_48206c58();
    }

    private void helpervar_48206c58() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
