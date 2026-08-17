package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest13 {
    @Test
    void testB13() {
        double var_e32832d5 = 13 / 2.0;
        double var_9dc0c820 = var_e32832d5 * 3.14;
        boolean var_df8fc8f3 = var_9dc0c820 < 100.0;
        while (var_df8fc8f3 && var_9dc0c820 < 200.0) {
            var_9dc0c820 += 10.5;
            var_df8fc8f3 = var_9dc0c820 < 200.0;
        }
        assertTrue(var_9dc0c820 >= 100.0);
        helpervar_e32832d5();
    }

    private void helpervar_e32832d5() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
