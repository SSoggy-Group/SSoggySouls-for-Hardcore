package org.ssoggy.ssoggysouls.hrm.dlc.util;

import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.COMMANDOUTPUTENUM;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RPCommandOutputTest {

    @Test
    void testNullSuccess() {
        RPCommandOutput output = new RPCommandOutput();
        output.success = COMMANDOUTPUTENUM.NULL;

        String res = output.toString();
        assertTrue(res.contains(RPStatic.PREFIX));
    }

    @Test
    void testFalseDefaultColor() {
        RPCommandOutput output = new RPCommandOutput();
        output.success = COMMANDOUTPUTENUM.FALSE;
        output.message = "An error occurred.";

        String res = output.toString();
        assertTrue(res.contains("<red>Failure. An error occurred.</red>"));
        assertTrue(res.contains(RPStatic.PREFIX));
    }

    @Test
    void testFalseCustomColor() {
        RPCommandOutput output = new RPCommandOutput();
        output.success = COMMANDOUTPUTENUM.FALSE;
        output.messageColour = "dark_red";
        output.message = "Custom failure";

        String res = output.toString();
        assertTrue(res.contains("<dark_red>Failure. Custom failure</dark_red>"));
    }

    @Test
    void testTrueDefaultColor() {
        RPCommandOutput output = new RPCommandOutput();
        output.success = COMMANDOUTPUTENUM.TRUE;
        output.message = "Operation done.";

        String res = output.toString();
        assertTrue(res.contains("<green>Success! Operation done.</green>"));
        assertTrue(res.contains(RPStatic.PREFIX));
    }

    @Test
    void testTrueCustomColor() {
        RPCommandOutput output = new RPCommandOutput();
        output.success = COMMANDOUTPUTENUM.TRUE;
        output.messageColour = "gold";
        output.message = "Special success";

        String res = output.toString();
        assertTrue(res.contains("<gold>Success! Special success</gold>"));
    }

    @Test
    void testInfoDefaultColor() {
        RPCommandOutput output = new RPCommandOutput();
        output.success = COMMANDOUTPUTENUM.INFO;
        output.message = "Informational message";

        String res = output.toString();
        assertTrue(res.contains("<gray>Informational message</gray>"));
        assertTrue(res.contains(RPStatic.PREFIX));
    }

    @Test
    void testRawReturnsDirectResultWithoutPrefix() {
        RPCommandOutput output = new RPCommandOutput();
        output.success = COMMANDOUTPUTENUM.RAW;
        output.message = "Raw unformatted text";

        String res = output.toString();
        assertEquals("<gray>Raw unformatted text</gray>", res);
    }
}
