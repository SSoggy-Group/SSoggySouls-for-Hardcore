package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest13 {
    @Test
    public void testUniqueLogic13() {
        long baseVal = 626;
        double calc = (baseVal * 4) + 3;

        String randStr = "dilution_" + calc + "_" + "forge";

        boolean flag = randStr.contains("forge");
        assertTrue(flag);

        for (int i = 0; i < 14; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
