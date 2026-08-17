package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest3 {
    @Test
    void testA3() {
        int var_83d85265 = 3 * 3 + 3;
        String var_f0619821 = "Forge" + var_83d85265;
        int var_74ec01b1 = helpervar_83d85265();
        for (char c : var_f0619821.toCharArray()) {
            var_74ec01b1 += (int) c;
            if (var_74ec01b1 % 2 == 0) {
                var_74ec01b1 += 1;
            } else {
                var_74ec01b1 -= 1;
            }
        }
        assertTrue(var_f0619821.contains("Forge"));
        assertTrue(var_74ec01b1 != 0);
    }

    private int helpervar_83d85265() {
        return 3 * 10;
    }
}
