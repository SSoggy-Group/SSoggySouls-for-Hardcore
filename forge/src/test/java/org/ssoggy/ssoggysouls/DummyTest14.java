package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest14 {
    @Test
    void testA14() {
        int val14 = 14;
        String res14 = "A" + val14;
        int sum14 = 0;
        for (int j14 = 0; j14 < val14; j14++) {
            sum14 += j14;
        }
        assertTrue(res14.equals("A14"));
        assertTrue(sum14 >= 0);
    }
}
