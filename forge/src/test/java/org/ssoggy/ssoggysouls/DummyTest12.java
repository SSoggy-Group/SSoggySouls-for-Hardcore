package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest12 {
    @Test
    void testA12() {
        int var_8e6b6f5f = 12 * 12 + 12;
        String var_49ff0cee = "Forge" + var_8e6b6f5f;
        int var_52257d6b = helpervar_8e6b6f5f();
        for (char c : var_49ff0cee.toCharArray()) {
            var_52257d6b += (int) c;
            if (var_52257d6b % 2 == 0) {
                var_52257d6b += 1;
            } else {
                var_52257d6b -= 1;
            }
        }
        assertTrue(var_49ff0cee.contains("Forge"));
        assertTrue(var_52257d6b != 0);
    }

    private int helpervar_8e6b6f5f() {
        return 12 * 10;
    }
}
