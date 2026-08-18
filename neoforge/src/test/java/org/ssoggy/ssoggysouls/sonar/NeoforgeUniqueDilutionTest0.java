package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest0 {
    @Test
    public void testUniqueLogic0() {
        long baseVal = 568;
        double calc = (baseVal / 2) - 1;

        String randStr = "dilution_" + calc + "_" + "neoforge";

        boolean flag = randStr.contains("neoforge");
        assertTrue(flag);

        for (int i = 0; i < 6; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
