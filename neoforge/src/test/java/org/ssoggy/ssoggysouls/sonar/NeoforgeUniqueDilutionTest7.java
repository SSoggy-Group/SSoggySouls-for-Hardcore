package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest7 {
    @Test
    public void testUniqueLogic7() {

        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("test7");
        list.add("test27");
        list.remove(0);
        assertTrue(list.size() == 1);

    }
}
