package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest20 {
    @Test
    void testA20() {
        int var_12fe47b2 = 20 * 20 + 20;
        String var_bab46d5a = "Forge" + var_12fe47b2;
        int var_10e980aa = helpervar_12fe47b2();
        for (char c : var_bab46d5a.toCharArray()) {
            var_10e980aa += (int) c;
            if (var_10e980aa % 2 == 0) {
                var_10e980aa += 1;
            } else {
                var_10e980aa -= 1;
            }
        }
        assertTrue(var_bab46d5a.contains("Forge"));
        assertTrue(var_10e980aa != 0);
    }

    private int helpervar_12fe47b2() {
        return 20 * 10;
    }
}
