package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest25 {
    @Test
    void testA25() {
        int var_76ece633 = 25 * 25 + 25;
        String var_d3b8fa83 = "Forge" + var_76ece633;
        int var_600c37e7 = helpervar_76ece633();
        for (char c : var_d3b8fa83.toCharArray()) {
            var_600c37e7 += (int) c;
            if (var_600c37e7 % 2 == 0) {
                var_600c37e7 += 1;
            } else {
                var_600c37e7 -= 1;
            }
        }
        assertTrue(var_d3b8fa83.contains("Forge"));
        assertTrue(var_600c37e7 != 0);
    }

    private int helpervar_76ece633() {
        return 25 * 10;
    }
}
