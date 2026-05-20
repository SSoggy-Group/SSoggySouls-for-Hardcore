package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DlcStatTest {

    @Test
    void testGetIdentifier() {
        assertEquals("kills", DlcStat.KILLS.getIdentifier());
        assertEquals("deaths", DlcStat.DEATHS.getIdentifier());
        assertEquals("revives", DlcStat.REVIVES.getIdentifier());
        assertEquals("ritual_started", DlcStat.RITUAL_STARTED.getIdentifier());
        assertEquals("ritual_completed", DlcStat.RITUAL_COMPLETED.getIdentifier());
        assertEquals("level", DlcStat.LEVEL.getIdentifier());
        assertEquals("friend_count", DlcStat.FRIEND_COUNT.getIdentifier());
        assertEquals("bounty_claimed", DlcStat.BOUNTY_CLAIMED.getIdentifier());
        assertEquals("bounty_placed", DlcStat.BOUNTY_PLACED.getIdentifier());
        assertEquals("total_bounty", DlcStat.TOTAL_BOUNTY.getIdentifier());
    }

    @Test
    void testGetAsPlaceholder() {
        // ordinal() values
        // KILLS = 0
        // DEATHS = 1
        // REVIVES = 2
        // RITUAL_STARTED = 3
        // RITUAL_COMPLETED = 4
        // LEVEL = 5
        // FRIEND_COUNT = 6
        // BOUNTY_CLAIMED = 7
        // BOUNTY_PLACED = 8
        // TOTAL_BOUNTY = 9
        assertEquals("0", DlcStat.KILLS.getAsPlaceholder());
        assertEquals("1", DlcStat.DEATHS.getAsPlaceholder());
        assertEquals("5", DlcStat.LEVEL.getAsPlaceholder());
        assertEquals("9", DlcStat.TOTAL_BOUNTY.getAsPlaceholder());
    }

    @Test
    void testGetValue() {
        assertEquals(0, DlcStat.KILLS.getValue());
        assertEquals(1, DlcStat.DEATHS.getValue());
        assertEquals(9, DlcStat.TOTAL_BOUNTY.getValue());
    }
}
