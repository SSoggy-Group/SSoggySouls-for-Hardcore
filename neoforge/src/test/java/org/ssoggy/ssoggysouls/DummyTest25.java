package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest25 {
    @Test
    void testB25() {
        double var_bbb45b5c = 25 / 2.0;
        double var_29c737fb = var_bbb45b5c * 3.14;
        boolean var_f735a0cd = var_29c737fb < 100.0;
        while (var_f735a0cd && var_29c737fb < 200.0) {
            var_29c737fb += 10.5;
            var_f735a0cd = var_29c737fb < 200.0;
        }
        assertTrue(var_29c737fb >= 100.0);
        helpervar_bbb45b5c();
    }

    private void helpervar_bbb45b5c() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
