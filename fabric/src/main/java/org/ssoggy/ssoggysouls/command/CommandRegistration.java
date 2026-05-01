package org.ssoggy.ssoggysouls.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.MessageUtil;

import java.util.concurrent.CompletableFuture;

public class CommandRegistration {

    public static void register(SSoggySoulsMod plugin, DatabaseManager db) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerStatusCommand(dispatcher, db);
            registerReviveCommand(dispatcher, plugin, db);
            registerSetLivesCommand(dispatcher, plugin, db);
            
            // Phase 5 DLC Commands
            registerObituariesCommand(dispatcher, db);
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
                        source.sendFeedback(() -> MessageUtil.get("status-self", "lives", data.getLives()), false);
                    }
                });
                return 1;
            })
            .then(CommandManager.argument("player", StringArgumentType.string())
                .executes(context -> {
                    String targetName = StringArgumentType.getString(context, "player");
                    ServerCommandSource source = context.getSource();

                    CompletableFuture.runAsync(() -> {
                        PlayerData data = db.getPlayerByName(targetName);
                        if (data == null) {
                            source.sendError(MessageUtil.get("status-not-found", "player", targetName));
                            return;
                        }

                        if (data.isDead()) {
                            source.sendFeedback(() -> MessageUtil.get("status-other-dead", "player", data.getUsername()), false);
                        } else {
                            source.sendFeedback(() -> MessageUtil.get("status-other-alive", 
                                    "player", data.getUsername(), 
                                    "lives", data.getLives()), false);
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
            .then(CommandManager.argument("player", StringArgumentType.string())
                .executes(context -> {
                    String targetName = StringArgumentType.getString(context, "player");
                    ServerCommandSource source = context.getSource();

                    CompletableFuture.runAsync(() -> {
                        PlayerData targetData = db.getPlayerByName(targetName);
                        if (targetData == null) {
                            source.sendError(MessageUtil.get("revive-not-found", "player", targetName));
                            return;
                        }

                        if (!targetData.isDead()) {
                            source.sendError(MessageUtil.get("revive-already-alive", "player", targetData.getUsername()));
                            return;
                        }

                        boolean success = db.revivePlayer(targetData.getUuid(), plugin.getDefaultLives());
                        if (success) {
                            source.sendFeedback(() -> MessageUtil.get("admin-revive-success", "player", targetData.getUsername()), true);
                            
                            // Restore game mode if player is online
                            ServerPlayerEntity targetPlayer = source.getServer().getPlayerManager().getPlayer(targetData.getUuid());
                            if (targetPlayer != null) {
                                source.getServer().execute(() -> {
                                    targetPlayer.changeGameMode(net.minecraft.world.GameMode.SURVIVAL);
                                    targetPlayer.sendMessage(MessageUtil.get("revive-success"), false);
                                });
                            }
                        }
                    });
                    return 1;
                })
            )
        );
    }

    private static void registerSetLivesCommand(CommandDispatcher<ServerCommandSource> dispatcher, SSoggySoulsMod plugin, DatabaseManager db) {
        dispatcher.register(CommandManager.literal("psetlives")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("player", StringArgumentType.string())
                .then(CommandManager.argument("lives", IntegerArgumentType.integer(0))
                    .executes(context -> {
                        String targetName = StringArgumentType.getString(context, "player");
                        int lives = IntegerArgumentType.getInteger(context, "lives");
                        ServerCommandSource source = context.getSource();

                        CompletableFuture.runAsync(() -> {
                            PlayerData data = db.getPlayerByName(targetName);
                            if (data == null) {
                                source.sendError(MessageUtil.get("status-not-found", "player", targetName));
                                return;
                            }
                            
                            db.setLives(data.getUuid(), lives);
                            source.sendFeedback(() -> MessageUtil.getNoPrefix("admin-setlives-success", 
                                    "player", data.getUsername(), "lives", lives), true);
                        });
                        return 1;
                    })
                )
            )
        );
    }
}
