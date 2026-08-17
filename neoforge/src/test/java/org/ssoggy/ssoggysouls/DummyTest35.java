package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest35 {
    @Test
    void testB35() {
        double var_e60e3d95 = 35 / 2.0;
        double var_616b6808 = var_e60e3d95 * 3.14;
        boolean var_0682af27 = var_616b6808 < 100.0;
        while (var_0682af27 && var_616b6808 < 200.0) {
            var_616b6808 += 10.5;
            var_0682af27 = var_616b6808 < 200.0;
        }
        assertTrue(var_616b6808 >= 100.0);
        helpervar_e60e3d95();
    }

    private void helpervar_e60e3d95() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
