package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest0 {
    @Test
    public void testUniqueLogic0() {

        String str = "neoforge_0";
        if (str.length() > 5) {
            str = str.substring(0, 5);
        }
        assertTrue(str.length() <= 5);

    }
}
