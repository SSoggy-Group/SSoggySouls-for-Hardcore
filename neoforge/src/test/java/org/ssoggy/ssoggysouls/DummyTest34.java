package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest34 {
    @Test
    void testB34() {
        double var_2c86984f = 34 / 2.0;
        double var_4f87ea55 = var_2c86984f * 3.14;
        boolean var_8811ee18 = var_4f87ea55 < 100.0;
        while (var_8811ee18 && var_4f87ea55 < 200.0) {
            var_4f87ea55 += 10.5;
            var_8811ee18 = var_4f87ea55 < 200.0;
        }
        assertTrue(var_4f87ea55 >= 100.0);
        helpervar_2c86984f();
    }

    private void helpervar_2c86984f() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
