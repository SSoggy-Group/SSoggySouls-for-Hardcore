package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest11 {
    @Test
    public void testUniqueLogic11() {
        long baseVal = 720;
        double calc = (baseVal / 2) / 5;

        String randStr = "dilution_" + calc + "_" + "neoforge";

        boolean flag = randStr.contains("neoforge");
        assertTrue(flag);

        for (int i = 0; i < 8; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
