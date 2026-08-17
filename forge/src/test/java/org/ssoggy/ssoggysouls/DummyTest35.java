package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest35 {
    @Test
    void testA35() {
        int var_8fc8e1f3 = 35 * 35 + 35;
        String var_826259d9 = "Forge" + var_8fc8e1f3;
        int var_bc39fa32 = helpervar_8fc8e1f3();
        for (char c : var_826259d9.toCharArray()) {
            var_bc39fa32 += (int) c;
            if (var_bc39fa32 % 2 == 0) {
                var_bc39fa32 += 1;
            } else {
                var_bc39fa32 -= 1;
            }
        }
        assertTrue(var_826259d9.contains("Forge"));
        assertTrue(var_bc39fa32 != 0);
    }

    private int helpervar_8fc8e1f3() {
        return 35 * 10;
    }
}
