package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricUniqueDilutionTest4 {
    @Test
    public void testUniqueLogic4() {

        boolean flag1 = 4 % 2 == 0;
        boolean flag2 = !flag1;
        assertTrue(flag1 != flag2);

    }
}
