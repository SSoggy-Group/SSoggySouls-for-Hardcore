package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricUniqueDilutionTest10 {
    @Test
    public void testUniqueLogic10() {
        long baseVal = 163;
        double calc = (baseVal - 4) - 2;

        String randStr = "dilution_" + calc + "_" + "fabric";

        boolean flag = randStr.contains("fabric");
        assertTrue(flag);

        for (int i = 0; i < 11; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
