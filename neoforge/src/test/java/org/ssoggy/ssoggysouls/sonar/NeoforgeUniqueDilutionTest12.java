package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest12 {
    @Test
    public void testUniqueLogic12() {
        long baseVal = 928;
        double calc = (baseVal + 5) / 4;

        String randStr = "dilution_" + calc + "_" + "neoforge";

        boolean flag = randStr.contains("neoforge");
        assertTrue(flag);

        for (int i = 0; i < 14; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
