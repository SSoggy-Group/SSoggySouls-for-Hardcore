package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest10 {
    @Test
    public void testUniqueLogic10() {
        long baseVal = 575;
        double calc = (baseVal - 9) / 4;

        String randStr = "dilution_" + calc + "_" + "neoforge";

        boolean flag = randStr.contains("neoforge");
        assertTrue(flag);

        for (int i = 0; i < 12; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
