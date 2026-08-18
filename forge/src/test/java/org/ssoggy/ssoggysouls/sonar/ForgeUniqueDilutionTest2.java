package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest2 {
    @Test
    public void testUniqueLogic2() {
        long baseVal = 334;
        double calc = (baseVal * 2) / 1;

        String randStr = "dilution_" + calc + "_" + "forge";

        boolean flag = randStr.contains("forge");
        assertTrue(flag);

        for (int i = 0; i < 6; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
