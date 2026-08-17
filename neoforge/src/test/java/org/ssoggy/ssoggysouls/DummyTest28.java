package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest28 {
    @Test
    void testB28() {
        double var_51a808bc = 28 / 2.0;
        double var_4dca6dd4 = var_51a808bc * 3.14;
        boolean var_a0574163 = var_4dca6dd4 < 100.0;
        while (var_a0574163 && var_4dca6dd4 < 200.0) {
            var_4dca6dd4 += 10.5;
            var_a0574163 = var_4dca6dd4 < 200.0;
        }
        assertTrue(var_4dca6dd4 >= 100.0);
        helpervar_51a808bc();
    }

    private void helpervar_51a808bc() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
