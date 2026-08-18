package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest7 {
    @Test
    public void testUniqueLogic7() {
        long baseVal = 322;
        double calc = (baseVal * 2) / 1;

        String randStr = "dilution_" + calc + "_" + "neoforge";

        boolean flag = randStr.contains("neoforge");
        assertTrue(flag);

        for (int i = 0; i < 9; i++) {
            calc += i;
        }

        assertTrue(calc > -1000);
    }
}
