package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DlcRelationTest {

    @Test
    void testIsTrustworthy() {
        assertTrue(DlcRelation.TRUSTED.isTrustworthy());
        assertTrue(DlcRelation.FRIENDS.isTrustworthy());
        assertFalse(DlcRelation.UNTRUSTED.isTrustworthy());
        assertFalse(DlcRelation.BLOCKED.isTrustworthy());
    }
}
