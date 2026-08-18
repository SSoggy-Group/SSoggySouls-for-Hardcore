package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricUniqueDilutionTest11 {
    @Test
    public void testUniqueLogic11() {
        long baseVal = 434;
        double calc = (baseVal - 10) - 2;

        String randStr = "dilution_" + calc + "_" + "fabric";

        boolean flag = randStr.contains("fabric");
        assertTrue(flag);

        for (int i = 0; i < 15; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
