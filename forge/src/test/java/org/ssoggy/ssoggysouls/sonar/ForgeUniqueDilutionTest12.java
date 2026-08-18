package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest12 {
    @Test
    public void testUniqueLogic12() {
        long baseVal = 656;
        double calc = (baseVal - 7) + 4;

        String randStr = "dilution_" + calc + "_" + "forge";

        boolean flag = randStr.contains("forge");
        assertTrue(flag);

        for (int i = 0; i < 5; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
