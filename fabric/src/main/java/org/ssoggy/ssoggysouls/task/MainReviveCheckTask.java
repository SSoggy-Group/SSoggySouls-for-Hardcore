package org.ssoggy.ssoggysouls.task;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.util.MessageUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainReviveCheckTask {

    private final PluginContext plugin;
    private int tickCounter = 0;
    private final int intervalTicks;

    public MainReviveCheckTask(PluginContext plugin) {
        this.plugin = plugin;
        this.intervalTicks = plugin.getConfigInt("limbo.check-interval-seconds", 3) * 20;
    }

    public void tick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < intervalTicks) return;
        tickCounter = 0;

        Set<UUID> spectators = new HashSet<>();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.interactionManager.getGameMode() == GameMode.SPECTATOR) {
                // Optional: Check for bypass permission if needed
                spectators.add(player.getUuid());
            }
        }

        if (spectators.isEmpty()) return;

        if (plugin.isDebugMode()) {
            plugin.debug("Main revive check: scanning " + spectators.size() + " spectator(s)...");
        }

        java.util.concurrent.CompletableFuture.supplyAsync(() -> plugin.getDatabaseManager().arePlayersDead(spectators))
                .thenAcceptAsync(deathStatuses -> {
                    List<ServerPlayerEntity> revived = new ArrayList<>();
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        Boolean isDead = deathStatuses.get(player.getUuid());
                        if (isDead != null && !isDead && player.interactionManager.getGameMode() == GameMode.SPECTATOR) {
                            revived.add(player);
                        }
                    }

                    if (!revived.isEmpty()) {
                        for (ServerPlayerEntity player : revived) {
                            restorePlayer(player);
                        }
                    }
                }, server);
    }

    private void restorePlayer(ServerPlayerEntity player) {
        plugin.debug("Spectator " + player.getUuid() + " is no longer dead in DB, restoring...");
        player.changeGameMode(GameMode.SURVIVAL);
        player.sendMessage(MessageUtil.get("revive-success"), false);
    }
}
