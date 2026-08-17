package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest19 {
    @Test
    void testB19() {
        double var_35c9926e = 19 / 2.0;
        double var_d06298cc = var_35c9926e * 3.14;
        boolean var_16a78f6c = var_d06298cc < 100.0;
        while (var_16a78f6c && var_d06298cc < 200.0) {
            var_d06298cc += 10.5;
            var_16a78f6c = var_d06298cc < 200.0;
        }
        assertTrue(var_d06298cc >= 100.0);
        helpervar_35c9926e();
    }

    private void helpervar_35c9926e() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
