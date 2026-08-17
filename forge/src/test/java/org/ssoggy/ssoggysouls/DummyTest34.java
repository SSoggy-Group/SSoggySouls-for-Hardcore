package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest34 {
    @Test
    void testA34() {
        int var_0b0942d3 = 34 * 34 + 34;
        String var_abf1ccbe = "Forge" + var_0b0942d3;
        int var_bbd46302 = helpervar_0b0942d3();
        for (char c : var_abf1ccbe.toCharArray()) {
            var_bbd46302 += (int) c;
            if (var_bbd46302 % 2 == 0) {
                var_bbd46302 += 1;
            } else {
                var_bbd46302 -= 1;
            }
        }
        assertTrue(var_abf1ccbe.contains("Forge"));
        assertTrue(var_bbd46302 != 0);
    }

    private int helpervar_0b0942d3() {
        return 34 * 10;
    }
}
