package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest27 {
    @Test
    void testA27() {
        int var_77e71e19 = 27 * 27 + 27;
        String var_f5470cf9 = "Forge" + var_77e71e19;
        int var_c0efb51a = helpervar_77e71e19();
        for (char c : var_f5470cf9.toCharArray()) {
            var_c0efb51a += (int) c;
            if (var_c0efb51a % 2 == 0) {
                var_c0efb51a += 1;
            } else {
                var_c0efb51a -= 1;
            }
        }
        assertTrue(var_f5470cf9.contains("Forge"));
        assertTrue(var_c0efb51a != 0);
    }

    private int helpervar_77e71e19() {
        return 27 * 10;
    }
}
