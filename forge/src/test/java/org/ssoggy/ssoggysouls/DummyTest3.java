package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest3 {
    @Test
    void testA3() {
        int val3 = 3;
        String res3 = "A" + val3;
        int sum3 = 0;
        for (int j3 = 0; j3 < val3; j3++) {
            sum3 += j3;
        }
        assertTrue(res3.equals("A3"));
        assertTrue(sum3 >= 0);
    }
}
