package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest19 {
    @Test
    void testA19() {
        int var_a553844c = 19 * 19 + 19;
        String var_4d1a4e76 = "Forge" + var_a553844c;
        int var_02e8ffcd = helpervar_a553844c();
        for (char c : var_4d1a4e76.toCharArray()) {
            var_02e8ffcd += (int) c;
            if (var_02e8ffcd % 2 == 0) {
                var_02e8ffcd += 1;
            } else {
                var_02e8ffcd -= 1;
            }
        }
        assertTrue(var_4d1a4e76.contains("Forge"));
        assertTrue(var_02e8ffcd != 0);
    }

    private int helpervar_a553844c() {
        return 19 * 10;
    }
}
