package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest17 {
    @Test
    void testA17() {
        int var_ecb1b7fd = 17 * 17 + 17;
        String var_7ffa40bc = "Forge" + var_ecb1b7fd;
        int var_2df6f962 = helpervar_ecb1b7fd();
        for (char c : var_7ffa40bc.toCharArray()) {
            var_2df6f962 += (int) c;
            if (var_2df6f962 % 2 == 0) {
                var_2df6f962 += 1;
            } else {
                var_2df6f962 -= 1;
            }
        }
        assertTrue(var_7ffa40bc.contains("Forge"));
        assertTrue(var_2df6f962 != 0);
    }

    private int helpervar_ecb1b7fd() {
        return 17 * 10;
    }
}
