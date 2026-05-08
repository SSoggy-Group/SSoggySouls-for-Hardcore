package org.ssoggy.ssoggysouls.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.ServerTransferUtil;

public class LeaveLimboCommand implements CommandExecutor {

    private static final long TRANSFER_DELAY_TICKS = 20L;

    private final SSoggySouls plugin;

    public LeaveLimboCommand(SSoggySouls plugin) {
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("java:S3516")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            handleLeave(player);
        } else {
            sender.sendMessage(MessageUtil.get("command-only-players"));
        }
        return true;
    }

    private void handleLeave(Player player) {
        // Run asynchronously to catch players who might not yet be in limboDeadPlayers
        // during the initial join delay
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                boolean isDead = plugin.getDatabaseManager().isPlayerDead(player.getUniqueId());

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) return;

                    if (isDead || plugin.getLimboDeadPlayers().contains(player.getUniqueId())) {
                        player.sendMessage(MessageUtil.get("limbo-cannot-leave"));
                        return;
                    }

                    player.sendMessage(MessageUtil.get("limbo-visit-leaving"));
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline()) {
                            ServerTransferUtil.sendToMain(player);
                        }
                    }, TRANSFER_DELAY_TICKS);
                });
            } catch (Exception e) {
                plugin.getLogger().severe(
                        "Failed to check limbo status for player "
                                + player.getUniqueId()
                                + ": "
                                + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        player.sendMessage(MessageUtil.get("command-error-database"));
                    }
                });
            }
        });
    }
}
