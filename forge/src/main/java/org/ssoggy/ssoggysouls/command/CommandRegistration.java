package org.ssoggy.ssoggysouls.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.hrm.dlc.command.DlcCommandRegistration;
import org.ssoggy.ssoggysouls.hrm.dlc.listener.GhostModeEvents;
import org.ssoggy.ssoggysouls.hrm.dlc.shared.DlcDeaths;
import org.ssoggy.ssoggysouls.hrm.dlc.util.GhostState;
import org.ssoggy.ssoggysouls.listener.ServerLifecycleListener;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.AdminLogger;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.PermissionUtil;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class CommandRegistration {
    
    private CommandRegistration() {
        // Utility class
    }

    private static final String PLAYER = "player";
    private static final String LIVES = "lives";
    private static DatabaseManager db;

    public static void setDatabase(DatabaseManager database) {
        db = database;
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        if (db == null) {
            SSoggySoulsMod.LOGGER.error("Cannot register commands: DatabaseManager is null");
            return;
        }
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        registerStatusCommand(dispatcher);
        registerReviveCommand(dispatcher);
        registerSetLivesCommand(dispatcher);
        registerAdminLogCommand(dispatcher);
        DlcCommandRegistration.register(dispatcher, db);
    }

    private static void registerStatusCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pstatus")
            .executes(context -> {
                CommandSourceStack source = context.getSource();
                if (!source.isPlayer()) {
                    source.sendFailure(MessageUtil.get("admin-players-only"));
                    return 0;
                }

                ServerPlayer player = source.getPlayer();
                CompletableFuture.runAsync(() -> {
                    PlayerData data = db.getPlayer(player.getUUID());
                    source.getServer().execute(() -> {
                        if (data == null) {
                            source.sendFailure(MessageUtil.get("admin-player-not-found", PLAYER, player.getScoreboardName()));
                            return;
                        }

                        source.sendSystemMessage(MessageUtil.get("status-self-header"));
                        source.sendSystemMessage(MessageUtil.get("status-self-lives", LIVES, data.getLives()));
                        source.sendSystemMessage(MessageUtil.get("status-self-state", "state", 
                                data.isDead() ? "Dead" : "Alive"));
                    });
                });
                return 1;
            })
            .then(Commands.argument(PLAYER, StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        context.getSource().getServer().getPlayerNames(), builder))
                .executes(context -> {
                    String targetName = StringArgumentType.getString(context, PLAYER);
                    CommandSourceStack source = context.getSource();

                    CompletableFuture.runAsync(() -> {
                        PlayerData data = db.getPlayerByName(targetName);
                        source.getServer().execute(() -> {
                            if (data == null) {
                                source.sendFailure(MessageUtil.get("admin-player-not-found", PLAYER, targetName));
                                return;
                            }

                            source.sendSystemMessage(MessageUtil.get("status-other-header", PLAYER, data.getUsername()));
                            source.sendSystemMessage(MessageUtil.get("status-other-lives", PLAYER, data.getUsername(), LIVES, data.getLives()));
                            source.sendSystemMessage(MessageUtil.get("status-other-state", PLAYER, data.getUsername(), "state", 
                                    data.isDead() ? "Dead" : "Alive"));
                        });
                    });
                    return 1;
                })
            )
        );
    }

    private static void registerReviveCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("revive")
            .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
            .executes(context -> {
                context.getSource().sendFailure(MessageUtil.get("usage-revive").copy()
                    .withStyle(s -> s.withColor(net.minecraft.ChatFormatting.RED)
                        .withClickEvent(new net.minecraft.network.chat.ClickEvent.SuggestCommand("/revive "))
                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(MessageUtil.get("click-to-autofill").copy().withStyle(net.minecraft.ChatFormatting.GRAY)))));
                return 0;
            })
            .then(Commands.argument(PLAYER, StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        context.getSource().getServer().getPlayerNames(), builder))
                .executes(context -> {
                    String targetName = StringArgumentType.getString(context, PLAYER);
                    CommandSourceStack source = context.getSource();

                    if (PermissionUtil.isBlockedByLimboOpSecurity(source)) {
                        PermissionUtil.sendSecurityBlockMessage(source);
                        return 0;
                    }

                    CompletableFuture.runAsync(() -> executeRevive(targetName, source));
                    return 1;
                })
            )
        );
    }

    private static void executeRevive(String targetName, CommandSourceStack source) {
        PlayerData data = db.getPlayerByName(targetName);
        if (data == null) {
            source.sendFailure(MessageUtil.get("admin-player-not-found", PLAYER, targetName));
            return;
        }

        if (!data.isDead()) {
            source.sendFailure(MessageUtil.get("admin-player-not-dead", PLAYER, data.getUsername()));
            return;
        }

        int onReviveLives = ConfigManager.getConfig().getOnReviveLives();
        boolean success = db.revivePlayer(data.getUuid(), onReviveLives);
        if (!success) {
            source.sendFailure(MessageUtil.get("admin-revive-failed", PLAYER, data.getUsername()));
            return;
        }

        PlayerData targetData = data;
        source.getServer().execute(() -> {
            AdminLogger.log(source.getTextName(), "Revived " + targetData.getUsername());

            GhostModeEvents.updateGhostStatus(targetData.getUuid(), false);
            DlcDeaths.clearDeath(targetData.getUuid());

            GhostState ghostState = GhostState.getServerState(source.getServer());
            ghostState.removeDeathLocation(targetData.getUuid());
            ghostState.removeDeathHolder(targetData.getUuid());
            ghostState.setDirty();

            ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayer(targetData.getUuid());
            if (targetPlayer != null) {
                targetPlayer.setGameMode(GameType.SURVIVAL);
                ServerLifecycleListener.setGhostModeAttributes(targetPlayer, false);
                targetPlayer.sendSystemMessage(MessageUtil.get("revive-success"));
            }

            source.sendSystemMessage(MessageUtil.get("admin-revive-success", PLAYER, targetData.getUsername(), LIVES, onReviveLives));
        });
    }

    private static void registerSetLivesCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("psetlives")
            .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
            .executes(context -> {
                context.getSource().sendFailure(MessageUtil.get("usage-psetlives").copy()
                    .withStyle(s -> s.withColor(net.minecraft.ChatFormatting.RED)
                        .withClickEvent(new net.minecraft.network.chat.ClickEvent.SuggestCommand("/psetlives "))
                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(MessageUtil.get("click-to-autofill").copy().withStyle(net.minecraft.ChatFormatting.GRAY)))));
                return 0;
            })
            .then(Commands.argument(PLAYER, StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        context.getSource().getServer().getPlayerNames(), builder))
                .executes(context -> {
                    String targetName = StringArgumentType.getString(context, PLAYER);
                    context.getSource().sendFailure(MessageUtil.get("usage-psetlives-player", PLAYER, targetName).copy()
                        .withStyle(s -> s.withColor(net.minecraft.ChatFormatting.RED)
                            .withClickEvent(new net.minecraft.network.chat.ClickEvent.SuggestCommand("/psetlives " + targetName + " "))
                            .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(MessageUtil.get("click-to-autofill").copy().withStyle(net.minecraft.ChatFormatting.GRAY)))));
                    return 0;
                })
                .then(Commands.argument(LIVES, IntegerArgumentType.integer(0))
                    .executes(context -> {
                        String targetName = StringArgumentType.getString(context, PLAYER);
                        int lives = IntegerArgumentType.getInteger(context, LIVES);
                        CommandSourceStack source = context.getSource();

                        if (PermissionUtil.isBlockedByLimboOpSecurity(source)) {
                            PermissionUtil.sendSecurityBlockMessage(source);
                            return 0;
                        }

                        CompletableFuture.runAsync(() -> {
                            PlayerData data = db.getPlayerByName(targetName);
                            if (data == null) {
                                source.sendFailure(MessageUtil.get("admin-player-not-found", PLAYER, targetName));
                                return;
                            }

                            data.setLives(lives);
                            db.savePlayer(data);

                            source.getServer().execute(() -> {
                                AdminLogger.log(source.getTextName(), "Set lives for " + data.getUsername() + " to " + lives);

                                ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayer(data.getUuid());
                                if (targetPlayer != null) {
                                    if (lives > 0 && targetPlayer.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
                                        targetPlayer.setGameMode(GameType.SURVIVAL);
                                        ServerLifecycleListener.setGhostModeAttributes(targetPlayer, false);
                                        GhostModeEvents.updateGhostStatus(data.getUuid(), false);
                                    } else if (lives == 0) {
                                        targetPlayer.setGameMode(GameType.ADVENTURE);
                                        ServerLifecycleListener.setGhostModeAttributes(targetPlayer, true);
                                        GhostModeEvents.updateGhostStatus(data.getUuid(), true);
                                    }
                                }
                                source.sendSystemMessage(MessageUtil.get("admin-setlives-success", PLAYER, data.getUsername(), LIVES, lives));
                            });
                        });
                        return 1;
                    })
                )
            )
        );
    }

    private static void registerAdminLogCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("adminlog")
            .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_ADMIN))
            .executes(context -> {
                CommandSourceStack source = context.getSource();

                CompletableFuture.runAsync(() -> {
                    File logFile = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve(SSoggySoulsMod.MODID).resolve(AdminLogger.LOG_FILE_NAME).toFile();
                    org.ssoggy.ssoggysouls.command.action.AdminLogAction.AdminLogResult result =
                        org.ssoggy.ssoggysouls.command.action.AdminLogAction.execute(logFile, 15);

                    source.getServer().execute(() -> {
                        switch (result.type) {
                            case FILE_NOT_FOUND -> source.sendFailure(Component.literal("No admin logs found."));
                            case READ_ERROR -> {
                                SSoggySoulsMod.LOGGER.error("Error reading admin log");
                                source.sendFailure(MessageUtil.get("admin-log-read-error"));
                            }
                            case SUCCESS -> {
                                source.sendSystemMessage(Component.literal("--- Recent Admin Logs ---").withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD));
                                if (source.isPlayer()) {
                                    for (String line : result.lines) {
                                        source.sendSystemMessage(Component.literal(line).withStyle(s ->
                                            s.withColor(net.minecraft.ChatFormatting.GRAY)
                                             .withClickEvent(new net.minecraft.network.chat.ClickEvent.CopyToClipboard(line))
                                             .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(Component.literal("Click to copy log entry").withStyle(net.minecraft.ChatFormatting.GRAY)))
                                        ));
                                    }
                                } else {
                                    for (String line : result.lines) {
                                        source.sendSystemMessage(Component.literal(line).withStyle(net.minecraft.ChatFormatting.GRAY));
                                    }
                                }
                            }
                        }
                    });
                });
                return 1;
            })
        );
    }
}
