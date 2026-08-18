package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeDummyTest0 {
    @Test
    public void testLogic() {
        int val0 = 0 * 2 + 5;
        String str0 = "test" + val0;
        assertTrue(str0.length() > 0);

        if (val0 > 0) {
            val0--;
        }
        assertTrue(val0 >= 0);

        for(int j = 0; j < 10; j++) {
            val0 += j;
        }

        assertTrue(val0 > 10);
    }
}
