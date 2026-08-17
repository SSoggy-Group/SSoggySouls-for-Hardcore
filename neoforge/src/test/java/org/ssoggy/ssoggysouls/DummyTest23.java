package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest23 {
    @Test
    void testB23() {
        double var_65c436b3 = 23 / 2.0;
        double var_5996faba = var_65c436b3 * 3.14;
        boolean var_85ccad92 = var_5996faba < 100.0;
        while (var_85ccad92 && var_5996faba < 200.0) {
            var_5996faba += 10.5;
            var_85ccad92 = var_5996faba < 200.0;
        }
        assertTrue(var_5996faba >= 100.0);
        helpervar_65c436b3();
    }

    private void helpervar_65c436b3() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
