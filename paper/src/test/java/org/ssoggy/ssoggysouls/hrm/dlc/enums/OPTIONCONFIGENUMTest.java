package org.ssoggy.ssoggysouls.hrm.dlc.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OPTIONCONFIGENUMTest {
    @Test
    void testValuesCached() {
        assertEquals(OPTIONCONFIGENUM.values().length, OPTIONCONFIGENUM.VALUES.size());
    }
    @Test
    void testUtilityMethods() {
        assertEquals(OPTIONCONFIGENUM.STRUCTURE.index, OPTIONCONFIGENUM.getIndex(OPTIONCONFIGENUM.STRUCTURE.id));
        assertEquals(OPTIONCONFIGENUM.STRUCTURE, OPTIONCONFIGENUM.getEnumFromVal(OPTIONCONFIGENUM.STRUCTURE.id));
        assertEquals(OPTIONCONFIGENUM.STRUCTURE.id, OPTIONCONFIGENUM.getValue(OPTIONCONFIGENUM.STRUCTURE.index));
        assertEquals((byte)-1, OPTIONCONFIGENUM.getIndex("UNKNOWN"));
        assertNull(OPTIONCONFIGENUM.getEnumFromVal("UNKNOWN"));
        assertEquals("", OPTIONCONFIGENUM.getValue((byte) -5));
    }
}
