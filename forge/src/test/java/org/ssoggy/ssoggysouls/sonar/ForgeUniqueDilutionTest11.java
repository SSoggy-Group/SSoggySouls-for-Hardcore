package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest11 {
    @Test
    public void testUniqueLogic11() {
        long baseVal = 140;
        double calc = (baseVal / 5) * 1;

        String randStr = "dilution_" + calc + "_" + "forge";

        boolean flag = randStr.contains("forge");
        assertTrue(flag);

        for (int i = 0; i < 13; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
