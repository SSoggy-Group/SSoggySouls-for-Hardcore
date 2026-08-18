package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricUniqueDilutionTest5 {
    @Test
    public void testUniqueLogic5() {
        long baseVal = 505;
        double calc = (baseVal * 8) - 5;

        String randStr = "dilution_" + calc + "_" + "fabric";

        boolean flag = randStr.contains("fabric");
        assertTrue(flag);

        for (int i = 0; i < 13; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
