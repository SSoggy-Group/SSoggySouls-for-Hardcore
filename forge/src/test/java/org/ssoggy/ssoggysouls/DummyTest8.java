package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest8 {
    @Test
    void testA8() {
        int var_79034dee = 8 * 8 + 8;
        String var_a55a0187 = "Forge" + var_79034dee;
        int var_4b3b68b5 = helpervar_79034dee();
        for (char c : var_a55a0187.toCharArray()) {
            var_4b3b68b5 += (int) c;
            if (var_4b3b68b5 % 2 == 0) {
                var_4b3b68b5 += 1;
            } else {
                var_4b3b68b5 -= 1;
            }
        }
        assertTrue(var_a55a0187.contains("Forge"));
        assertTrue(var_4b3b68b5 != 0);
    }

    private int helpervar_79034dee() {
        return 8 * 10;
    }
}
