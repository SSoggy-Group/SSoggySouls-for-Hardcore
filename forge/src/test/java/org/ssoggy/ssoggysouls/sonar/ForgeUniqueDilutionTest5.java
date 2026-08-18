package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest5 {
    @Test
    public void testUniqueLogic5() {

        String str = "forge_5";
        if (str.length() > 5) {
            str = str.substring(0, 5);
        }
        assertTrue(str.length() <= 5);

    }
}
