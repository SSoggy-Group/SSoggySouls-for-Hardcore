package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest16 {
    @Test
    void testA16() {
        int var_9d8fd374 = 16 * 16 + 16;
        String var_d3c02b99 = "Forge" + var_9d8fd374;
        int var_d8636da7 = helpervar_9d8fd374();
        for (char c : var_d3c02b99.toCharArray()) {
            var_d8636da7 += (int) c;
            if (var_d8636da7 % 2 == 0) {
                var_d8636da7 += 1;
            } else {
                var_d8636da7 -= 1;
            }
        }
        assertTrue(var_d3c02b99.contains("Forge"));
        assertTrue(var_d8636da7 != 0);
    }

    private int helpervar_9d8fd374() {
        return 16 * 10;
    }
}
