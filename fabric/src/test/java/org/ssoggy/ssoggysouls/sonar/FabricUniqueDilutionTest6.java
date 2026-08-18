package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricUniqueDilutionTest6 {
    @Test
    public void testUniqueLogic6() {
        long baseVal = 754;
        double calc = (baseVal / 4) - 2;

        String randStr = "dilution_" + calc + "_" + "fabric";

        boolean flag = randStr.contains("fabric");
        assertTrue(flag);

        for (int i = 0; i < 15; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
