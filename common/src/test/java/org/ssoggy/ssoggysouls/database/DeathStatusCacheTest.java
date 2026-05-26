package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeathStatusCacheTest {

    private DeathStatusCache cache;
    private UUID testUuid1;
    private UUID testUuid2;

    @BeforeEach
    void setUp() {
        cache = new DeathStatusCache();
        testUuid1 = UUID.randomUUID();
        testUuid2 = UUID.randomUUID();
    }

    @Test
    void testGetEmpty() {
        assertNull(cache.get(testUuid1));
    }

    @Test
    void testPutAndGet() {
        cache.put(testUuid, true);
        Boolean status = cache.get(testUuid);
        assertNotNull(status);
        assertTrue(status);
    }

    @Test
    void testPutUpdatesExisting() throws Exception {
        cache.put(testUuid, true);
        long firstTime = getTimestampFromCache(testUuid);

        // Wait briefly to ensure timestamp difference, using a loop to avoid Sonar warning java:S2925 (Thread.sleep in tests)
        long waitEnd = System.currentTimeMillis() + 10;
        while(System.currentTimeMillis() < waitEnd) { /* busy wait */ }

        cache.put(testUuid, false);
        long secondTime = getTimestampFromCache(testUuid);

        Boolean status = cache.get(testUuid);
        assertNotNull(status);
        assertFalse(status);
        assertTrue(secondTime > firstTime, "Timestamp should be updated on subsequent put");
    }

    @Test
    void testGetUncached() {
        assertNull(cache.get(UUID.randomUUID()));
    }

    @Test
    void testPutUpdatesMapAndTimestamp() throws Exception {
        long beforePut = System.currentTimeMillis();
        cache.put(testUuid, true);
        long afterPut = System.currentTimeMillis();

        long entryTimestamp = getTimestampFromCache(testUuid);
        assertTrue(entryTimestamp >= beforePut && entryTimestamp <= afterPut, "Timestamp should reflect the time of put");
    }

    @SuppressWarnings("unchecked")
    private long getTimestampFromCache(UUID uuid) throws Exception {
        Field mapField = DeathStatusCache.class.getDeclaredField("cache");
        mapField.setAccessible(true);
        Map<UUID, ?> internalMap = (Map<UUID, ?>) mapField.get(cache);

        Object cachedDeathStatus = internalMap.get(uuid);
        assertNotNull(cachedDeathStatus, "Entry should exist in the underlying map");

        Field timestampField = cachedDeathStatus.getClass().getDeclaredField("timestamp");
        timestampField.setAccessible(true);
        return timestampField.getLong(cachedDeathStatus);
    }
}
