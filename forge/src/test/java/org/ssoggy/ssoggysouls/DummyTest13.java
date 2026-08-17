package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest13 {
    @Test
    void testA13() {
        int val13 = 13;
        String res13 = "A" + val13;
        int sum13 = 0;
        for (int j13 = 0; j13 < val13; j13++) {
            sum13 += j13;
        }
        assertTrue(res13.equals("A13"));
        assertTrue(sum13 >= 0);
    }
}
