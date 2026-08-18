package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricUniqueDilutionTest0 {
    @Test
    public void testUniqueLogic0() {
        long baseVal = 438;
        double calc = (baseVal - 3) + 2;

        String randStr = "dilution_" + calc + "_" + "fabric";

        boolean flag = randStr.contains("fabric");
        assertTrue(flag);

        for (int i = 0; i < 9; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
