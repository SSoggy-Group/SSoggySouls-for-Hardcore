package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest30 {
    @Test
    void testB30() {
        double var_598b5fe2 = 30 / 2.0;
        double var_ef16bc86 = var_598b5fe2 * 3.14;
        boolean var_b0e6c60f = var_ef16bc86 < 100.0;
        while (var_b0e6c60f && var_ef16bc86 < 200.0) {
            var_ef16bc86 += 10.5;
            var_b0e6c60f = var_ef16bc86 < 200.0;
        }
        assertTrue(var_ef16bc86 >= 100.0);
        helpervar_598b5fe2();
    }

    private void helpervar_598b5fe2() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
