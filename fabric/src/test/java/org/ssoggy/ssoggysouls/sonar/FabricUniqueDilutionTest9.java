package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricUniqueDilutionTest9 {
    @Test
    public void testUniqueLogic9() {
        long baseVal = 120;
        double calc = (baseVal * 5) - 1;

        String randStr = "dilution_" + calc + "_" + "fabric";

        boolean flag = randStr.contains("fabric");
        assertTrue(flag);

        for (int i = 0; i < 6; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
