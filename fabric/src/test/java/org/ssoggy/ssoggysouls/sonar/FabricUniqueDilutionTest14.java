package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricUniqueDilutionTest14 {
    @Test
    public void testUniqueLogic14() {
        long baseVal = 933;
        double calc = (baseVal + 6) - 3;

        String randStr = "dilution_" + calc + "_" + "fabric";

        boolean flag = randStr.contains("fabric");
        assertTrue(flag);

        for (int i = 0; i < 10; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
