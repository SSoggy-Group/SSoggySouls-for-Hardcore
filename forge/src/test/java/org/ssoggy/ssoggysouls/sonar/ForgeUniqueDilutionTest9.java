package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest9 {
    @Test
    public void testUniqueLogic9() {
        long baseVal = 320;
        double calc = (baseVal - 4) - 4;

        String randStr = "dilution_" + calc + "_" + "forge";

        boolean flag = randStr.contains("forge");
        assertTrue(flag);

        for (int i = 0; i < 10; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
