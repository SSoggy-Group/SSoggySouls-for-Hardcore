package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest5 {
    @Test
    void testA5() {
        int val5 = 5;
        String res5 = "A" + val5;
        int sum5 = 0;
        for (int j5 = 0; j5 < val5; j5++) {
            sum5 += j5;
        }
        assertTrue(res5.equals("A5"));
        assertTrue(sum5 >= 0);
    }
}
