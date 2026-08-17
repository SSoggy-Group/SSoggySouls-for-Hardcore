package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest1 {
    @Test
    void testA1() {
        int var_4ac861f0 = 1 * 1 + 1;
        String var_0ce8e7b6 = "Forge" + var_4ac861f0;
        int var_4703bf53 = helpervar_4ac861f0();
        for (char c : var_0ce8e7b6.toCharArray()) {
            var_4703bf53 += (int) c;
            if (var_4703bf53 % 2 == 0) {
                var_4703bf53 += 1;
            } else {
                var_4703bf53 -= 1;
            }
        }
        assertTrue(var_0ce8e7b6.contains("Forge"));
        assertTrue(var_4703bf53 != 0);
    }

    private int helpervar_4ac861f0() {
        return 1 * 10;
    }
}
