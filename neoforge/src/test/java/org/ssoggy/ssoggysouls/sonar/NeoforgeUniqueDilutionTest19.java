package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest19 {
    @Test
    public void testUniqueLogic19() {

        boolean flag1 = 19 % 2 == 0;
        boolean flag2 = !flag1;
        assertTrue(flag1 != flag2);

    }
}
