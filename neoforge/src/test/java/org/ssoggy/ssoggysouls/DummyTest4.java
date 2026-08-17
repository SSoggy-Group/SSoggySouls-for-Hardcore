package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest4 {
    @Test
    void testB4() {
        double var_49826787 = 4 / 2.0;
        double var_3d63366d = var_49826787 * 3.14;
        boolean var_788a8fd3 = var_3d63366d < 100.0;
        while (var_788a8fd3 && var_3d63366d < 200.0) {
            var_3d63366d += 10.5;
            var_788a8fd3 = var_3d63366d < 200.0;
        }
        assertTrue(var_3d63366d >= 100.0);
        helpervar_49826787();
    }

    private void helpervar_49826787() {
        long x = System.currentTimeMillis();
        assertTrue(x > 0);
    }
}
