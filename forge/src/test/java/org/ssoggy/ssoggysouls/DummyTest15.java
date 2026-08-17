package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest15 {
    @Test
    void testA15() {
        int var_253ce80a = 15 * 15 + 15;
        String var_0dbf66d5 = "Forge" + var_253ce80a;
        int var_0153ca25 = helpervar_253ce80a();
        for (char c : var_0dbf66d5.toCharArray()) {
            var_0153ca25 += (int) c;
            if (var_0153ca25 % 2 == 0) {
                var_0153ca25 += 1;
            } else {
                var_0153ca25 -= 1;
            }
        }
        assertTrue(var_0dbf66d5.contains("Forge"));
        assertTrue(var_0153ca25 != 0);
    }

    private int helpervar_253ce80a() {
        return 15 * 10;
    }
}
