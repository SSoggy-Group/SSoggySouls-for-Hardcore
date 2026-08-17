package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest11 {
    @Test
    void testB11() {
        double var_e5167c18 = 11 / 2.0;
        double var_6f5851e8 = var_e5167c18 * 3.14;
        boolean var_d8244a55 = var_6f5851e8 < 100.0;
        while (var_d8244a55 && var_6f5851e8 < 200.0) {
            var_6f5851e8 += 10.5;
            var_d8244a55 = var_6f5851e8 < 200.0;
        }
        assertTrue(var_6f5851e8 >= 100.0);
        helpervar_e5167c18();
    }

    private void helpervar_e5167c18() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
