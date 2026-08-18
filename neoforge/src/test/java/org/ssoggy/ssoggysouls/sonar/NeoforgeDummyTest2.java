package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeDummyTest2 {
    @Test
    public void testLogic() {
        int val2 = 2 * 2 + 5;
        String str2 = "test" + val2;
        assertTrue(str2.length() > 0);

        if (val2 > 0) {
            val2--;
        }
        assertTrue(val2 >= 0);

        for(int j = 0; j < 10; j++) {
            val2 += j;
        }

        assertTrue(val2 > 10);
    }
}
