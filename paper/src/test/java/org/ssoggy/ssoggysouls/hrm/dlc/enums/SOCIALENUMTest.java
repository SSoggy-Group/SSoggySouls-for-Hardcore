package org.ssoggy.ssoggysouls.hrm.dlc.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SOCIALENUMTest {

    @Test
    void testIsTrustworthy() {
        assertTrue(SOCIALENUM.TRUSTED.isTrustworthy());
        assertTrue(SOCIALENUM.FRIENDS.isTrustworthy());
        assertFalse(SOCIALENUM.UNTRUSTED.isTrustworthy());
        assertFalse(SOCIALENUM.BLOCKED.isTrustworthy());
    }
}
