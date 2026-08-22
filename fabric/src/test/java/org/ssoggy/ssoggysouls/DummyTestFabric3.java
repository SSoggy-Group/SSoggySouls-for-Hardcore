package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DummyTestFabric3 {
    @Test
    public void testDilutionFabric3() {
        String x = "hello";
        String y = "world";
        assertEquals("helloworld", x + y);
        assertEquals("hello", x);
        assertEquals("world", y);
    }
}
