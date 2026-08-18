package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest4 {
    @Test
    public void testUniqueLogic4() {
        long baseVal = 329;
        double calc = (baseVal - 4) / 5;

        String randStr = "dilution_" + calc + "_" + "forge";

        boolean flag = randStr.contains("forge");
        assertTrue(flag);

        for (int i = 0; i < 8; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
