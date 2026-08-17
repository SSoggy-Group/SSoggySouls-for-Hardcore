package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest32 {
    @Test
    void testA32() {
        int var_8f6622eb = 32 * 32 + 32;
        String var_e12f96eb = "Forge" + var_8f6622eb;
        int var_3799bfa8 = helpervar_8f6622eb();
        for (char c : var_e12f96eb.toCharArray()) {
            var_3799bfa8 += (int) c;
            if (var_3799bfa8 % 2 == 0) {
                var_3799bfa8 += 1;
            } else {
                var_3799bfa8 -= 1;
            }
        }
        assertTrue(var_e12f96eb.contains("Forge"));
        assertTrue(var_3799bfa8 != 0);
    }

    private int helpervar_8f6622eb() {
        return 32 * 10;
    }
}
