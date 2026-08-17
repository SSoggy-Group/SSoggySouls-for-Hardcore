package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest15 {
    @Test
    void testB15() {
        double var_9bf4ff0e = 15 / 2.0;
        double var_06faa0e5 = var_9bf4ff0e * 3.14;
        boolean var_02b93e6d = var_06faa0e5 < 100.0;
        while (var_02b93e6d && var_06faa0e5 < 200.0) {
            var_06faa0e5 += 10.5;
            var_02b93e6d = var_06faa0e5 < 200.0;
        }
        assertTrue(var_06faa0e5 >= 100.0);
        helpervar_9bf4ff0e();
    }

    private void helpervar_9bf4ff0e() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
