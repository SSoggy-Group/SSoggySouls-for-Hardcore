package org.ssoggy.ssoggysouls.hrm.dlc.util;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.STATSENUM;

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

class RPStatsTest {

    private File tempFolder;
    private UUID uuid;
    private RPStats rpStats;

    @BeforeEach
    void setup() throws IOException {
        tempFolder = Files.createTempDirectory("rpstats").toFile();
        JavaPlugin mockPlugin = mock(JavaPlugin.class);
        Server mockServer = mock(Server.class);
        when(mockPlugin.getServer()).thenReturn(mockServer);
        when(mockPlugin.getDataFolder()).thenReturn(tempFolder);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("test"));

        RPStatic.init(mockPlugin);

        uuid = UUID.randomUUID();
        rpStats = new RPStats(uuid);
    }

    @AfterEach
    void tearDown() {
        if (RPStatic.STATS_STORAGE != null) RPStatic.STATS_STORAGE.shutdown();
        if (RPStatic.DEAD_STORAGE != null) RPStatic.DEAD_STORAGE.shutdown();
        if (RPStatic.SOCIAL_STORAGE != null) RPStatic.SOCIAL_STORAGE.shutdown();
        if (RPStatic.USERNAME_CACHE != null) RPStatic.USERNAME_CACHE.shutdown();

        File[] files = tempFolder.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        tempFolder.delete();
    }

    @Test
    void testGetAndOverrideStat() {
        assertNull(rpStats.getStat(STATSENUM.KILLS));

        rpStats.overrideStat(STATSENUM.KILLS, 5.0);
        assertEquals("5.0", rpStats.getStat(STATSENUM.KILLS));
    }

    @Test
    void testGetAllStats() {
        rpStats.overrideStat(STATSENUM.KILLS, "10");
        Map<STATSENUM, String> allStats = rpStats.getAllStats();

        assertEquals(STATSENUM.VALUES.size(), allStats.size());
        assertEquals("10", allStats.get(STATSENUM.KILLS));
        assertNull(allStats.get(STATSENUM.DEATHS));
    }

    @Test
    void testIncrementStatFromNull() {
        double result = rpStats.incrementStat(STATSENUM.REVIVES, 2.5);
        assertEquals(2.5, result);
        assertEquals("2.5", rpStats.getStat(STATSENUM.REVIVES));
    }

    @Test
    void testIncrementStatExisting() {
        rpStats.overrideStat(STATSENUM.REVIVES, "3.0");
        double result = rpStats.incrementStat(STATSENUM.REVIVES, 1.5);
        assertEquals(4.5, result);
        assertEquals("4.5", rpStats.getStat(STATSENUM.REVIVES));
    }

    @Test
    void testIncrementStatInvalidNumber() {
        rpStats.overrideStat(STATSENUM.REVIVES, "invalid");
        double result = rpStats.incrementStat(STATSENUM.REVIVES, 1.0);
        assertEquals(Double.NEGATIVE_INFINITY, result);
    }

    @Test
    void testDecrementStat() {
        rpStats.overrideStat(STATSENUM.LEVEL, "10.0");
        double result = rpStats.decrementStat(STATSENUM.LEVEL, 3.0);
        assertEquals(7.0, result);
        assertEquals("7.0", rpStats.getStat(STATSENUM.LEVEL));
    }
}
