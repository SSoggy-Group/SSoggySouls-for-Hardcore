package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ForgeDummyTest {
    @Test
    public void testForgeString() {
        String test = "Forge";
        assertEquals("Forge", test.substring(0, 5), "Forge string manipulation failed");
    }
}
