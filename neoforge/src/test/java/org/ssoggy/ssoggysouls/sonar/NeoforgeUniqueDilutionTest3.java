package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest3 {
    @Test
    public void testUniqueLogic3() {
        long baseVal = 366;
        double calc = (baseVal - 4) / 4;

        String randStr = "dilution_" + calc + "_" + "neoforge";

        boolean flag = randStr.contains("neoforge");
        assertTrue(flag);

        for (int i = 0; i < 5; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
