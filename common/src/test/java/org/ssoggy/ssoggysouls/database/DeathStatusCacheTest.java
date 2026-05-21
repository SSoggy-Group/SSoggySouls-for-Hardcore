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

        // Use reflection to forcefully expire the cache entry to avoid Thread.sleep(2000)
        Field cacheField = DeathStatusCache.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        Map<?, ?> internalCache = (Map<?, ?>) cacheField.get(cache);

        Object entry = internalCache.get(testUuid);
        Field timestampField = entry.getClass().getDeclaredField("timestamp");
        timestampField.setAccessible(true);

        // Set timestamp to 3000ms in the past
        timestampField.set(entry, System.currentTimeMillis() - 3000);

        assertNull(cache.get(testUuid));
    }
}
