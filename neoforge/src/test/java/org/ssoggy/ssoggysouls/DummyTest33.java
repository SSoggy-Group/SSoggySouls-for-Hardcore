package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest33 {
    @Test
    void testB33() {
        double var_74054f63 = 33 / 2.0;
        double var_d3811bb4 = var_74054f63 * 3.14;
        boolean var_12180025 = var_d3811bb4 < 100.0;
        while (var_12180025 && var_d3811bb4 < 200.0) {
            var_d3811bb4 += 10.5;
            var_12180025 = var_d3811bb4 < 200.0;
        }
        assertTrue(var_d3811bb4 >= 100.0);
        helpervar_74054f63();
    }

    private void helpervar_74054f63() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
