package org.ssoggy.ssoggysouls.task;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter < intervalTicks) return;
        tickCounter = 0;

        MinecraftServer server = event.getServer();
        Set<UUID> spectators = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                spectators.add(player.getUUID());
            }
        }

        if (spectators.isEmpty()) return;

        if (plugin.isDebugMode()) {
            plugin.debug("Main revive check: scanning " + spectators.size() + " spectator(s)...");
        }

        java.util.concurrent.CompletableFuture.supplyAsync(() -> plugin.getDatabaseManager().arePlayersDead(spectators))
                .thenAcceptAsync(deathStatuses -> {
                    List<ServerPlayer> revived = new ArrayList<>();
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        Boolean isDead = deathStatuses.get(player.getUUID());
                        if (isDead != null && !isDead && player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                            revived.add(player);
                        }
                    }

                    if (!revived.isEmpty()) {
                        for (ServerPlayer player : revived) {
                            restorePlayer(player);
                        }
                    }
                }, server);
    }

    private void restorePlayer(ServerPlayer player) {
        plugin.debug("Spectator " + player.getUUID() + " is no longer dead in DB, restoring...");
        player.setGameMode(GameType.SURVIVAL);
        player.sendSystemMessage(MessageUtil.get("revive-success"));
    }
}
