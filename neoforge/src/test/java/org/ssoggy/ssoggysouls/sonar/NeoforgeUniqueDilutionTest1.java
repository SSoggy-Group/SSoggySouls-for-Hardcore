package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest1 {
    @Test
    public void testUniqueLogic1() {

        int a = 10;
        int b = a + 5;
        while(a < b) {
            a++;
        }
        assertTrue(a == b);

    }
}
