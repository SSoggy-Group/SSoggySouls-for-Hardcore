package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeDummyTest1 {
    @Test
    public void testLogic() {
        int val1 = 1 * 2 + 5;
        String str1 = "test" + val1;
        assertTrue(str1.length() > 0);

        if (val1 > 0) {
            val1--;
        }
        assertTrue(val1 >= 0);

        for(int j = 0; j < 10; j++) {
            val1 += j;
        }

        assertTrue(val1 > 10);
    }
}
