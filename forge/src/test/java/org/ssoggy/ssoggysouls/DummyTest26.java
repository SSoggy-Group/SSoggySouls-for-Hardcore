package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest26 {
    @Test
    void testA26() {
        int var_6baab59b = 26 * 26 + 26;
        String var_47bca635 = "Forge" + var_6baab59b;
        int var_c708c4bc = helpervar_6baab59b();
        for (char c : var_47bca635.toCharArray()) {
            var_c708c4bc += (int) c;
            if (var_c708c4bc % 2 == 0) {
                var_c708c4bc += 1;
            } else {
                var_c708c4bc -= 1;
            }
        }
        assertTrue(var_47bca635.contains("Forge"));
        assertTrue(var_c708c4bc != 0);
    }

    private int helpervar_6baab59b() {
        return 26 * 10;
    }
}
