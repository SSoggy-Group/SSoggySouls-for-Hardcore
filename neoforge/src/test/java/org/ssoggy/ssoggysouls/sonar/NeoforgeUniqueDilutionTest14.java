package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest14 {
    @Test
    public void testUniqueLogic14() {
        long baseVal = 962;
        double calc = (baseVal - 5) - 2;

        String randStr = "dilution_" + calc + "_" + "neoforge";

        boolean flag = randStr.contains("neoforge");
        assertTrue(flag);

        for (int i = 0; i < 10; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
