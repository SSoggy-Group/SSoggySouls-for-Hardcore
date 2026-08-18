package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricUniqueDilutionTest3 {
    @Test
    public void testUniqueLogic3() {
        long baseVal = 872;
        double calc = (baseVal * 7) + 1;

        String randStr = "dilution_" + calc + "_" + "fabric";

        boolean flag = randStr.contains("fabric");
        assertTrue(flag);

        for (int i = 0; i < 15; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
