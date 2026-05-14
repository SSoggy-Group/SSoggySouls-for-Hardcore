package org.ssoggy.ssoggysouls.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.util.MessageUtil;

// pings the db on main server for spectators who've been revived externally and then restores em to survival

import java.util.concurrent.ConcurrentHashMap;

public class MainReviveCheckTask extends BukkitRunnable {

    private static final String PERM_BYPASS = "ssoggysouls.bypass";

    private final SSoggySouls plugin;
    private final Set<UUID> trackedSpectators = ConcurrentHashMap.newKeySet();

    public MainReviveCheckTask(SSoggySouls plugin) {
        this.plugin = plugin;
    }

    public void addSpectator(UUID uuid) {
        trackedSpectators.add(uuid);
    }

    public void removeSpectator(UUID uuid) {
        trackedSpectators.remove(uuid);
    }

    @Override
    public void run() {
        // Clean up tracking set: remove offline players or those who are no longer spectators/have bypass
        trackedSpectators.removeIf(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            return p == null || p.getGameMode() != GameMode.SPECTATOR || p.hasPermission(PERM_BYPASS);
        });

        Set<UUID> spectatorsToCheck = new HashSet<>(trackedSpectators);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getGameMode() == GameMode.SPECTATOR && !onlinePlayer.hasPermission(PERM_BYPASS)) {
                UUID uuid = onlinePlayer.getUniqueId();
                spectatorsToCheck.add(uuid);
                trackedSpectators.add(uuid);
            }
        }

        if (spectatorsToCheck.isEmpty()) return;

        if (plugin.isDebugMode()) {
            plugin.debug("Main revive check: scanning " + spectatorsToCheck.size() + " spectator(s)...");
        }

        Map<UUID, Boolean> deathStatuses = plugin.getDatabaseManager().arePlayersDead(spectatorsToCheck);
        List<UUID> revived = new ArrayList<>();

        for (UUID uuid : spectatorsToCheck) {
            Boolean isDead = deathStatuses.get(uuid);
            if (isDead != null && !isDead) {
                revived.add(uuid);
                trackedSpectators.remove(uuid);
                if (plugin.isDebugMode()) {
                    plugin.debug("Spectator " + uuid + " is no longer dead in DB, restoring...");
                }
            }
        }

        if (!revived.isEmpty()) {
            Bukkit.getScheduler().runTask(plugin, () -> restoreAll(revived));
        }
    }

    private void restoreAll(List<UUID> uuids) {
        for (UUID uuid : uuids) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage(MessageUtil.get("revive-success"));
                plugin.getLogger().log(Level.INFO,
                        "Restored {0} from spectator to survival (revived in DB).",
                        player.getName());
            }
        }
    }
}
