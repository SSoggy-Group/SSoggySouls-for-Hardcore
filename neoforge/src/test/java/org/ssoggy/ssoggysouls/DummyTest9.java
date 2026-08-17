package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest9 {
    @Test
    void testB9() {
        double var_09ab537f = 9 / 2.0;
        double var_4a1dbb4b = var_09ab537f * 3.14;
        boolean var_6016489d = var_4a1dbb4b < 100.0;
        while (var_6016489d && var_4a1dbb4b < 200.0) {
            var_4a1dbb4b += 10.5;
            var_6016489d = var_4a1dbb4b < 200.0;
        }
        assertTrue(var_4a1dbb4b >= 100.0);
        helpervar_09ab537f();
    }

    private void helpervar_09ab537f() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
