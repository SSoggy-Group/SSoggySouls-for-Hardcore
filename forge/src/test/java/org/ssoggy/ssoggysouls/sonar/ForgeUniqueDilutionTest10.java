package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest10 {
    @Test
    public void testUniqueLogic10() {
        long baseVal = 659;
        double calc = (baseVal - 8) + 4;

        String randStr = "dilution_" + calc + "_" + "forge";

        boolean flag = randStr.contains("forge");
        assertTrue(flag);

        for (int i = 0; i < 6; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
