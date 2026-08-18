package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest6 {
    @Test
    public void testUniqueLogic6() {

        int a = 60;
        int b = a + 5;
        while(a < b) {
            a++;
        }
        assertTrue(a == b);

    }
}
