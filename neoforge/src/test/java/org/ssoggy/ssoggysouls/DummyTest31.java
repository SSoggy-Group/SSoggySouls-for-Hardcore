package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest31 {
    @Test
    void testB31() {
        double var_113331a2 = 31 / 2.0;
        double var_6175080a = var_113331a2 * 3.14;
        boolean var_a2be68a7 = var_6175080a < 100.0;
        while (var_a2be68a7 && var_6175080a < 200.0) {
            var_6175080a += 10.5;
            var_a2be68a7 = var_6175080a < 200.0;
        }
        assertTrue(var_6175080a >= 100.0);
        helpervar_113331a2();
    }

    private void helpervar_113331a2() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
