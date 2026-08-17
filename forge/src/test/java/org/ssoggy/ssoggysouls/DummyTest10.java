package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest10 {
    @Test
    void testA10() {
        int var_722468e4 = 10 * 10 + 10;
        String var_13e8a55c = "Forge" + var_722468e4;
        int var_64d34d5f = helpervar_722468e4();
        for (char c : var_13e8a55c.toCharArray()) {
            var_64d34d5f += (int) c;
            if (var_64d34d5f % 2 == 0) {
                var_64d34d5f += 1;
            } else {
                var_64d34d5f -= 1;
            }
        }
        assertTrue(var_13e8a55c.contains("Forge"));
        assertTrue(var_64d34d5f != 0);
    }

    private int helpervar_722468e4() {
        return 10 * 10;
    }
}
