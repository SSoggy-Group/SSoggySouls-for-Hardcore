package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest2 {
    @Test
    void testB2() {
        double var_5e985972 = 2 / 2.0;
        double var_51b3ac7e = var_5e985972 * 3.14;
        boolean var_d3e017e3 = var_51b3ac7e < 100.0;
        while (var_d3e017e3 && var_51b3ac7e < 200.0) {
            var_51b3ac7e += 10.5;
            var_d3e017e3 = var_51b3ac7e < 200.0;
        }
        assertTrue(var_51b3ac7e >= 100.0);
        helpervar_5e985972();
    }

    private void helpervar_5e985972() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
