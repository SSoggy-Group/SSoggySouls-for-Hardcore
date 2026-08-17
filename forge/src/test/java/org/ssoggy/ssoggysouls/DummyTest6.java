package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest6 {
    @Test
    void testA6() {
        int var_76ed13d0 = 6 * 6 + 6;
        String var_4ca3e44f = "Forge" + var_76ed13d0;
        int var_1208b75a = helpervar_76ed13d0();
        for (char c : var_4ca3e44f.toCharArray()) {
            var_1208b75a += (int) c;
            if (var_1208b75a % 2 == 0) {
                var_1208b75a += 1;
            } else {
                var_1208b75a -= 1;
            }
        }
        assertTrue(var_4ca3e44f.contains("Forge"));
        assertTrue(var_1208b75a != 0);
    }

    private int helpervar_76ed13d0() {
        return 6 * 10;
    }
}
