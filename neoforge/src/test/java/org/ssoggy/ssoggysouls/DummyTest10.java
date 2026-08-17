package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest10 {
    @Test
    void testB10() {
        double var_8f48190c = 10 / 2.0;
        double var_2c88e697 = var_8f48190c * 3.14;
        boolean var_4576b3c6 = var_2c88e697 < 100.0;
        while (var_4576b3c6 && var_2c88e697 < 200.0) {
            var_2c88e697 += 10.5;
            var_4576b3c6 = var_2c88e697 < 200.0;
        }
        assertTrue(var_2c88e697 >= 100.0);
        helpervar_8f48190c();
    }

    private void helpervar_8f48190c() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
