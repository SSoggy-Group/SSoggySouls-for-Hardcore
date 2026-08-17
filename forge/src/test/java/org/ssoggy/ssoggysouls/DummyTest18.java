package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest18 {
    @Test
    void testA18() {
        int var_dad1ecf1 = 18 * 18 + 18;
        String var_81f40fe5 = "Forge" + var_dad1ecf1;
        int var_c8cb803d = helpervar_dad1ecf1();
        for (char c : var_81f40fe5.toCharArray()) {
            var_c8cb803d += (int) c;
            if (var_c8cb803d % 2 == 0) {
                var_c8cb803d += 1;
            } else {
                var_c8cb803d -= 1;
            }
        }
        assertTrue(var_81f40fe5.contains("Forge"));
        assertTrue(var_c8cb803d != 0);
    }

    private int helpervar_dad1ecf1() {
        return 18 * 10;
    }
}
