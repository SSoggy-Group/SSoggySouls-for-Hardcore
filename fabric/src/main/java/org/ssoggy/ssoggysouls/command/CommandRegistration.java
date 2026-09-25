package org.ssoggy.ssoggysouls.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
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

import java.io.File;
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

    private static void registerStatusCommand(CommandDispatcher<CommandSourceStack> dispatcher, DatabaseManager db) {
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

    private static void registerReviveCommand(CommandDispatcher<CommandSourceStack> dispatcher, SSoggySoulsMod plugin, DatabaseManager db) {
        dispatcher.register(Commands.literal("revive")
            .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
            .executes(context -> {
                context.getSource().sendFailure(MessageUtil.get("usage-revive").copy()
                    .withStyle(s -> s.withColor(net.minecraft.ChatFormatting.RED)
                        .withClickEvent(new ClickEvent.SuggestCommand("/revive "))
                        .withHoverEvent(new HoverEvent.ShowText(MessageUtil.get("click-to-autofill").copy().withStyle(net.minecraft.ChatFormatting.GRAY)))));
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

                    CompletableFuture.runAsync(() -> executeRevive(targetName, source, db));
                    return 1;
                })
            )
        );
    }

    private static void executeRevive(String targetName, CommandSourceStack source, DatabaseManager db) {
        PlayerData data = db.getPlayerByName(targetName);
        if (data == null) {
            source.sendFailure(MessageUtil.get("admin-player-not-found", PLAYER, targetName));
            return;
        }

        if (!data.isDead()) {
            source.sendFailure(MessageUtil.get("admin-player-not-dead", PLAYER, data.getUsername()));
            return;
        }

        int onReviveLives = org.ssoggy.ssoggysouls.util.ConfigManager.getConfig().getOnReviveLives();
        boolean success = db.revivePlayer(data.getUuid(), onReviveLives);
        if (!success) {
            source.sendFailure(MessageUtil.get("admin-revive-failed", PLAYER, data.getUsername()));
            return;
        }

        String adminName = source.isPlayer() ? source.getPlayer().getScoreboardName() : "CONSOLE";
        AdminLogger.log(adminName, "Revived " + data.getUsername());

        source.getServer().execute(() -> {
            GhostModeEvents.updateGhostStatus(data.getUuid(), false);
            DlcDeaths.clearDeath(data.getUuid());

            GhostState ghostState = GhostState.getServerState(source.getServer());
            ghostState.removeDeathLocation(data.getUuid());
            ghostState.removeDeathHolder(data.getUuid());
            ghostState.setDirty();

            ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayer(data.getUuid());
            if (targetPlayer != null) {
                targetPlayer.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                MainServerListener.setGhostModeAttributes(targetPlayer, false);
                targetPlayer.sendSystemMessage(MessageUtil.get("revive-success"));
            }

            source.sendSystemMessage(MessageUtil.get("admin-revive-success", PLAYER, data.getUsername(), LIVES, onReviveLives));
        });
    }

    private static void registerSetLivesCommand(CommandDispatcher<CommandSourceStack> dispatcher, DatabaseManager db) {
        dispatcher.register(Commands.literal("psetlives")
            .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
            .executes(context -> {
                context.getSource().sendFailure(MessageUtil.get("usage-psetlives").copy()
                    .withStyle(s -> s.withColor(net.minecraft.ChatFormatting.RED)
                        .withClickEvent(new ClickEvent.SuggestCommand("/psetlives "))
                        .withHoverEvent(new HoverEvent.ShowText(MessageUtil.get("click-to-autofill").copy().withStyle(net.minecraft.ChatFormatting.GRAY)))));
                return 0;
            })
            .then(Commands.argument(PLAYER, StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        context.getSource().getServer().getPlayerNames(), builder))
                .executes(context -> {
                    String targetName = StringArgumentType.getString(context, PLAYER);
                    context.getSource().sendFailure(MessageUtil.get("usage-psetlives-player", PLAYER, targetName).copy()
                        .withStyle(s -> s.withColor(net.minecraft.ChatFormatting.RED)
                            .withClickEvent(new ClickEvent.SuggestCommand("/psetlives " + targetName + " "))
                            .withHoverEvent(new HoverEvent.ShowText(MessageUtil.get("click-to-autofill").copy().withStyle(net.minecraft.ChatFormatting.GRAY)))));
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

                            int oldLives = data.getLives();
                            data.setLives(lives);
                            db.savePlayer(data);

                            String adminName = source.isPlayer() ? source.getPlayer().getScoreboardName() : "CONSOLE";
                            AdminLogger.log(adminName, "Set lives for " + data.getUsername() + " to " + lives);

                            source.getServer().execute(() -> {
                                ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayer(data.getUuid());
                                if (targetPlayer != null) {
                                    if (lives > 0 && targetPlayer.gameMode.getGameModeForPlayer() == net.minecraft.world.level.GameType.ADVENTURE) {
                                        targetPlayer.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                                        MainServerListener.setGhostModeAttributes(targetPlayer, false);
                                        GhostModeEvents.updateGhostStatus(data.getUuid(), false);
                                    } else if (lives == 0) {
                                        targetPlayer.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);
                                        MainServerListener.setGhostModeAttributes(targetPlayer, true);
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

    private static void registerAdminLogCommand(CommandDispatcher<CommandSourceStack> dispatcher, SSoggySoulsMod plugin) {
        dispatcher.register(Commands.literal("adminlog")
            .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_ADMIN))
            .executes(context -> {
                CommandSourceStack source = context.getSource();

                CompletableFuture.runAsync(() -> {
                    File logFile = plugin.getDataFolder().toPath().resolve(AdminLogger.LOG_FILE_NAME).toFile();
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
                                             .withClickEvent(new ClickEvent.CopyToClipboard(line))
                                             .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to copy log entry").withStyle(net.minecraft.ChatFormatting.GRAY)))
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
