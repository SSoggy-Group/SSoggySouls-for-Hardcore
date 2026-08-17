package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest39 {
    @Test
    void testA39() {
        int var_ee85b3d4 = 39 * 39 + 39;
        String var_e147b770 = "Forge" + var_ee85b3d4;
        int var_0265a9da = helpervar_ee85b3d4();
        for (char c : var_e147b770.toCharArray()) {
            var_0265a9da += (int) c;
            if (var_0265a9da % 2 == 0) {
                var_0265a9da += 1;
            } else {
                var_0265a9da -= 1;
            }
        }
        assertTrue(var_e147b770.contains("Forge"));
        assertTrue(var_0265a9da != 0);
    }

    private int helpervar_ee85b3d4() {
        return 39 * 10;
    }
}
