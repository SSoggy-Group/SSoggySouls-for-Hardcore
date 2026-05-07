package org.ssoggy.ssoggysouls.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.hrm.dlc.command.DlcCommandRegistration;
import org.ssoggy.ssoggysouls.hrm.dlc.listener.GhostModeEvents;
import org.ssoggy.ssoggysouls.hrm.dlc.shared.DlcDeaths;
import org.ssoggy.ssoggysouls.hrm.dlc.util.GhostState;
import org.ssoggy.ssoggysouls.listener.MainServerListener;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.AdminLogger;
import org.ssoggy.ssoggysouls.util.MessageUtil;

import java.util.concurrent.CompletableFuture;

public class CommandRegistration {

    private static final String PLAYER = "player";
    private static final String LIVES = "lives";

    private CommandRegistration() {
        // Utility class
    }

    public static void register(SSoggySoulsMod plugin, DatabaseManager db) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerStatusCommand(dispatcher, db);
            registerReviveCommand(dispatcher, plugin, db);
            registerSetLivesCommand(dispatcher, db);
            registerAdminLogCommand(dispatcher, plugin);
            DlcCommandRegistration.register(dispatcher, db);
        });
    }

    private static void registerStatusCommand(CommandDispatcher<ServerCommandSource> dispatcher, DatabaseManager db) {
        dispatcher.register(CommandManager.literal("pstatus")
            .executes(context -> {
                ServerCommandSource source = context.getSource();
                if (!source.isExecutedByPlayer()) {
                    source.sendError(MessageUtil.get("admin-players-only"));
                    return 0;
                }
                ServerPlayerEntity player = source.getPlayer();

                CompletableFuture.runAsync(() -> {
                    PlayerData data = db.getPlayer(player.getUuid());
                    if (data != null) {
                        source.getServer().execute(() ->
                            source.sendFeedback(() -> MessageUtil.get("status-self", LIVES, data.getLives()), false));
                    }
                });
                return 1;
            })
            .then(CommandManager.argument(PLAYER, StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(
                        context.getSource().getServer().getPlayerManager().getPlayerList().stream().map(p -> p.getName().getString()), builder))
                .executes(context -> {
                    String targetName = StringArgumentType.getString(context, PLAYER);
                    ServerCommandSource source = context.getSource();

                    CompletableFuture.runAsync(() -> {
                        PlayerData data = db.getPlayerByName(targetName);
                        if (data == null) {
                            source.getServer().execute(() ->
                                source.sendError(MessageUtil.get("status-not-found", PLAYER, targetName)));
                            return;
                        }

                        if (data.isDead()) {
                            source.getServer().execute(() ->
                                source.sendFeedback(() -> MessageUtil.get("status-other-dead", PLAYER, data.getUsername()), false));
                        } else {
                            source.getServer().execute(() ->
                                source.sendFeedback(() -> MessageUtil.get("status-other-alive",
                                        PLAYER, data.getUsername(),
                                        LIVES, data.getLives()), false));
                        }
                    });
                    return 1;
                })
            )
        );
    }

    private static void registerReviveCommand(CommandDispatcher<ServerCommandSource> dispatcher, SSoggySoulsMod plugin, DatabaseManager db) {
        dispatcher.register(CommandManager.literal("revive")
            // Require op level 2 or higher for now (since no permissions api is installed yet)
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument(PLAYER, StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(
                        context.getSource().getServer().getPlayerManager().getPlayerList().stream().map(p -> p.getName().getString()), builder))
                .executes(context -> {
                    String targetName = StringArgumentType.getString(context, PLAYER);
                    ServerCommandSource source = context.getSource();

                    CompletableFuture.runAsync(() -> {
                        PlayerData targetData = db.getPlayerByName(targetName);
                        if (targetData == null) {
                            source.getServer().execute(() ->
                                source.sendError(MessageUtil.get("revive-not-found", PLAYER, targetName)));
                            return;
                        }

                        if (!targetData.isDead()) {
                            source.getServer().execute(() ->
                                source.sendError(MessageUtil.get("revive-already-alive", PLAYER, targetData.getUsername())));
                            return;
                        }

                        boolean success = db.revivePlayer(targetData.getUuid(), plugin.getDefaultLives());
                        if (success) {
                            source.getServer().execute(() -> {
                                DlcDeaths.clearDeath(targetData.getUuid());
                                GhostModeEvents.updateGhostStatus(targetData.getUuid(), false);
                                org.ssoggy.ssoggysouls.listener.LimboServerListener.updateLimboStatus(targetData.getUuid(), false);
                                GhostState ghostState = GhostState.getServerState(source.getServer());
                                ghostState.deathLocations.remove(targetData.getUuid());
                                ghostState.deathHolders.remove(targetData.getUuid());
                                ghostState.markDirty();
                                source.sendFeedback(() -> MessageUtil.get("admin-revive-success", PLAYER, targetData.getUsername()), true);
                                AdminLogger.log(source.getName(), "Revived " + targetData.getUsername());

                                // Restore game mode if player is online
                                ServerPlayerEntity targetPlayer = source.getServer().getPlayerManager().getPlayer(targetData.getUuid());
                                if (targetPlayer != null) {
                                    targetPlayer.changeGameMode(net.minecraft.world.GameMode.SURVIVAL);
                                    MainServerListener.setGhostModeAttributes(targetPlayer, false);
                                    targetPlayer.sendMessage(MessageUtil.get("revive-success"), false);
                                }
                            });
                        }
                    });
                    return 1;
                })
            )
        );
    }

    private static void registerSetLivesCommand(CommandDispatcher<ServerCommandSource> dispatcher, DatabaseManager db) {
        dispatcher.register(CommandManager.literal("psetlives")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument(PLAYER, StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(
                        context.getSource().getServer().getPlayerManager().getPlayerList().stream().map(p -> p.getName().getString()), builder))
                .then(CommandManager.argument(LIVES, IntegerArgumentType.integer(0))
                    .executes(context -> {
                        String targetName = StringArgumentType.getString(context, PLAYER);
                        int lives = IntegerArgumentType.getInteger(context, LIVES);
                        ServerCommandSource source = context.getSource();

                        CompletableFuture.runAsync(() -> {
                            PlayerData data = db.getPlayerByName(targetName);
                            if (data == null) {
                                source.getServer().execute(() ->
                                    source.sendError(MessageUtil.get("status-not-found", PLAYER, targetName)));
                                return;
                            }
                            db.setLives(data.getUuid(), lives);
                            source.getServer().execute(() -> {
                                ServerPlayerEntity online = source.getServer().getPlayerManager().getPlayer(data.getUuid());
                                if (lives > 0) {
                                    DlcDeaths.clearDeath(data.getUuid());
                                    GhostModeEvents.updateGhostStatus(data.getUuid(), false);
                                    org.ssoggy.ssoggysouls.listener.LimboServerListener.updateLimboStatus(data.getUuid(), false);
                                    GhostState ghostState = GhostState.getServerState(source.getServer());
                                    ghostState.deathLocations.remove(data.getUuid());
                                    ghostState.deathHolders.remove(data.getUuid());
                                    ghostState.markDirty();
                                    if (online != null) {
                                        MainServerListener.setGhostModeAttributes(online, false);
                                        online.changeGameMode(net.minecraft.world.GameMode.SURVIVAL);
                                    }
                                }
                                source.sendFeedback(() -> MessageUtil.get("admin-setlives-success",
                                        PLAYER, data.getUsername(), LIVES, lives), true);
                                AdminLogger.log(source.getName(), "Set lives for " + data.getUsername() + " to " + lives);
                            });
                        });
                        return 1;
                    })
                )
            )
        );
    }



    private static void registerAdminLogCommand(CommandDispatcher<ServerCommandSource> dispatcher, SSoggySoulsMod plugin) {
        dispatcher.register(CommandManager.literal("adminlog")
            .requires(source -> source.hasPermissionLevel(3))
            .executes(context -> {
                ServerCommandSource source = context.getSource();

                CompletableFuture.runAsync(() -> {
                    java.io.File logFile = new java.io.File(plugin.getDataFolder(), AdminLogger.LOG_FILE_NAME);
                    if (!logFile.exists()) {
                        source.sendError(net.minecraft.text.Text.literal("No admin logs found."));
                        return;
                    }

                    try {
                        java.util.Deque<String> lines = readLastLines(logFile, 15);
                        source.getServer().execute(() -> {
                            source.sendMessage(net.minecraft.text.Text.literal("--- Recent Admin Logs ---").styled(s -> s.withColor(net.minecraft.util.Formatting.RED).withBold(true)));
                            for (String line : lines) {
                                source.sendMessage(net.minecraft.text.Text.literal(line).styled(s -> s.withColor(net.minecraft.util.Formatting.GRAY)));
                            }
                        });
                    } catch (java.io.IOException e) {
                        source.getServer().execute(() -> source.sendError(net.minecraft.text.Text.literal("Error reading admin log: " + e.getMessage())));
                    }
                });

                return 1;
            })
        );
    }

    private static java.util.Deque<String> readLastLines(java.io.File file, int maxLines) throws java.io.IOException {
        if (maxLines <= 0) return new java.util.ArrayDeque<>();
        java.util.Deque<String> lastLines = new java.util.ArrayDeque<>(maxLines);
        try (java.util.stream.Stream<String> lines = java.nio.file.Files.lines(file.toPath())) {
            lines.forEach(line -> {
                if (lastLines.size() >= maxLines) {
                    lastLines.pollFirst();
                }
                lastLines.addLast(line);
            });
        }
        return lastLines;
    }
}
