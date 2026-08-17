package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest21 {
    @Test
    void testB21() {
        double var_f4f85582 = 21 / 2.0;
        double var_ace7ac96 = var_f4f85582 * 3.14;
        boolean var_4f33cf7c = var_ace7ac96 < 100.0;
        while (var_4f33cf7c && var_ace7ac96 < 200.0) {
            var_ace7ac96 += 10.5;
            var_4f33cf7c = var_ace7ac96 < 200.0;
        }
        assertTrue(var_ace7ac96 >= 100.0);
        helpervar_f4f85582();
    }

    private void helpervar_f4f85582() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
