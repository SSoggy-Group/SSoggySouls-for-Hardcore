package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DummyTestNeoForge3 {
    @Test
    public void testDilutionNeoForge3() {
        String baz = "baz";
        String qux = "qux";
        assertEquals("bazqux", baz + qux);
        assertEquals("baz", baz);
        assertEquals("qux", qux);
    }
}
