package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest14 {
    @Test
    void testA14() {
        int var_1b9625d1 = 14 * 14 + 14;
        String var_44b6cd5c = "Forge" + var_1b9625d1;
        int var_bac1ee24 = helpervar_1b9625d1();
        for (char c : var_44b6cd5c.toCharArray()) {
            var_bac1ee24 += (int) c;
            if (var_bac1ee24 % 2 == 0) {
                var_bac1ee24 += 1;
            } else {
                var_bac1ee24 -= 1;
            }
        }
        assertTrue(var_44b6cd5c.contains("Forge"));
        assertTrue(var_bac1ee24 != 0);
    }

    private int helpervar_1b9625d1() {
        return 14 * 10;
    }
}
