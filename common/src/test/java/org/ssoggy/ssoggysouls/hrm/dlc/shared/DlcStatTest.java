package org.ssoggy.ssoggysouls.hrm.dlc.shared;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
public class DlcStatTest {
    @Test
    void testValuesCached() {
        assertEquals(DlcStat.values().length, DlcStat.VALUES.size());
    }
}
