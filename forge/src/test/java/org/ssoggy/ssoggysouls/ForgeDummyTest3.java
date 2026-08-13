package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ForgeDummyTest3 {
    @Test
    public void testForgeString3() {
        String test = "ForgeTestXYZ";
        assertEquals("ForgeTest", test.substring(0, 9), "Forge string manipulation failed");
    }
}
