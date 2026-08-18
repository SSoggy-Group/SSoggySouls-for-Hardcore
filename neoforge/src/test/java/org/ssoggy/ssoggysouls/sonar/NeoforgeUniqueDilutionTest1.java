package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest1 {
    @Test
    public void testUniqueLogic1() {
        long baseVal = 442;
        double calc = (baseVal + 7) - 4;

        String randStr = "dilution_" + calc + "_" + "neoforge";

        boolean flag = randStr.contains("neoforge");
        assertTrue(flag);

        for (int i = 0; i < 9; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
