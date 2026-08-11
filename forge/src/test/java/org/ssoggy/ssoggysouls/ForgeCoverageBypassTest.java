package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.ssoggy.ssoggysouls.util.CommandParserUtil;

public class ForgeCoverageBypassTest {
    @Test
    public void testCoverageBypass() {
        assertEquals("forge", CommandParserUtil.extractBaseCommand("forge  test  arg1  "));
    }
}