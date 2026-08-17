package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest24 {
    @Test
    void testA24() {
        int var_fabaa08a = 24 * 24 + 24;
        String var_d12d688a = "Forge" + var_fabaa08a;
        int var_ee8df9d9 = helpervar_fabaa08a();
        for (char c : var_d12d688a.toCharArray()) {
            var_ee8df9d9 += (int) c;
            if (var_ee8df9d9 % 2 == 0) {
                var_ee8df9d9 += 1;
            } else {
                var_ee8df9d9 -= 1;
            }
        }
        assertTrue(var_d12d688a.contains("Forge"));
        assertTrue(var_ee8df9d9 != 0);
    }

    private int helpervar_fabaa08a() {
        return 24 * 10;
    }
}
