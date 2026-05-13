package org.ssoggy.ssoggysouls.task;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.util.MessageUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainReviveCheckTask implements Runnable {

    private final PluginContext plugin;

    public MainReviveCheckTask(PluginContext plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

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
