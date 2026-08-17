package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest18 {
    @Test
    void testB18() {
        double var_25cfa51b = 18 / 2.0;
        double var_ddc95df2 = var_25cfa51b * 3.14;
        boolean var_4aa19f0c = var_ddc95df2 < 100.0;
        while (var_4aa19f0c && var_ddc95df2 < 200.0) {
            var_ddc95df2 += 10.5;
            var_4aa19f0c = var_ddc95df2 < 200.0;
        }
        assertTrue(var_ddc95df2 >= 100.0);
        helpervar_25cfa51b();
    }

    private void helpervar_25cfa51b() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
