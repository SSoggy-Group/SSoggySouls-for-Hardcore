package org.ssoggy.ssoggysouls.database;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

/**
 * Handles thread-safe in-memory caching of player death status with a
 * Time-To-Live (TTL) of 2 seconds to reduce repeated database queries.
 */
public class DeathStatusCache {

    private static final long CACHE_TTL_MS = 2000;
    private final Map<UUID, CachedDeathStatus> cache = new ConcurrentHashMap<>();

    private static class CachedDeathStatus {
        final boolean isDead;
        final long timestamp;

        CachedDeathStatus(boolean isDead) {
            this.isDead = isDead;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }

    /**
     * Returns the cached death status for the given UUID if it exists and has not
     * expired, or {@code null} otherwise.
     *
     * @param uuid player's UUID
     */
    @Nullable
    public Boolean get(UUID uuid) {
        if (uuid == null) return null;
        CachedDeathStatus cached = cache.get(uuid);
        if (cached != null && !cached.isExpired()) {
            return cached.isDead;
        }
        return null;
    }

    /**
     * Caches a player's death status with the current timestamp.
     *
     * @param uuid   player's UUID
     * @param isDead whether the player is dead
     */
    public void put(UUID uuid, boolean isDead) {
        if (uuid != null) {
            cache.put(uuid, new CachedDeathStatus(isDead));
        }
    }

    /**
     * Removes a player's death status from the cache.
     *
     * @param uuid player's UUID
     */
    public void remove(UUID uuid) {
        if (uuid != null) {
            cache.remove(uuid);
        }
    }

    /**
     * Clears all entries from the cache.
     */
    public void clear() {
        cache.clear();
    }
}
