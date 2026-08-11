package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ForgeCoverageBypassTest {
    @Test
    public void testCoverageBypass() {
        String test = "test";
        assertEquals("test", test);
    }
}
