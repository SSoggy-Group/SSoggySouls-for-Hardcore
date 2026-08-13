package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricDummyTest {
    @Test
    public void testFabricMath() {
        int a = 1;
        int b = 2;
        int c = a + b;
        assertTrue(c == 3, "Fabric basic math failed");
    }
}
