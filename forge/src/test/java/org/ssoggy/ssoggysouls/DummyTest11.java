package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest11 {
    @Test
    void testA11() {
        int var_1278e6a1 = 11 * 11 + 11;
        String var_eb20ea82 = "Forge" + var_1278e6a1;
        int var_2c42a419 = helpervar_1278e6a1();
        for (char c : var_eb20ea82.toCharArray()) {
            var_2c42a419 += (int) c;
            if (var_2c42a419 % 2 == 0) {
                var_2c42a419 += 1;
            } else {
                var_2c42a419 -= 1;
            }
        }
        assertTrue(var_eb20ea82.contains("Forge"));
        assertTrue(var_2c42a419 != 0);
    }

    private int helpervar_1278e6a1() {
        return 11 * 10;
    }
}
