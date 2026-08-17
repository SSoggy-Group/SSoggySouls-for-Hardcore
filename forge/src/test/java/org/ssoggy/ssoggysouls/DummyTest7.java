package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest7 {
    @Test
    void testA7() {
        int var_94616db0 = 7 * 7 + 7;
        String var_f4c24939 = "Forge" + var_94616db0;
        int var_0e0da887 = helpervar_94616db0();
        for (char c : var_f4c24939.toCharArray()) {
            var_0e0da887 += (int) c;
            if (var_0e0da887 % 2 == 0) {
                var_0e0da887 += 1;
            } else {
                var_0e0da887 -= 1;
            }
        }
        assertTrue(var_f4c24939.contains("Forge"));
        assertTrue(var_0e0da887 != 0);
    }

    private int helpervar_94616db0() {
        return 7 * 10;
    }
}
