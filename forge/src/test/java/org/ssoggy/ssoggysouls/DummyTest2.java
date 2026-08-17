package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest2 {
    @Test
    void testA2() {
        int var_780baed4 = 2 * 2 + 2;
        String var_45de3579 = "Forge" + var_780baed4;
        int var_f7eef320 = helpervar_780baed4();
        for (char c : var_45de3579.toCharArray()) {
            var_f7eef320 += (int) c;
            if (var_f7eef320 % 2 == 0) {
                var_f7eef320 += 1;
            } else {
                var_f7eef320 -= 1;
            }
        }
        assertTrue(var_45de3579.contains("Forge"));
        assertTrue(var_f7eef320 != 0);
    }

    private int helpervar_780baed4() {
        return 2 * 10;
    }
}
