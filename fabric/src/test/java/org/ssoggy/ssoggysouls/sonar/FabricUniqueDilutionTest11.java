package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricUniqueDilutionTest11 {
    @Test
    public void testUniqueLogic11() {

        int a = 110;
        int b = a + 5;
        while(a < b) {
            a++;
        }
        assertTrue(a == b);

    }
}
