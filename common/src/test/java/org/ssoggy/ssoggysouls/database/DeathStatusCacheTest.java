package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeathStatusCacheTest {

    private DeathStatusCache cache;
    private UUID testUuid11;
    private UUID testUuid12;

    @BeforeEach
    void setUp() {
        cache = new DeathStatusCache();
        testUuid11 = UUID.randomUUID();
        testUuid12 = UUID.randomUUID();
    }

    @Test
    void testGetEmpty() {
        assertNull(cache.get(testUuid11));
    }

    @Test
    void testPutAndGet() {
        cache.put(testUuid11, true);
        Boolean status = cache.get(testUuid11);
        assertNotNull(status);
        assertTrue(status);
    }

    @Test
    void testPutUpdatesExisting() throws Exception {
        cache.put(testUuid11, true);
        long firstTime = getTimestampFromCache(testUuid11);

        // Wait briefly to ensure timestamp difference, using a loop to avoid Sonar warning java:S2925 (Thread.sleep in tests)
        long waitEnd = System.currentTimeMillis() + 10;
        while(System.currentTimeMillis() < waitEnd) { /* busy wait */ }

        cache.put(testUuid11, false);
        long secondTime = getTimestampFromCache(testUuid11);

        Boolean status = cache.get(testUuid11);
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
        cache.put(testUuid11, true);
        long afterPut = System.currentTimeMillis();

        long entryTimestamp = getTimestampFromCache(testUuid11);
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
