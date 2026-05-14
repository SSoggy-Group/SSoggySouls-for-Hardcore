package org.ssoggy.ssoggysouls.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.ServerTransferUtil;

import java.util.concurrent.ConcurrentHashMap;

public class LimboCheckTask extends BukkitRunnable {

    private final SSoggySouls plugin;
    private final Set<UUID> trackedPlayers = ConcurrentHashMap.newKeySet();

    public LimboCheckTask(SSoggySouls plugin) {
        this.plugin = plugin;
    }

    public void addPlayer(UUID uuid) {
        trackedPlayers.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        trackedPlayers.remove(uuid);
    }

    @Override
    public void run() {
        // Clean up offline players
        trackedPlayers.removeIf(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            return p == null || p.getGameMode() != org.bukkit.GameMode.ADVENTURE || p.hasPermission("ssoggysouls.bypass");
        });

        if (trackedPlayers.isEmpty()) return;

        // Avoid string concatenation overhead unless debug is enabled
        if (plugin.isDebugMode()) {
            plugin.debug("Limbo check: scanning " + trackedPlayers.size() + " player(s)...");
        }

        List<UUID> toRelease = findRevivedPlayers(trackedPlayers);

        if (!toRelease.isEmpty()) {
            Bukkit.getScheduler().runTask(plugin, () -> releaseAll(toRelease));
        }
    }

    private List<UUID> findRevivedPlayers(Set<UUID> onlinePlayers) {
        List<UUID> toRelease = new ArrayList<>();
        java.util.Map<UUID, Boolean> deathStatuses = plugin.getDatabaseManager().arePlayersDead(onlinePlayers);
        for (UUID uuid : onlinePlayers) {
            if (Boolean.FALSE.equals(deathStatuses.get(uuid))) {
                toRelease.add(uuid);
                // Avoid string concatenation overhead unless debug is enabled
                if (plugin.isDebugMode()) {
                    plugin.debug("Player " + uuid + " has been revived! Releasing...");
                }
            }
        }
        return toRelease;
    }

    private void releaseAll(List<UUID> uuids) {
        for (UUID uuid : uuids) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                releasePlayer(player);
            }
        }
    }

    private void releasePlayer(Player player) {
        plugin.getLogger().log(Level.INFO, "Releasing {0} from Limbo!", player.getName());

        player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        player.sendMessage(MessageUtil.get("revive-success"));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                ServerTransferUtil.sendToMain(player);
            }
        }, 40L);
    }
}
