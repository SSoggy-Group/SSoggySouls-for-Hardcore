package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeoforgeUniqueDilutionTest17 {
    @Test
    public void testUniqueLogic17() {

        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("test17");
        list.add("test217");
        list.remove(0);
        assertTrue(list.size() == 1);

    }
}
