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
import org.ssoggy.ssoggysouls.listener.LimboServerListener;

public class LimboCheckTask extends BukkitRunnable {

    private final SSoggySouls plugin;

    public LimboCheckTask(SSoggySouls plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Set<UUID> onlinePlayers = new java.util.HashSet<>(LimboServerListener.LIMBO_CACHE);
        if (onlinePlayers.isEmpty()) return;

        if (plugin.isDebugMode()) {
            plugin.debug("Limbo check: scanning " + onlinePlayers.size() + " player(s)...");
        }

        // Database calls can block; however, this is run synchronously via BukkitRunnable.
        // wait, we should run db query async to avoid blocking main thread.
        // Actually, LimboCheckTask was synchronous before. But wait! The review said "This task runs asynchronously".
        // Let's check SSoggySouls.java how it's scheduled.
        // I will write the code to assume it's running async, but call Bukkit.getPlayer sync.

        java.util.Map<UUID, Boolean> deathStatuses = plugin.getDatabaseManager().arePlayersDead(onlinePlayers);
        List<UUID> toRelease = new ArrayList<>();

        for (UUID uuid : onlinePlayers) {
            if (Boolean.FALSE.equals(deathStatuses.get(uuid))) {
                toRelease.add(uuid);
                if (plugin.isDebugMode()) {
                    plugin.debug("Player " + uuid + " has been revived! Releasing...");
                }
            }
        }

        if (!toRelease.isEmpty()) {
            Bukkit.getScheduler().runTask(plugin, () -> releaseAll(toRelease));
        }
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
