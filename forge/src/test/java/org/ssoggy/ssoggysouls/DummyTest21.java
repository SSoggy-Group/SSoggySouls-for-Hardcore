package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest21 {
    @Test
    void testA21() {
        int var_4e152b05 = 21 * 21 + 21;
        String var_302e279f = "Forge" + var_4e152b05;
        int var_74a0ea8d = helpervar_4e152b05();
        for (char c : var_302e279f.toCharArray()) {
            var_74a0ea8d += (int) c;
            if (var_74a0ea8d % 2 == 0) {
                var_74a0ea8d += 1;
            } else {
                var_74a0ea8d -= 1;
            }
        }
        assertTrue(var_302e279f.contains("Forge"));
        assertTrue(var_74a0ea8d != 0);
    }

    private int helpervar_4e152b05() {
        return 21 * 10;
    }
}
