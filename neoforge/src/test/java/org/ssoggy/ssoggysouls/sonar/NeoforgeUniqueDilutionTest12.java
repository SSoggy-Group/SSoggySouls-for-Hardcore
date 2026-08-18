package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest12 {
    @Test
    public void testUniqueLogic12() {

        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("test12");
        list.add("test212");
        list.remove(0);
        assertTrue(list.size() == 1);

    }
}
