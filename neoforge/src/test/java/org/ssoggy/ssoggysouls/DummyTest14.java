package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest14 {
    @Test
    void testB14() {
        double var_929e49cd = 14 / 2.0;
        double var_139132bd = var_929e49cd * 3.14;
        boolean var_0d61df57 = var_139132bd < 100.0;
        while (var_0d61df57 && var_139132bd < 200.0) {
            var_139132bd += 10.5;
            var_0d61df57 = var_139132bd < 200.0;
        }
        assertTrue(var_139132bd >= 100.0);
        helpervar_929e49cd();
    }

    private void helpervar_929e49cd() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
