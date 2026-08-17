package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest11 {
    @Test
    void testA11() {
        int val11 = 11;
        String res11 = "A" + val11;
        int sum11 = 0;
        for (int j11 = 0; j11 < val11; j11++) {
            sum11 += j11;
        }
        assertTrue(res11.equals("A11"));
        assertTrue(sum11 >= 0);
    }
}
