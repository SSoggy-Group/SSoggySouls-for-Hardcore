package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest12 {
    @Test
    void testB12() {
        double var_8cf6ae40 = 12 / 2.0;
        double var_7f87b57c = var_8cf6ae40 * 3.14;
        boolean var_ea2315b1 = var_7f87b57c < 100.0;
        while (var_ea2315b1 && var_7f87b57c < 200.0) {
            var_7f87b57c += 10.5;
            var_ea2315b1 = var_7f87b57c < 200.0;
        }
        assertTrue(var_7f87b57c >= 100.0);
        helpervar_8cf6ae40();
    }

    private void helpervar_8cf6ae40() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
