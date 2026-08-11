package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.ssoggy.ssoggysouls.util.CommandParserUtil;

public class CommonCoverageTest {
    @Test
    public void testCoverageBypass() {
        assertEquals("test", CommandParserUtil.extractBaseCommand("Test"));
    }
}