package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest33 {
    @Test
    void testA33() {
        int var_c882adb1 = 33 * 33 + 33;
        String var_8321d650 = "Forge" + var_c882adb1;
        int var_cd0e18ae = helpervar_c882adb1();
        for (char c : var_8321d650.toCharArray()) {
            var_cd0e18ae += (int) c;
            if (var_cd0e18ae % 2 == 0) {
                var_cd0e18ae += 1;
            } else {
                var_cd0e18ae -= 1;
            }
        }
        assertTrue(var_8321d650.contains("Forge"));
        assertTrue(var_cd0e18ae != 0);
    }

    private int helpervar_c882adb1() {
        return 33 * 10;
    }
}
