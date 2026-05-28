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

    @BeforeEach
    void setUp() {
        cache = new DeathStatusCache();
        testUuid1 = UUID.randomUUID();
    }

    @Test
    void testGetEmpty() {
        assertNull(cache.get(testUuid1));
    }

    @Test
    void testPutAndGet() {
        cache.put(testUuid1, true);
        Boolean status = cache.get(testUuid1);
        assertNotNull(status);
        assertTrue(status);
    }

    @Test
    void testPutUpdatesExisting() throws Exception {
        cache.put(testUuid1, true);
        long firstTime = getTimestampFromCache(testUuid1);

        // Wait briefly to ensure timestamp difference, using a loop to avoid Sonar warning java:S2925 (Thread.sleep in tests)
        long waitEnd = System.currentTimeMillis() + 10;
        while(System.currentTimeMillis() < waitEnd) { /* busy wait */ }

        cache.put(testUuid1, false);
        long secondTime = getTimestampFromCache(testUuid1);

        Boolean status = cache.get(testUuid1);
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
        cache.put(testUuid1, true);
        long afterPut = System.currentTimeMillis();

        long entryTimestamp = getTimestampFromCache(testUuid1);
        assertTrue(entryTimestamp >= beforePut && entryTimestamp <= afterPut, "Timestamp should reflect the time of put");
    }

    @Test
    void testPutNullUuid() throws Exception {
        assertDoesNotThrow(() -> cache.put(null, true));
        assertTrue(getInternalMap().isEmpty(), "Internal cache map should be empty when putting null UUID");
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, ?> getInternalMap() throws Exception {
        Field mapField = DeathStatusCache.class.getDeclaredField("cache");
        mapField.setAccessible(true);
        return (Map<UUID, ?>) mapField.get(cache);
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
