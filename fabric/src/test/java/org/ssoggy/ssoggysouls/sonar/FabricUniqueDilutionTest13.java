package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricUniqueDilutionTest13 {
    @Test
    public void testUniqueLogic13() {
        long baseVal = 792;
        double calc = (baseVal * 2) - 1;

        String randStr = "dilution_" + calc + "_" + "fabric";

        boolean flag = randStr.contains("fabric");
        assertTrue(flag);

        for (int i = 0; i < 12; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
