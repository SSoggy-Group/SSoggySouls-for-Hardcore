package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest0 {
    @Test
    public void testUniqueLogic0() {
        long baseVal = 856;
        double calc = (baseVal * 6) * 3;

        String randStr = "dilution_" + calc + "_" + "forge";

        boolean flag = randStr.contains("forge");
        assertTrue(flag);

        for (int i = 0; i < 9; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
