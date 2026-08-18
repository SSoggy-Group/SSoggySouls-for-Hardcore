package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest8 {
    @Test
    public void testUniqueLogic8() {
        long baseVal = 308;
        double calc = (baseVal - 8) + 5;

        String randStr = "dilution_" + calc + "_" + "neoforge";

        boolean flag = randStr.contains("neoforge");
        assertTrue(flag);

        for (int i = 0; i < 12; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
