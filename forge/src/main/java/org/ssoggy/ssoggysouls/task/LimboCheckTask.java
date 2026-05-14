package org.ssoggy.ssoggysouls.task;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.ServerTransferUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter < intervalTicks) return;
        tickCounter = 0;

        MinecraftServer server = event.getServer();
        Set<UUID> onlinePlayers = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            onlinePlayers.add(player.getUUID());
        }

        if (onlinePlayers.isEmpty()) return;

        if (plugin.isDebugMode()) {
            plugin.debug("Limbo check: scanning " + onlinePlayers.size() + " player(s)...");
        }

        java.util.concurrent.CompletableFuture.supplyAsync(() -> plugin.getDatabaseManager().arePlayersDead(onlinePlayers))
                .thenAcceptAsync(deathStatuses -> {
                    List<ServerPlayer> toRelease = new ArrayList<>();
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        if (Boolean.FALSE.equals(deathStatuses.get(player.getUUID()))) {
                            toRelease.add(player);
                        }
                    }

                    if (!toRelease.isEmpty()) {
                        for (ServerPlayer player : toRelease) {
                            releasePlayer(player);
                        }
                    }
                }, server);
    }

    private void releasePlayer(ServerPlayer player) {
        plugin.debug("Player " + player.getUUID() + " has been revived! Releasing...");
        
        player.setGameMode(GameType.SURVIVAL);
        player.sendSystemMessage(MessageUtil.get("revive-success"));

        ServerTransferUtil.sendToMain(player);
    }
}
