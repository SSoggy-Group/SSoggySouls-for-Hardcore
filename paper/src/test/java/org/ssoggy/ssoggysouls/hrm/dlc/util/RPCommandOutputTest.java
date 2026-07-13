package org.ssoggy.ssoggysouls.hrm.dlc.util;

import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.COMMANDOUTPUTENUM;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RPCommandOutputTest {

    @Test
    void testToStringTrue() {
        RPCommandOutput output = new RPCommandOutput();
        output.success = COMMANDOUTPUTENUM.TRUE;
        output.message = "Test message";
        assertEquals("\n" + RPStatic.PREFIX + " <green>Success! Test message</green>\n", output.toString());
    }

    @Test
    void testToStringFalse() {
        RPCommandOutput output = new RPCommandOutput();
        output.success = COMMANDOUTPUTENUM.FALSE;
        output.message = "Test message";
        assertEquals("\n" + RPStatic.PREFIX + " <red>Failure. Test message</red>\n", output.toString());
    }

    @Test
    void testToStringInfo() {
        RPCommandOutput output = new RPCommandOutput();
        output.success = COMMANDOUTPUTENUM.INFO;
        output.message = "Test message";
        assertEquals("\n" + RPStatic.PREFIX + " <gray>Test message</gray>\n", output.toString());
    }
}
