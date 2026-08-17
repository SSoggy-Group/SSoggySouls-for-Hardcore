package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest7 {
    @Test
    void testB7() {
        double var_a4f4e610 = 7 / 2.0;
        double var_4c23e49a = var_a4f4e610 * 3.14;
        boolean var_f8a4f1e7 = var_4c23e49a < 100.0;
        while (var_f8a4f1e7 && var_4c23e49a < 200.0) {
            var_4c23e49a += 10.5;
            var_f8a4f1e7 = var_4c23e49a < 200.0;
        }
        assertTrue(var_4c23e49a >= 100.0);
        helpervar_a4f4e610();
    }

    private void helpervar_a4f4e610() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
