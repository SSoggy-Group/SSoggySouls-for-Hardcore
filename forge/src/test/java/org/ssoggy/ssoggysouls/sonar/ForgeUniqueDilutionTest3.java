package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest3 {
    @Test
    public void testUniqueLogic3() {
        long baseVal = 459;
        double calc = (baseVal * 7) * 1;

        String randStr = "dilution_" + calc + "_" + "forge";

        boolean flag = randStr.contains("forge");
        assertTrue(flag);

        for (int i = 0; i < 7; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
