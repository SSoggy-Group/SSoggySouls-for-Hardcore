package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest29 {
    @Test
    void testA29() {
        int var_34de2d8d = 29 * 29 + 29;
        String var_89b8bf54 = "Forge" + var_34de2d8d;
        int var_a1f8e8cf = helpervar_34de2d8d();
        for (char c : var_89b8bf54.toCharArray()) {
            var_a1f8e8cf += (int) c;
            if (var_a1f8e8cf % 2 == 0) {
                var_a1f8e8cf += 1;
            } else {
                var_a1f8e8cf -= 1;
            }
        }
        assertTrue(var_89b8bf54.contains("Forge"));
        assertTrue(var_a1f8e8cf != 0);
    }

    private int helpervar_34de2d8d() {
        return 29 * 10;
    }
}
