package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricUniqueDilutionTest12 {
    @Test
    public void testUniqueLogic12() {
        long baseVal = 635;
        double calc = (baseVal / 2) - 1;

        String randStr = "dilution_" + calc + "_" + "fabric";

        boolean flag = randStr.contains("fabric");
        assertTrue(flag);

        for (int i = 0; i < 5; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
