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
    void testGetNullUuid() {
        assertNull(cache.get(null));
    }

    @Test
    void testPutAndGet() {
        cache.put(testUuid1, true);
        cache.put(testUuid2, false);

        assertEquals(Boolean.TRUE, cache.get(testUuid1));
        assertEquals(Boolean.FALSE, cache.get(testUuid2));
    }

    @Test
    void testPutNullUuid() {
        cache.put(null, true);
        assertNull(cache.get(null));
    }

    @Test
    void testRemove() {
        cache.put(testUuid1, true);
        assertNotNull(cache.get(testUuid1));

        cache.remove(testUuid1);
        assertNull(cache.get(testUuid1));
    }

    @Test
    void testRemoveNullUuid() {
        assertDoesNotThrow(() -> cache.remove(null));
    }

    @Test
    void testClear() {
        cache.put(testUuid1, true);
        cache.put(testUuid2, false);

        cache.clear();

        assertNull(cache.get(testUuid1));
        assertNull(cache.get(testUuid2));
    }

    @Test
    void testExpirationTTL() throws NoSuchFieldException, IllegalAccessException {
        cache.put(testUuid1, true);

        Field cacheField = DeathStatusCache.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<UUID, Object> map = (Map<UUID, Object>) cacheField.get(cache);

        Object cachedDeathStatus = map.get(testUuid1);
        assertNotNull(cachedDeathStatus, "Cached entry should exist in the internal map");

        Field timestampField = cachedDeathStatus.getClass().getDeclaredField("timestamp");
        timestampField.setAccessible(true);
        // Set it to more than 2000ms ago to simulate expiration
        timestampField.set(cachedDeathStatus, System.currentTimeMillis() - 2500);

        assertNull(cache.get(testUuid1));
    }
}
