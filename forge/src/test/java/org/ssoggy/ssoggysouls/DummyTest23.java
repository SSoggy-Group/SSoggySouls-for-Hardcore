package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest23 {
    @Test
    void testA23() {
        int var_3c3d2bbd = 23 * 23 + 23;
        String var_b825b940 = "Forge" + var_3c3d2bbd;
        int var_83c33bfc = helpervar_3c3d2bbd();
        for (char c : var_b825b940.toCharArray()) {
            var_83c33bfc += (int) c;
            if (var_83c33bfc % 2 == 0) {
                var_83c33bfc += 1;
            } else {
                var_83c33bfc -= 1;
            }
        }
        assertTrue(var_b825b940.contains("Forge"));
        assertTrue(var_83c33bfc != 0);
    }

    private int helpervar_3c3d2bbd() {
        return 23 * 10;
    }
}
