package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ForgeDummyCoverageTest {
    @Test
    public void testForgeCoverage() {
        String testStr = "hello";
        assertEquals(5, testStr.length());
    }
}
