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

public class LimboCheckTask extends BukkitRunnable {

    private static final String PERM_BYPASS = "ssoggysouls.bypass";

    private final SSoggySouls plugin;

    public LimboCheckTask(SSoggySouls plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Set<UUID> onlinePlayers = collectOnlinePlayers();
        if (onlinePlayers.isEmpty()) return;

        // Avoid string concatenation overhead unless debug is enabled
        if (plugin.isDebugMode()) {
            plugin.debug("Limbo check: scanning " + onlinePlayers.size() + " player(s)...");
        }

        List<UUID> toRelease = findRevivedPlayers(onlinePlayers);

        if (!toRelease.isEmpty()) {
            Bukkit.getScheduler().runTask(plugin, () -> releaseAll(toRelease));
        }
    }

    private Set<UUID> collectOnlinePlayers() {
        Set<UUID> players = new java.util.HashSet<>();
        Set<UUID> deadPlayers = plugin.getLimboDeadPlayers();
        // Bolt Optimization: Iterate over dead players instead of all online players
        // This changes the complexity from O(N) to O(M) where M is typically much smaller than N
        for (UUID uuid : deadPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                players.add(uuid);
            }
        }
        return players;
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
        Set<UUID> deadPlayers = plugin.getLimboDeadPlayers();
        for (UUID uuid : uuids) {
            deadPlayers.remove(uuid);
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
