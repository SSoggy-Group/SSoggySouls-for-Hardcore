package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeathStatusCacheTest {

    private DeathStatusCache cache;
    private final UUID testUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        cache = new DeathStatusCache();
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

        // Wait briefly to ensure timestamp difference
        Thread.sleep(10);

        cache.put(testUuid, false);
        long secondTime = getTimestampFromCache(testUuid);

        Boolean status = cache.get(testUuid);
        assertNotNull(status);
        assertFalse(status);
        assertTrue(secondTime > firstTime, "Timestamp should be updated on subsequent put");
    }

    @Test
    void testGetNullUuid() {
        assertNull(cache.get(null));
    }

    @Test
    void testGetUnknownUuid() {
        assertNull(cache.get(testUuid));
    }

    @Test
    void testGetKnownUuidDead() {
        cache.put(testUuid, true);
        Boolean result = cache.get(testUuid);
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    void testGetKnownUuidAlive() {
        cache.put(testUuid, false);
        Boolean result = cache.get(testUuid);
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    void testGetExpiredUuid() throws Exception {
        cache.put(testUuid, true);

        Field cacheField = DeathStatusCache.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        Map<?, ?> internalCache = (Map<?, ?>) cacheField.get(cache);

        Object entry = internalCache.get(testUuid);
        Field timestampField = entry.getClass().getDeclaredField("timestamp");
        timestampField.setAccessible(true);
        timestampField.set(entry, System.currentTimeMillis() - 3000);

        assertNull(cache.get(testUuid));
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
