package org.ssoggy.ssoggysouls.database;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.ssoggy.ssoggysouls.model.PlayerData;

public interface DatabaseManager {
    void initialize() throws DatabaseInitializationException;
    void shutdown();
    PlayerData getPlayer(UUID uuid);
    /**
     * Like {@link #getPlayer(UUID)} but propagates database errors, so callers can tell
     * "no record" ({@code null}) apart from "query failed" (exception).
     */
    PlayerData getPlayerStrict(UUID uuid) throws java.sql.SQLException;
    PlayerData getPlayerByName(String username);
    Map<UUID, PlayerData> loadMultiple(Set<UUID> uuids);
    void savePlayer(PlayerData data);
    boolean isPlayerDead(UUID uuid);
    Map<UUID, Boolean> arePlayersDead(Set<UUID> uuids);
    boolean revivePlayer(UUID uuid, int livesToRestore);
    /** @return true if a row was updated; false if no record exists or the write failed */
    boolean setLives(UUID uuid, int lives);
    void setFirstJoin(UUID uuid, long firstJoin);
    void setLastSeen(UUID uuid, long lastSeen);
    void setGraceUntil(UUID uuid, long graceUntil);
    /** Updates only the username column (avoids overwriting concurrent changes). */
    void setUsername(UUID uuid, String username);
    /**
     * Atomically adds one life to a living player, unless that would exceed
     * {@code maxLives} (ignored when {@code maxLives <= 0}).
     *
     * @return true if a life was added
     */
    boolean incrementLives(UUID uuid, int maxLives);
    void invalidateDeathStatusCache(UUID uuid);
    List<PlayerData> getDeadPlayers();
    String getPluginVersion(String key);
    void savePluginVersion(String key, String version);
}
