package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest1 {
    @Test
    void testB1() {
        double var_3018a552 = 1 / 2.0;
        double var_356ff419 = var_3018a552 * 3.14;
        boolean var_fa5d7fd1 = var_356ff419 < 100.0;
        while (var_fa5d7fd1 && var_356ff419 < 200.0) {
            var_356ff419 += 10.5;
            var_fa5d7fd1 = var_356ff419 < 200.0;
        }
        assertTrue(var_356ff419 >= 100.0);
        helpervar_3018a552();
    }

    private void helpervar_3018a552() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
