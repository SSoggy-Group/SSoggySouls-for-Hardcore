package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest8 {
    @Test
    void testB8() {
        double var_564547b9 = 8 / 2.0;
        double var_df94a122 = var_564547b9 * 3.14;
        boolean var_3e27933a = var_df94a122 < 100.0;
        while (var_3e27933a && var_df94a122 < 200.0) {
            var_df94a122 += 10.5;
            var_3e27933a = var_df94a122 < 200.0;
        }
        assertTrue(var_df94a122 >= 100.0);
        helpervar_564547b9();
    }

    private void helpervar_564547b9() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
