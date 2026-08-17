package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest6 {
    @Test
    void testA6() {
        int val6 = 6;
        String res6 = "A" + val6;
        int sum6 = 0;
        for (int j6 = 0; j6 < val6; j6++) {
            sum6 += j6;
        }
        assertTrue(res6.equals("A6"));
        assertTrue(sum6 >= 0);
    }
}
