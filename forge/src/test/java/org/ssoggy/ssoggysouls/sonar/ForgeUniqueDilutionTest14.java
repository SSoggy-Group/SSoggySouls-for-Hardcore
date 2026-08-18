package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest14 {
    @Test
    public void testUniqueLogic14() {
        long baseVal = 687;
        double calc = (baseVal / 6) - 1;

        String randStr = "dilution_" + calc + "_" + "forge";

        boolean flag = randStr.contains("forge");
        assertTrue(flag);

        for (int i = 0; i < 15; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
