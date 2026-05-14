package org.ssoggy.ssoggysouls.task;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.ServerTransferUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LimboCheckTask {

    private final PluginContext plugin;
    private int tickCounter = 0;
    private final int intervalTicks;

    public LimboCheckTask(PluginContext plugin) {
        this.plugin = plugin;
        this.intervalTicks = plugin.getConfigInt("limbo.check-interval-seconds", 3) * 20;
    }

    public void tick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < intervalTicks) return;
        tickCounter = 0;

        Set<UUID> onlinePlayers = new HashSet<>();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            onlinePlayers.add(player.getUuid());
        }

        if (onlinePlayers.isEmpty()) return;

        if (plugin.isDebugMode()) {
            plugin.debug("Limbo check: scanning " + onlinePlayers.size() + " player(s)...");
        }

        // Run DB check asynchronously to avoid lagging the main thread
        java.util.concurrent.CompletableFuture.supplyAsync(() -> plugin.getDatabaseManager().arePlayersDead(onlinePlayers))
                .thenAcceptAsync(deathStatuses -> {
                    List<ServerPlayerEntity> toRelease = new ArrayList<>();
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        if (Boolean.FALSE.equals(deathStatuses.get(player.getUuid()))) {
                            toRelease.add(player);
                        }
                    }

                    if (!toRelease.isEmpty()) {
                        for (ServerPlayerEntity player : toRelease) {
                            releasePlayer(player);
                        }
                    }
                }, server);
    }

    private void releasePlayer(ServerPlayerEntity player) {
        plugin.debug("Player " + player.getUuid() + " has been revived! Releasing...");
        
        player.changeGameMode(net.minecraft.world.GameMode.SURVIVAL);
        player.sendMessage(MessageUtil.get("revive-success"), false);

        // Schedule transfer back to main
        // In Fabric, we can use a simple delay or just fire it
        ServerTransferUtil.sendToMain(player);
    }
}
