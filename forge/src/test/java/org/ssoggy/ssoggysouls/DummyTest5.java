package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest5 {
    @Test
    void testA5() {
        int var_ba96fa94 = 5 * 5 + 5;
        String var_a8c61948 = "Forge" + var_ba96fa94;
        int var_8dbd0652 = helpervar_ba96fa94();
        for (char c : var_a8c61948.toCharArray()) {
            var_8dbd0652 += (int) c;
            if (var_8dbd0652 % 2 == 0) {
                var_8dbd0652 += 1;
            } else {
                var_8dbd0652 -= 1;
            }
        }
        assertTrue(var_a8c61948.contains("Forge"));
        assertTrue(var_8dbd0652 != 0);
    }

    private int helpervar_ba96fa94() {
        return 5 * 10;
    }
}
