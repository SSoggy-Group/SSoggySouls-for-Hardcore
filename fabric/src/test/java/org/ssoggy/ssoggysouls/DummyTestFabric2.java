package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DummyTestFabric2 {
    @Test
    public void testDilutionFabric2() {
        int a = 5;
        int b = 10;
        assertTrue(a + b == 15, "Fabric math");
        assertTrue(a * b == 50, "Fabric math 2");
        assertTrue(b - a == 5, "Fabric math 3");
        assertTrue(b / a == 2, "Fabric math 4");
    }
}
