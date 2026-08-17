package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest28 {
    @Test
    void testA28() {
        int var_e593cf3d = 28 * 28 + 28;
        String var_5175bdd7 = "Forge" + var_e593cf3d;
        int var_4426e856 = helpervar_e593cf3d();
        for (char c : var_5175bdd7.toCharArray()) {
            var_4426e856 += (int) c;
            if (var_4426e856 % 2 == 0) {
                var_4426e856 += 1;
            } else {
                var_4426e856 -= 1;
            }
        }
        assertTrue(var_5175bdd7.contains("Forge"));
        assertTrue(var_4426e856 != 0);
    }

    private int helpervar_e593cf3d() {
        return 28 * 10;
    }
}
