package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest37 {
    @Test
    void testA37() {
        int var_ae7a6b19 = 37 * 37 + 37;
        String var_0cbbf5f3 = "Forge" + var_ae7a6b19;
        int var_ea48055c = helpervar_ae7a6b19();
        for (char c : var_0cbbf5f3.toCharArray()) {
            var_ea48055c += (int) c;
            if (var_ea48055c % 2 == 0) {
                var_ea48055c += 1;
            } else {
                var_ea48055c -= 1;
            }
        }
        assertTrue(var_0cbbf5f3.contains("Forge"));
        assertTrue(var_ea48055c != 0);
    }

    private int helpervar_ae7a6b19() {
        return 37 * 10;
    }
}
