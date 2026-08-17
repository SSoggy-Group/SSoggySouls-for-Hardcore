package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest4 {
    @Test
    void testA4() {
        int val4 = 4;
        String res4 = "A" + val4;
        int sum4 = 0;
        for (int j4 = 0; j4 < val4; j4++) {
            sum4 += j4;
        }
        assertTrue(res4.equals("A4"));
        assertTrue(sum4 >= 0);
    }
}
