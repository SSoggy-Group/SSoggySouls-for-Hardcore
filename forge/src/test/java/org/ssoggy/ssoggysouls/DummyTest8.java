package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest8 {
    @Test
    void testA8() {
        int val8 = 8;
        String res8 = "A" + val8;
        int sum8 = 0;
        for (int j8 = 0; j8 < val8; j8++) {
            sum8 += j8;
        }
        assertTrue(res8.equals("A8"));
        assertTrue(sum8 >= 0);
    }
}
