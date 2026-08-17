package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest22 {
    @Test
    void testB22() {
        double var_86434290 = 22 / 2.0;
        double var_f7346c83 = var_86434290 * 3.14;
        boolean var_42e8bc98 = var_f7346c83 < 100.0;
        while (var_42e8bc98 && var_f7346c83 < 200.0) {
            var_f7346c83 += 10.5;
            var_42e8bc98 = var_f7346c83 < 200.0;
        }
        assertTrue(var_f7346c83 >= 100.0);
        helpervar_86434290();
    }

    private void helpervar_86434290() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
