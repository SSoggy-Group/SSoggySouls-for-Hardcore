package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.model.PlayerData;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoadMultipleBenchmarkTest {

    @TempDir
    Path tempDir;

    private PluginContext plugin;
    private SQLiteManager sqliteManager;
    private List<UUID> testUuids;
    private Set<UUID> testUuidSet;

    @BeforeEach
    void setup() throws Exception {
        plugin = mock(PluginContext.class);
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(java.util.logging.Level.OFF);
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getConfigString(eq("database.table-name"), anyString())).thenReturn("hardcore_players");
        when(plugin.getConfigInt(eq("database.max-pool-size"), anyInt())).thenReturn(1);
        when(plugin.getDefaultLives()).thenReturn(3);

        sqliteManager = new SQLiteManager(plugin);
        sqliteManager.initialize();

        // Populate 200 test players
        int playerCount = 200;
        testUuids = new ArrayList<>(playerCount);
        testUuidSet = new HashSet<>(playerCount);

        for (int i = 0; i < playerCount; i++) {
            UUID uuid = UUID.randomUUID();
            testUuids.add(uuid);
            testUuidSet.add(uuid);
            PlayerData data = new PlayerData(
                    uuid,
                    "Player_" + i,
                    3,
                    i % 2 == 0,
                    1000L + i,
                    2000L + i,
                    3000L + i,
                    4000L + i
            );
            sqliteManager.savePlayer(data);
        }
    }

    @AfterEach
    void tearDown() {
        if (sqliteManager != null) {
            sqliteManager.shutdown();
        }
    }

    @Test
    void testLoadMultipleFunctionalCorrectness() {
        Map<UUID, PlayerData> loaded = sqliteManager.loadMultiple(testUuidSet);

        assertNotNull(loaded);
        assertEquals(200, loaded.size());
        for (UUID uuid : testUuids) {
            PlayerData data = loaded.get(uuid);
            assertNotNull(data);
            assertEquals(uuid, data.getUuid());
            assertEquals(3, data.getLives());
        }

        // Test null and empty set handling
        assertTrue(sqliteManager.loadMultiple(null).isEmpty());
        assertTrue(sqliteManager.loadMultiple(Collections.emptySet()).isEmpty());

        // Test set with null element and non-existent element
        Set<UUID> mixed = new HashSet<>();
        mixed.add(null);
        mixed.add(UUID.randomUUID());
        mixed.add(testUuids.get(0));
        Map<UUID, PlayerData> mixedResult = sqliteManager.loadMultiple(mixed);
        assertEquals(1, mixedResult.size());
        assertTrue(mixedResult.containsKey(testUuids.get(0)));
    }

    @Test
    void benchmarkNPlusOneVsBatchLoading() {
        int iterations = 10;

        // Warmup
        for (int w = 0; w < 3; w++) {
            for (UUID uuid : testUuids) {
                sqliteManager.getPlayer(uuid);
            }
            sqliteManager.loadMultiple(testUuidSet);
        }

        // Baseline: N+1 queries
        long startN1 = System.nanoTime();
        for (int iter = 0; iter < iterations; iter++) {
            Map<UUID, PlayerData> n1Result = new HashMap<>();
            for (UUID uuid : testUuids) {
                PlayerData pd = sqliteManager.getPlayer(uuid);
                if (pd != null) {
                    n1Result.put(uuid, pd);
                }
            }
            assertEquals(200, n1Result.size());
        }
        long durationN1Ns = System.nanoTime() - startN1;
        double avgN1Ms = (durationN1Ns / 1_000_000.0) / iterations;

        // Optimized: Batched query
        long startBatch = System.nanoTime();
        for (int iter = 0; iter < iterations; iter++) {
            Map<UUID, PlayerData> batchResult = sqliteManager.loadMultiple(testUuidSet);
            assertEquals(200, batchResult.size());
        }
        long durationBatchNs = System.nanoTime() - startBatch;
        double avgBatchMs = (durationBatchNs / 1_000_000.0) / iterations;

        double speedup = avgN1Ms / avgBatchMs;
        System.out.printf(Locale.US,
                "[BENCHMARK] 200 records | N+1 baseline: %.2f ms | Batch optimized: %.2f ms | Speedup: %.1fx%n",
                avgN1Ms, avgBatchMs, speedup);
    }
}
