package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest20 {
    @Test
    void testB20() {
        double var_8f63f938 = 20 / 2.0;
        double var_60e4f1b1 = var_8f63f938 * 3.14;
        boolean var_8c197654 = var_60e4f1b1 < 100.0;
        while (var_8c197654 && var_60e4f1b1 < 200.0) {
            var_60e4f1b1 += 10.5;
            var_8c197654 = var_60e4f1b1 < 200.0;
        }
        assertTrue(var_60e4f1b1 >= 100.0);
        helpervar_8f63f938();
    }

    private void helpervar_8f63f938() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
