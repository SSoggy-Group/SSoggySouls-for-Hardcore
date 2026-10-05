package org.ssoggy.ssoggysouls.hrm.dlc.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class COMMANDOUTPUTENUMTest {

    @Test
    void testGetIndex() {
        assertEquals((byte) -1, COMMANDOUTPUTENUM.NULL.getIndex());
        assertEquals((byte) 0, COMMANDOUTPUTENUM.FALSE.getIndex());
        assertEquals((byte) 1, COMMANDOUTPUTENUM.TRUE.getIndex());
        assertEquals((byte) 2, COMMANDOUTPUTENUM.INFO.getIndex());
        assertEquals((byte) 3, COMMANDOUTPUTENUM.RAW.getIndex());
    }

    @Test
    void testValueOfByte() {
        assertEquals(COMMANDOUTPUTENUM.FALSE, COMMANDOUTPUTENUM.valueOf((byte) 0));
        assertEquals(COMMANDOUTPUTENUM.TRUE, COMMANDOUTPUTENUM.valueOf((byte) 1));
        assertEquals(COMMANDOUTPUTENUM.INFO, COMMANDOUTPUTENUM.valueOf((byte) 2));
        assertEquals(COMMANDOUTPUTENUM.RAW, COMMANDOUTPUTENUM.valueOf((byte) 3));
        assertEquals(COMMANDOUTPUTENUM.NULL, COMMANDOUTPUTENUM.valueOf((byte) -1));
        assertEquals(COMMANDOUTPUTENUM.NULL, COMMANDOUTPUTENUM.valueOf((byte) 99));
    }
}
