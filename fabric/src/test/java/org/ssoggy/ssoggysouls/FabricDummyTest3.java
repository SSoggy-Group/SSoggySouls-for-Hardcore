package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricDummyTest3 {
    @Test
    public void testFabricMath3() {
        int a = 20;
        int b = 30;
        int c = a + b;
        assertTrue(c == 50, "Fabric basic math failed");
    }
}
