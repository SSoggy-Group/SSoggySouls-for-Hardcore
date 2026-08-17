package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest9 {
    @Test
    void testA9() {
        int val9 = 9;
        String res9 = "A" + val9;
        int sum9 = 0;
        for (int j9 = 0; j9 < val9; j9++) {
            sum9 += j9;
        }
        assertTrue(res9.equals("A9"));
        assertTrue(sum9 >= 0);
    }
}
