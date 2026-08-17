package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest38 {
    @Test
    void testA38() {
        int var_5d92b1f3 = 38 * 38 + 38;
        String var_313a824b = "Forge" + var_5d92b1f3;
        int var_7c654ddd = helpervar_5d92b1f3();
        for (char c : var_313a824b.toCharArray()) {
            var_7c654ddd += (int) c;
            if (var_7c654ddd % 2 == 0) {
                var_7c654ddd += 1;
            } else {
                var_7c654ddd -= 1;
            }
        }
        assertTrue(var_313a824b.contains("Forge"));
        assertTrue(var_7c654ddd != 0);
    }

    private int helpervar_5d92b1f3() {
        return 38 * 10;
    }
}
