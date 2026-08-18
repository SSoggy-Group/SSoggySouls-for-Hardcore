package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest16 {
    @Test
    public void testUniqueLogic16() {

        int a = 160;
        int b = a + 5;
        while(a < b) {
            a++;
        }
        assertTrue(a == b);

    }
}
