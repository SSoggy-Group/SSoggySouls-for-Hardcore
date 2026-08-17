package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest36 {
    @Test
    void testA36() {
        int var_fecbcd99 = 36 * 36 + 36;
        String var_e6bc448d = "Forge" + var_fecbcd99;
        int var_7dc3d619 = helpervar_fecbcd99();
        for (char c : var_e6bc448d.toCharArray()) {
            var_7dc3d619 += (int) c;
            if (var_7dc3d619 % 2 == 0) {
                var_7dc3d619 += 1;
            } else {
                var_7dc3d619 -= 1;
            }
        }
        assertTrue(var_e6bc448d.contains("Forge"));
        assertTrue(var_7dc3d619 != 0);
    }

    private int helpervar_fecbcd99() {
        return 36 * 10;
    }
}
