package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricDummyTest2 {
    @Test
    public void testFabricMath2() {
        int a = 2;
        int b = 3;
        int c = a + b;
        assertTrue(c == 5, "Fabric basic math failed");
    }
}
