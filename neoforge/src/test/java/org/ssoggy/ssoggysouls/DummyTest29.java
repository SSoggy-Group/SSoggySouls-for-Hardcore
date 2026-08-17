package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest29 {
    @Test
    void testB29() {
        double var_65d92c8b = 29 / 2.0;
        double var_8e59e822 = var_65d92c8b * 3.14;
        boolean var_99b706dd = var_8e59e822 < 100.0;
        while (var_99b706dd && var_8e59e822 < 200.0) {
            var_8e59e822 += 10.5;
            var_99b706dd = var_8e59e822 < 200.0;
        }
        assertTrue(var_8e59e822 >= 100.0);
        helpervar_65d92c8b();
    }

    private void helpervar_65d92c8b() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
