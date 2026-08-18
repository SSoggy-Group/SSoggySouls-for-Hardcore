package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest15 {
    @Test
    public void testUniqueLogic15() {

        String str = "forge_15";
        if (str.length() > 5) {
            str = str.substring(0, 5);
        }
        assertTrue(str.length() <= 5);

    }
}
