package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DlcTrustActionTest {

    @Test
    void testValuesCached() {
        assertEquals(DlcTrustAction.values().length, DlcTrustAction.VALUES.size());
    }

    @Test
    void testFromInputValid() {
        assertEquals(Optional.of(DlcTrustAction.GRANT), DlcTrustAction.fromInput("grant"));
        assertEquals(Optional.of(DlcTrustAction.REVOKE), DlcTrustAction.fromInput("  rEvOkE  "));
        assertEquals(Optional.of(DlcTrustAction.INFO), DlcTrustAction.fromInput("INFO"));
        assertEquals(Optional.of(DlcTrustAction.BLOCK), DlcTrustAction.fromInput("block"));
    }

    @Test
    void testFromInputInvalidOrBlank() {
        assertFalse(DlcTrustAction.fromInput(null).isPresent());
        assertFalse(DlcTrustAction.fromInput("").isPresent());
        assertFalse(DlcTrustAction.fromInput("   ").isPresent());
        assertFalse(DlcTrustAction.fromInput("unknown_action").isPresent());
    }
}
