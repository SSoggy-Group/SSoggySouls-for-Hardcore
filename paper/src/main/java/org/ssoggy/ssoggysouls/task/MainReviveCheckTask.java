package org.ssoggy.ssoggysouls.task;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.listener.MainServerListener;

public class MainReviveCheckTask extends BukkitRunnable {

    private static final String PERM_BYPASS = "ssoggysouls.bypass";

    private final SSoggySouls plugin;

    public MainReviveCheckTask(SSoggySouls plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        java.util.Set<UUID> spectators = new java.util.HashSet<>();
        for (UUID uuid : MainServerListener.SPECTATOR_CACHE) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.getGameMode() == GameMode.SPECTATOR
                    && !player.hasPermission(PERM_BYPASS)) {
                spectators.add(uuid);
            }
        }

        if (spectators.isEmpty()) return;

        if (plugin.isDebugMode()) {
            plugin.debug("Main revive check: scanning " + spectators.size() + " spectator(s)...");
        }

        java.util.Map<UUID, Boolean> deathStatuses = plugin.getDatabaseManager().arePlayersDead(spectators);
        List<UUID> revived = new ArrayList<>();

        for (UUID uuid : spectators) {
            Boolean isDead = deathStatuses.get(uuid);
            if (isDead != null && !isDead) {
                revived.add(uuid);
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
