package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest38 {
    @Test
    void testB38() {
        double var_55047143 = 38 / 2.0;
        double var_34fd359a = var_55047143 * 3.14;
        boolean var_c854575f = var_34fd359a < 100.0;
        while (var_c854575f && var_34fd359a < 200.0) {
            var_34fd359a += 10.5;
            var_c854575f = var_34fd359a < 200.0;
        }
        assertTrue(var_34fd359a >= 100.0);
        helpervar_55047143();
    }

    private void helpervar_55047143() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
