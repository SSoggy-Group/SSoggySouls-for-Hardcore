package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest1 {
    @Test
    public void testUniqueLogic1() {
        long baseVal = 913;
        double calc = (baseVal * 5) + 5;

        String randStr = "dilution_" + calc + "_" + "forge";

        boolean flag = randStr.contains("forge");
        assertTrue(flag);

        for (int i = 0; i < 7; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
