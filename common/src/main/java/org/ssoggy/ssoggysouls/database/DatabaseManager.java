package org.ssoggy.ssoggysouls.database;

import java.util.List;
import java.util.UUID;
import org.ssoggy.ssoggysouls.model.PlayerData;

public interface DatabaseManager {
    void initialize() throws DatabaseInitializationException;
    void shutdown();
    PlayerData getPlayer(UUID uuid);
    PlayerData getPlayerByName(String username);
    void savePlayer(PlayerData data);
    boolean isPlayerDead(UUID uuid);
    java.util.Map<UUID, Boolean> arePlayersDead(java.util.Set<UUID> uuids);
    boolean revivePlayer(UUID uuid, int livesToRestore);
    void setLives(UUID uuid, int lives);
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
