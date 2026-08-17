package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest7 {
    @Test
    void testA7() {
        int val7 = 7;
        String res7 = "A" + val7;
        int sum7 = 0;
        for (int j7 = 0; j7 < val7; j7++) {
            sum7 += j7;
        }
        assertTrue(res7.equals("A7"));
        assertTrue(sum7 >= 0);
    }
}
