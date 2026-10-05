package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.PluginContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DlcStatsTest {

    private File tempFolder;
    private UUID uuid;
    private DlcStats dlcStats;

    @BeforeEach
    void setup() throws IOException {
        tempFolder = Files.createTempDirectory("dlcstats").toFile();
        PluginContext context = mock(PluginContext.class);
        when(context.getLogger()).thenReturn(mock(Logger.class));
        when(context.getDataFolder()).thenReturn(tempFolder);
        DlcServices.init(context);

        uuid = UUID.randomUUID();
        dlcStats = new DlcStats(uuid);
    }

    @AfterEach
    void tearDown() {
        File rplus = new File(tempFolder, "revivalplus");
        if (rplus.exists()) {
            File[] files = rplus.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            rplus.delete();
        }
        tempFolder.delete();
    }

    @Test
    void testGetAndOverrideStat() {
        assertNull(dlcStats.getStat(DlcStat.KILLS));

        dlcStats.overrideStat(DlcStat.KILLS, 5.0);
        assertEquals("5.0", dlcStats.getStat(DlcStat.KILLS));
    }

    @Test
    void testGetAllStats() {
        dlcStats.overrideStat(DlcStat.KILLS, "10");
        Map<DlcStat, String> allStats = dlcStats.getAllStats();

        assertEquals(DlcStat.VALUES.size(), allStats.size());
        assertEquals("10", allStats.get(DlcStat.KILLS));
        assertNull(allStats.get(DlcStat.DEATHS));
    }

    @Test
    void testIncrementStatFromNull() {
        double result = dlcStats.incrementStat(DlcStat.REVIVES, 2.5);
        assertEquals(2.5, result);
        assertEquals("2.5", dlcStats.getStat(DlcStat.REVIVES));
    }

    @Test
    void testIncrementStatExisting() {
        dlcStats.overrideStat(DlcStat.REVIVES, "3.0");
        double result = dlcStats.incrementStat(DlcStat.REVIVES, 1.5);
        assertEquals(4.5, result);
        assertEquals("4.5", dlcStats.getStat(DlcStat.REVIVES));
    }

    @Test
    void testIncrementStatInvalidNumber() {
        dlcStats.overrideStat(DlcStat.REVIVES, "not_a_number");
        double result = dlcStats.incrementStat(DlcStat.REVIVES, 1.0);
        assertEquals(Double.NEGATIVE_INFINITY, result);
    }

    @Test
    void testDecrementStat() {
        dlcStats.overrideStat(DlcStat.LEVEL, "10.0");
        double result = dlcStats.decrementStat(DlcStat.LEVEL, 3.0);
        assertEquals(7.0, result);
        assertEquals("7.0", dlcStats.getStat(DlcStat.LEVEL));
    }
}
