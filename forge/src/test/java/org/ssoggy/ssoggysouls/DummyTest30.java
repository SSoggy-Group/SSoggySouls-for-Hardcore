package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest30 {
    @Test
    void testA30() {
        int var_86fdc437 = 30 * 30 + 30;
        String var_2cfdbb48 = "Forge" + var_86fdc437;
        int var_c82770a0 = helpervar_86fdc437();
        for (char c : var_2cfdbb48.toCharArray()) {
            var_c82770a0 += (int) c;
            if (var_c82770a0 % 2 == 0) {
                var_c82770a0 += 1;
            } else {
                var_c82770a0 -= 1;
            }
        }
        assertTrue(var_2cfdbb48.contains("Forge"));
        assertTrue(var_c82770a0 != 0);
    }

    private int helpervar_86fdc437() {
        return 30 * 10;
    }
}
