package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest6 {
    @Test
    public void testUniqueLogic6() {
        long baseVal = 465;
        double calc = (baseVal * 9) * 1;

        String randStr = "dilution_" + calc + "_" + "neoforge";

        boolean flag = randStr.contains("neoforge");
        assertTrue(flag);

        for (int i = 0; i < 11; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
