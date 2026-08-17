package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest4 {
    @Test
    void testA4() {
        int var_4d79150c = 4 * 4 + 4;
        String var_87ecd032 = "Forge" + var_4d79150c;
        int var_996ada25 = helpervar_4d79150c();
        for (char c : var_87ecd032.toCharArray()) {
            var_996ada25 += (int) c;
            if (var_996ada25 % 2 == 0) {
                var_996ada25 += 1;
            } else {
                var_996ada25 -= 1;
            }
        }
        assertTrue(var_87ecd032.contains("Forge"));
        assertTrue(var_996ada25 != 0);
    }

    private int helpervar_4d79150c() {
        return 4 * 10;
    }
}
