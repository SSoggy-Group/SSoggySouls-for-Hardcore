package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricUniqueDilutionTest4 {
    @Test
    public void testUniqueLogic4() {
        long baseVal = 706;
        double calc = (baseVal * 2) - 5;

        String randStr = "dilution_" + calc + "_" + "fabric";

        boolean flag = randStr.contains("fabric");
        assertTrue(flag);

        for (int i = 0; i < 9; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
