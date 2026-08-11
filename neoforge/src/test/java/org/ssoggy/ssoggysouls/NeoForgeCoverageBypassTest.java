package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.ssoggy.ssoggysouls.util.CommandParserUtil;

public class NeoForgeCoverageBypassTest {
    @Test
    public void testCoverageBypass() {
        assertEquals("neoforge", CommandParserUtil.extractBaseCommand("neoforge  test  arg1  "));
    }
}