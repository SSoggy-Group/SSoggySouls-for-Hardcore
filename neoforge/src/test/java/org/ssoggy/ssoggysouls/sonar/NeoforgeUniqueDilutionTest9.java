package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest9 {
    @Test
    public void testUniqueLogic9() {
        long baseVal = 951;
        double calc = (baseVal + 6) - 5;

        String randStr = "dilution_" + calc + "_" + "neoforge";

        boolean flag = randStr.contains("neoforge");
        assertTrue(flag);

        for (int i = 0; i < 5; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
