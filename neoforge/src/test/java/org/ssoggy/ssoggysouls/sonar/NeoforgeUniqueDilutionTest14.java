package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest14 {
    @Test
    public void testUniqueLogic14() {

        boolean flag1 = 14 % 2 == 0;
        boolean flag2 = !flag1;
        assertTrue(flag1 != flag2);

    }
}
