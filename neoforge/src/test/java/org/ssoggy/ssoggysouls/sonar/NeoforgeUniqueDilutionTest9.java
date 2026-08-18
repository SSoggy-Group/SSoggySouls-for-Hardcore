package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest9 {
    @Test
    public void testUniqueLogic9() {

        boolean flag1 = 9 % 2 == 0;
        boolean flag2 = !flag1;
        assertTrue(flag1 != flag2);

    }
}
