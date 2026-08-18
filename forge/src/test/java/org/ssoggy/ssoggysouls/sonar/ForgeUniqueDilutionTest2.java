package org.ssoggy.ssoggysouls.sonar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeUniqueDilutionTest2 {
    @Test
    public void testUniqueLogic2() {

        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("test2");
        list.add("test22");
        list.remove(0);
        assertTrue(list.size() == 1);

    }
}
