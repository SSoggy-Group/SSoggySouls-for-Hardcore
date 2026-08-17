package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest31 {
    @Test
    void testA31() {
        int var_02a6bc24 = 31 * 31 + 31;
        String var_33d0e207 = "Forge" + var_02a6bc24;
        int var_ac98ed9c = helpervar_02a6bc24();
        for (char c : var_33d0e207.toCharArray()) {
            var_ac98ed9c += (int) c;
            if (var_ac98ed9c % 2 == 0) {
                var_ac98ed9c += 1;
            } else {
                var_ac98ed9c -= 1;
            }
        }
        assertTrue(var_33d0e207.contains("Forge"));
        assertTrue(var_ac98ed9c != 0);
    }

    private int helpervar_02a6bc24() {
        return 31 * 10;
    }
}
