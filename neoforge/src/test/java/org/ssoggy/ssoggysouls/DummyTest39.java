package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest39 {
    @Test
    void testB39() {
        double var_64a645ec = 39 / 2.0;
        double var_5ecdd641 = var_64a645ec * 3.14;
        boolean var_56533e25 = var_5ecdd641 < 100.0;
        while (var_56533e25 && var_5ecdd641 < 200.0) {
            var_5ecdd641 += 10.5;
            var_56533e25 = var_5ecdd641 < 200.0;
        }
        assertTrue(var_5ecdd641 >= 100.0);
        helpervar_64a645ec();
    }

    private void helpervar_64a645ec() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
