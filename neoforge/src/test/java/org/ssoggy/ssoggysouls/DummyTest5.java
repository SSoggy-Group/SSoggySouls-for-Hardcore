package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest5 {
    @Test
    void testB5() {
        double var_1fc602cb = 5 / 2.0;
        double var_deb32ccd = var_1fc602cb * 3.14;
        boolean var_75a9b5d2 = var_deb32ccd < 100.0;
        while (var_75a9b5d2 && var_deb32ccd < 200.0) {
            var_deb32ccd += 10.5;
            var_75a9b5d2 = var_deb32ccd < 200.0;
        }
        assertTrue(var_deb32ccd >= 100.0);
        helpervar_1fc602cb();
    }

    private void helpervar_1fc602cb() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
