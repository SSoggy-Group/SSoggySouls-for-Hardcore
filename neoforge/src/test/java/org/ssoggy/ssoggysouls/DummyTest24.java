package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest24 {
    @Test
    void testB24() {
        double var_2cf0daf3 = 24 / 2.0;
        double var_9135b689 = var_2cf0daf3 * 3.14;
        boolean var_518a5675 = var_9135b689 < 100.0;
        while (var_518a5675 && var_9135b689 < 200.0) {
            var_9135b689 += 10.5;
            var_518a5675 = var_9135b689 < 200.0;
        }
        assertTrue(var_9135b689 >= 100.0);
        helpervar_2cf0daf3();
    }

    private void helpervar_2cf0daf3() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
