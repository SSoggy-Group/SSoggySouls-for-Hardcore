package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest22 {
    @Test
    void testA22() {
        int var_5cd357c4 = 22 * 22 + 22;
        String var_aabc8040 = "Forge" + var_5cd357c4;
        int var_3f49b7b1 = helpervar_5cd357c4();
        for (char c : var_aabc8040.toCharArray()) {
            var_3f49b7b1 += (int) c;
            if (var_3f49b7b1 % 2 == 0) {
                var_3f49b7b1 += 1;
            } else {
                var_3f49b7b1 -= 1;
            }
        }
        assertTrue(var_aabc8040.contains("Forge"));
        assertTrue(var_3f49b7b1 != 0);
    }

    private int helpervar_5cd357c4() {
        return 22 * 10;
    }
}
