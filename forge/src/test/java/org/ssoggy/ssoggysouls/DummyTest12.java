package org.ssoggy.ssoggysouls;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class DummyTest12 {
    @Test
    void testA12() {
        int val12 = 12;
        String res12 = "A" + val12;
        int sum12 = 0;
        for (int j12 = 0; j12 < val12; j12++) {
            sum12 += j12;
        }
        assertTrue(res12.equals("A12"));
        assertTrue(sum12 >= 0);
    }
}
