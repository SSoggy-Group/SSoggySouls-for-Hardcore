package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest10 {
    @Test
    void testA10() {
        int val10 = 10;
        String res10 = "A" + val10;
        int sum10 = 0;
        for (int j10 = 0; j10 < val10; j10++) {
            sum10 += j10;
        }
        assertTrue(res10.equals("A10"));
        assertTrue(sum10 >= 0);
    }
}
