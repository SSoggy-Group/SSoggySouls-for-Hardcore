package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest9 {
    @Test
    void testA9() {
        int var_79ece319 = 9 * 9 + 9;
        String var_3300683a = "Forge" + var_79ece319;
        int var_5f761a19 = helpervar_79ece319();
        for (char c : var_3300683a.toCharArray()) {
            var_5f761a19 += (int) c;
            if (var_5f761a19 % 2 == 0) {
                var_5f761a19 += 1;
            } else {
                var_5f761a19 -= 1;
            }
        }
        assertTrue(var_3300683a.contains("Forge"));
        assertTrue(var_5f761a19 != 0);
    }

    private int helpervar_79ece319() {
        return 9 * 10;
    }
}
