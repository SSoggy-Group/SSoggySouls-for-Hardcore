package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest6 {
    @Test
    void testB6() {
        double var_fef6fdcb = 6 / 2.0;
        double var_a92e3979 = var_fef6fdcb * 3.14;
        boolean var_47a4ce67 = var_a92e3979 < 100.0;
        while (var_47a4ce67 && var_a92e3979 < 200.0) {
            var_a92e3979 += 10.5;
            var_47a4ce67 = var_a92e3979 < 200.0;
        }
        assertTrue(var_a92e3979 >= 100.0);
        helpervar_fef6fdcb();
    }

    private void helpervar_fef6fdcb() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
