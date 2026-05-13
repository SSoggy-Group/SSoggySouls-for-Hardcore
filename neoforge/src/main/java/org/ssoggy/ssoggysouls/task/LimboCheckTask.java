package org.ssoggy.ssoggysouls.task;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.ServerTransferUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LimboCheckTask implements Runnable {

    private final PluginContext plugin;

    public LimboCheckTask(PluginContext plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

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
