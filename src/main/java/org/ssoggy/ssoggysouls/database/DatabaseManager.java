package org.ssoggy.ssoggysouls.database;

import java.util.List;
import java.util.UUID;
import org.ssoggy.ssoggysouls.model.PlayerData;

public interface DatabaseManager {
    boolean initialize();
    void shutdown();
    PlayerData getPlayer(UUID uuid);
    PlayerData getPlayerByName(String username);
    void savePlayer(PlayerData data);
    boolean isPlayerDead(UUID uuid);
    java.util.Map<UUID, Boolean> arePlayersDead(List<UUID> uuids);
    boolean revivePlayer(UUID uuid, int livesToRestore);
    void setLives(UUID uuid, int lives);
    void setFirstJoin(UUID uuid, long firstJoin);
    void setLastSeen(UUID uuid, long lastSeen);
    void setGraceUntil(UUID uuid, long graceUntil);
    void invalidateDeathStatusCache(UUID uuid);
    List<PlayerData> getDeadPlayers();
    String getPluginVersion(String key);
    void savePluginVersion(String key, String version);
}
