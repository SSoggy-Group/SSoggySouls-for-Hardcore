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
import org.ssoggy.ssoggysouls.util.PermissionUtil;

import java.util.concurrent.CompletableFuture;

public class CommandRegistration {

    private static final String PLAYER = "player";
    private static final String LIVES = "lives";

    private CommandRegistration() {
        // Utility class
    }

    public static void register(SSoggySoulsMod plugin, DatabaseManager db) {
        CommandRegistrationCallback.EVENT.register((dispatcher, ignoredRegistryAccess, ignoredEnvironment) -> {
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
                        context.getSource().getServer().getPlayerNames(), builder))
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
            .executes(context -> {
                context.getSource().sendError(MessageUtil.get("usage-revive").copy()
                    .styled(s -> s.withColor(net.minecraft.util.Formatting.RED)
                        .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.SUGGEST_COMMAND, "/revive "))
                        .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, MessageUtil.get("click-to-autofill").copy().styled(h -> h.withColor(net.minecraft.util.Formatting.GRAY))))));
                return 0;
            })
            .then(CommandManager.argument(PLAYER, StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(
                        context.getSource().getServer().getPlayerNames(), builder))
                .executes(context -> {
                    String targetName = StringArgumentType.getString(context, PLAYER);
                    ServerCommandSource source = context.getSource();

                    if (PermissionUtil.isBlockedByLimboOpSecurity(source)) {
                        PermissionUtil.sendSecurityBlockMessage(source);
                        return 0;
                    }

                    CompletableFuture.runAsync(() -> executeRevive(targetName, source, plugin, db));
                    return 1;
                })
            )
        );
    }

    private static void executeRevive(String targetName, ServerCommandSource source, SSoggySoulsMod plugin, DatabaseManager db) {
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
                GhostState ghostState = GhostState.getServerState(source.getServer());
                ghostState.deathLocations.remove(targetData.getUuid());
                ghostState.deathHolders.remove(targetData.getUuid());
                ghostState.markDirty();
                org.ssoggy.ssoggysouls.hrm.HeadDropListener.removeDroppedHeads(targetData.getUuid(), source.getServer());
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
    }

    private static void registerSetLivesCommand(CommandDispatcher<ServerCommandSource> dispatcher, DatabaseManager db) {
        dispatcher.register(CommandManager.literal("psetlives")
            .requires(source -> source.hasPermissionLevel(2))
            .executes(context -> {
                context.getSource().sendError(MessageUtil.get("usage-psetlives").copy()
                    .styled(s -> s.withColor(net.minecraft.util.Formatting.RED)
                        .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.SUGGEST_COMMAND, "/psetlives "))
                        .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, MessageUtil.get("click-to-autofill").copy().styled(h -> h.withColor(net.minecraft.util.Formatting.GRAY))))));
                return 0;
            })
            .then(CommandManager.argument(PLAYER, StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(
                        context.getSource().getServer().getPlayerNames(), builder))
                .executes(context -> {
                    String targetName = StringArgumentType.getString(context, PLAYER);
                    context.getSource().sendError(MessageUtil.get("usage-psetlives-player", PLAYER, targetName).copy()
                        .styled(s -> s.withColor(net.minecraft.util.Formatting.RED)
                            .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.SUGGEST_COMMAND, "/psetlives " + targetName + " "))
                            .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, MessageUtil.get("click-to-autofill").copy().styled(h -> h.withColor(net.minecraft.util.Formatting.GRAY))))));
                    return 0;
                })
                .then(CommandManager.argument(LIVES, IntegerArgumentType.integer(0))
                    .executes(context -> {
                        String targetName = StringArgumentType.getString(context, PLAYER);
                        int lives = IntegerArgumentType.getInteger(context, LIVES);
                        ServerCommandSource source = context.getSource();

                        if (PermissionUtil.isBlockedByLimboOpSecurity(source)) {
                            PermissionUtil.sendSecurityBlockMessage(source);
                            return 0;
                        }

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
                                    GhostState ghostState = GhostState.getServerState(source.getServer());
                                    ghostState.deathLocations.remove(data.getUuid());
                                    ghostState.deathHolders.remove(data.getUuid());
                                    ghostState.markDirty();
                                    org.ssoggy.ssoggysouls.hrm.HeadDropListener.removeDroppedHeads(data.getUuid(), source.getServer());
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
                    org.ssoggy.ssoggysouls.command.action.AdminLogAction.AdminLogResult result =
                        org.ssoggy.ssoggysouls.command.action.AdminLogAction.execute(logFile, 15);

                    source.getServer().execute(() -> {
                        switch (result.type) {
                            case FILE_NOT_FOUND -> source.sendError(net.minecraft.text.Text.literal("No admin logs found."));
                            case READ_ERROR -> {
                                SSoggySoulsMod.LOGGER.error("Error reading admin log");
                                source.sendError(MessageUtil.get("admin-log-read-error"));
                            }
                            case SUCCESS -> {
                                source.sendMessage(MessageUtil.colorizeText("&6&l══ Admin Action Log ══").copy());
                                if (result.lines.isEmpty()) {
                                    source.sendMessage(MessageUtil.colorizeText("&7(Empty)").copy());
                                } else {
                                    for (String line : result.lines) {
                                        String formatted = org.ssoggy.ssoggysouls.command.action.AdminLogAction.formatLogLine(line);
                                        net.minecraft.text.MutableText component = MessageUtil.colorizeText(formatted).copy();
                                        if (source.isExecutedByPlayer()) {
                                            component = component.styled(s ->
                                                s.withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.COPY_TO_CLIPBOARD, line))
                                                .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, net.minecraft.text.Text.literal("Click to copy log entry").copy().styled(h -> h.withColor(net.minecraft.util.Formatting.GRAY))))
                                            );
                                        }
                                        source.sendMessage(component);
                                    }
                                }
                                source.sendMessage(MessageUtil.colorizeText("&6&l══════════════════════").copy());
                            }
                        }
                    });
                });

                return 1;
            })
        );
    }

}
