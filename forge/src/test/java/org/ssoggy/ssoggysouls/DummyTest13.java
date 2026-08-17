package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest13 {
    @Test
    void testA13() {
        int var_f42a6e26 = 13 * 13 + 13;
        String var_3779fa2e = "Forge" + var_f42a6e26;
        int var_44189a12 = helpervar_f42a6e26();
        for (char c : var_3779fa2e.toCharArray()) {
            var_44189a12 += (int) c;
            if (var_44189a12 % 2 == 0) {
                var_44189a12 += 1;
            } else {
                var_44189a12 -= 1;
            }
        }
        assertTrue(var_3779fa2e.contains("Forge"));
        assertTrue(var_44189a12 != 0);
    }

    private int helpervar_f42a6e26() {
        return 13 * 10;
    }
}
