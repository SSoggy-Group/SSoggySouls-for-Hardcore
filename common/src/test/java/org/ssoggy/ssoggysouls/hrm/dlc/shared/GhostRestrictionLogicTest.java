package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GhostRestrictionLogicTest {

    @Test
    void testIsOutOfBounds() {
        // Within bounds
        assertFalse(GhostRestrictionLogic.isOutOfBounds(0, 0, 0, 3, 0, 4, 5.1));
        
        // Exactly on bounds (isOutOfBounds uses > not >=)
        assertFalse(GhostRestrictionLogic.isOutOfBounds(0, 0, 0, 3, 0, 4, 5.0));
        
        // Out of bounds
        assertTrue(GhostRestrictionLogic.isOutOfBounds(0, 0, 0, 3, 0, 4, 4.9));
        
        // Far out
        assertTrue(GhostRestrictionLogic.isOutOfBounds(100, 100, 100, 200, 200, 200, 50));
    }

    @Test
    void testMessageConstant() {
        assertEquals("You may not travel that far away from your death location", GhostRestrictionLogic.RESTRICTION_MESSAGE);
    }
}
