package org.ssoggy.ssoggysouls.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
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
            registerSetLivesCommand(dispatcher, plugin, db);
            registerAdminLogCommand(dispatcher, plugin);
            
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
            .then(CommandManager.argument(PLAYER, StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(
                        context.getSource().getServer().getPlayerManager().getPlayerList().stream().map(p -> p.getName().getString()), builder))
                .executes(context -> {
                    String targetName = StringArgumentType.getString(context, PLAYER);
                    ServerCommandSource source = context.getSource();

                    CompletableFuture.runAsync(() -> {
                        PlayerData data = db.getPlayerByName(targetName);
                        if (data == null) {
                            source.sendError(MessageUtil.get("status-not-found", PLAYER, targetName));
                            return;
                        }

                        if (data.isDead()) {
                            source.sendFeedback(() -> MessageUtil.get("status-other-dead", PLAYER, data.getUsername()), false);
                        } else {
                            source.sendFeedback(() -> MessageUtil.get("status-other-alive", 
                                    PLAYER, data.getUsername(), 
                                    LIVES, data.getLives()), false);
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
                            source.sendError(MessageUtil.get("revive-not-found", PLAYER, targetName));
                            return;
                        }

                        if (!targetData.isDead()) {
                            source.sendError(MessageUtil.get("revive-already-alive", PLAYER, targetData.getUsername()));
                            return;
                        }

                        boolean success = db.revivePlayer(targetData.getUuid(), plugin.getDefaultLives());
                        if (success) {
                            source.sendFeedback(() -> MessageUtil.get("admin-revive-success", "player", targetData.getUsername()), true);
                            AdminLogger.log(source.getName(), "Revived " + targetData.getUsername());
                            
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
                                source.sendError(MessageUtil.get("status-not-found", PLAYER, targetName));
                                return;
                            }
                            db.setLives(data.getUuid(), lives);
                            source.sendFeedback(() -> MessageUtil.getNoPrefix("admin-setlives-success", 
                                    PLAYER, data.getUsername(), LIVES, lives), true);
                            AdminLogger.log(source.getName(), "Set lives for " + data.getUsername() + " to " + lives);
                        });
                        return 1;
                    })
                )
            )
        );
    }

    private static void registerObituariesCommand(CommandDispatcher<ServerCommandSource> dispatcher, DatabaseManager db) {
        dispatcher.register(CommandManager.literal("obituaries")
            .executes(context -> {
                ServerCommandSource source = context.getSource();
                
                CompletableFuture.runAsync(() -> {
                    java.util.List<PlayerData> deadPlayers = db.getDeadPlayers();
                    source.getServer().execute(() -> {
                        if (deadPlayers.isEmpty()) {
                            source.sendMessage(net.minecraft.text.Text.literal("Nobody has died recently. The server is peaceful.").styled(s -> s.withColor(net.minecraft.util.Formatting.GREEN)));
                            return;
                        }
                        
                        source.sendMessage(net.minecraft.text.Text.literal("--- Server Obituaries ---").styled(s -> s.withColor(net.minecraft.util.Formatting.RED).withBold(true)));
                        for (PlayerData dead : deadPlayers) {
                            String time = "Recently";
                            if (dead.getLastDeath() > 0) {
                                long days = (System.currentTimeMillis() - dead.getLastDeath()) / (1000 * 60 * 60 * 24);
                                time = days + " days ago";
                            }
                            source.sendMessage(net.minecraft.text.Text.literal("- " + dead.getUsername() + " (" + time + ")").styled(s -> s.withColor(net.minecraft.util.Formatting.GRAY)));
                        }
                    });
                });
                
                return 1;
            })
        );
    }

    private static void registerAdminLogCommand(CommandDispatcher<ServerCommandSource> dispatcher, SSoggySoulsMod plugin) {
        dispatcher.register(CommandManager.literal("adminlog")
            .requires(source -> source.hasPermissionLevel(3))
            .executes(context -> {
                ServerCommandSource source = context.getSource();
                
                CompletableFuture.runAsync(() -> {
                    java.io.File logFile = new java.io.File(plugin.getDataFolder().toFile(), "admin_abuse.log");
                    if (!logFile.exists()) {
                        source.sendError(net.minecraft.text.Text.literal("No admin logs found."));
                        return;
                    }
                    
                    try {
                        java.util.List<String> lines = java.nio.file.Files.readAllLines(logFile.toPath());
                        source.sendMessage(net.minecraft.text.Text.literal("--- Recent Admin Logs ---").styled(s -> s.withColor(net.minecraft.util.Formatting.RED).withBold(true)));
                        int start = Math.max(0, lines.size() - 15);
                        for (int i = start; i < lines.size(); i++) {
                            source.sendMessage(net.minecraft.text.Text.literal(lines.get(i)).styled(s -> s.withColor(net.minecraft.util.Formatting.GRAY)));
                        }
                    } catch (Exception e) {
                        source.sendError(net.minecraft.text.Text.literal("Error reading admin log: " + e.getMessage()));
                    }
                });
                
                return 1;
            })
        );
    }
}
