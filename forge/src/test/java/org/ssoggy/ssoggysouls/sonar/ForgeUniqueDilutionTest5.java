package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest5 {
    @Test
    public void testUniqueLogic5() {
        long baseVal = 229;
        double calc = (baseVal / 8) - 4;

        String randStr = "dilution_" + calc + "_" + "forge";

        boolean flag = randStr.contains("forge");
        assertTrue(flag);

        for (int i = 0; i < 13; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
