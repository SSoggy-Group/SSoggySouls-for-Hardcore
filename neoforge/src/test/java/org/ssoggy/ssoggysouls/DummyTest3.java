package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest3 {
    @Test
    void testB3() {
        double var_143b4102 = 3 / 2.0;
        double var_951b9911 = var_143b4102 * 3.14;
        boolean var_5619810a = var_951b9911 < 100.0;
        while (var_5619810a && var_951b9911 < 200.0) {
            var_951b9911 += 10.5;
            var_5619810a = var_951b9911 < 200.0;
        }
        assertTrue(var_951b9911 >= 100.0);
        helpervar_143b4102();
    }

    private void helpervar_143b4102() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
