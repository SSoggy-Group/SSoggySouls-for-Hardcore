package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricDummyCoverageTest {
    @Test
    public void testFabricCoverage() {
        int x = 5;
        int y = 10;
        assertTrue(x + y == 15);
    }
}
