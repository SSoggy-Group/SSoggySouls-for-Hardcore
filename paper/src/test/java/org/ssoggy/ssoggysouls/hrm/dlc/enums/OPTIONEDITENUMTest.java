package org.ssoggy.ssoggysouls.hrm.dlc.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OPTIONEDITENUMTest {
    @Test
    void testValuesCached() {
        assertEquals(OPTIONEDITENUM.values().length, OPTIONEDITENUM.VALUES.size());
    }
    @Test
    void testUtilityMethods() {
        assertEquals(OPTIONEDITENUM.ADD.index, OPTIONEDITENUM.getIndex(OPTIONEDITENUM.ADD.id));
        assertEquals(OPTIONEDITENUM.ADD, OPTIONEDITENUM.getEnumFromVal(OPTIONEDITENUM.ADD.id));
        assertEquals(OPTIONEDITENUM.ADD.id, OPTIONEDITENUM.getValue(OPTIONEDITENUM.ADD.index));
        assertEquals((byte)-1, OPTIONEDITENUM.getIndex("UNKNOWN"));
        assertNull(OPTIONEDITENUM.getEnumFromVal("UNKNOWN"));
        assertEquals("", OPTIONEDITENUM.getValue((byte) -5));
    }
}
