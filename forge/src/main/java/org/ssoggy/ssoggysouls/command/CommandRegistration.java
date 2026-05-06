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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.AdminLogger;
import org.ssoggy.ssoggysouls.util.MessageUtil;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = SSoggySoulsMod.MODID)
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
        registerObituariesCommand(dispatcher);
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
                    if (data != null) {
                        source.getServer().execute(() ->
                            source.sendSuccess(() -> MessageUtil.get("status-self", LIVES, data.getLives()), false));
                    }
                });
                return 1;
            })
            .then(Commands.argument(PLAYER, StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        context.getSource().getServer().getPlayerList().getPlayers().stream().map(p -> p.getScoreboardName()), builder))
                .executes(context -> {
                    String targetName = StringArgumentType.getString(context, PLAYER);
                    CommandSourceStack source = context.getSource();

                    CompletableFuture.runAsync(() -> {
                        PlayerData data = db.getPlayerByName(targetName);
                        if (data == null) {
                            source.getServer().execute(() ->
                                source.sendFailure(MessageUtil.get("status-not-found", PLAYER, targetName)));
                            return;
                        }

                        if (data.isDead()) {
                            source.getServer().execute(() ->
                                source.sendSuccess(() -> MessageUtil.get("status-other-dead", PLAYER, data.getUsername()), false));
                        } else {
                            source.getServer().execute(() ->
                                source.sendSuccess(() -> MessageUtil.get("status-other-alive",
                                        PLAYER, data.getUsername(),
                                        LIVES, data.getLives()), false));
                        }
                    });
                    return 1;
                })
            )
        );
    }

    private static void registerReviveCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("revive")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument(PLAYER, StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        context.getSource().getServer().getPlayerList().getPlayers().stream().map(p -> p.getScoreboardName()), builder))
                .executes(context -> {
                    String targetName = StringArgumentType.getString(context, PLAYER);
                    CommandSourceStack source = context.getSource();

                    CompletableFuture.runAsync(() -> {
                        PlayerData targetData = db.getPlayerByName(targetName);
                        if (targetData == null) {
                            source.getServer().execute(() ->
                                source.sendFailure(MessageUtil.get("revive-not-found", PLAYER, targetName)));
                            return;
                        }

                        if (!targetData.isDead()) {
                            source.getServer().execute(() ->
                                source.sendFailure(MessageUtil.get("revive-already-alive", PLAYER, targetData.getUsername())));
                            return;
                        }

                        int defaultLives = org.ssoggy.ssoggysouls.util.ConfigManager.getConfig().getDefaultLives();
                        boolean success = db.revivePlayer(targetData.getUuid(), defaultLives);
                        if (success) {
                            source.getServer().execute(() -> {
                                source.sendSuccess(() -> MessageUtil.get("admin-revive-success", PLAYER, targetData.getUsername()), true);
                                AdminLogger.log(source.getTextName(), "Revived " + targetData.getUsername());

                                ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayer(targetData.getUuid());
                                if (targetPlayer != null) {
                                    targetPlayer.setGameMode(GameType.SURVIVAL);
                                    targetPlayer.sendSystemMessage(MessageUtil.get("revive-success"));
                                }
                            });
                        }
                    });
                    return 1;
                })
            )
        );
    }

    private static void registerSetLivesCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("psetlives")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument(PLAYER, StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        context.getSource().getServer().getPlayerList().getPlayers().stream().map(p -> p.getScoreboardName()), builder))
                .then(Commands.argument(LIVES, IntegerArgumentType.integer(0))
                    .executes(context -> {
                        String targetName = StringArgumentType.getString(context, PLAYER);
                        int lives = IntegerArgumentType.getInteger(context, LIVES);
                        CommandSourceStack source = context.getSource();

                        CompletableFuture.runAsync(() -> {
                            PlayerData data = db.getPlayerByName(targetName);
                            if (data == null) {
                                source.getServer().execute(() ->
                                    source.sendFailure(MessageUtil.get("status-not-found", PLAYER, targetName)));
                                return;
                            }
                            db.setLives(data.getUuid(), lives);
                            source.getServer().execute(() -> {
                                source.sendSuccess(() -> MessageUtil.get("admin-setlives-success",
                                        PLAYER, data.getUsername(), LIVES, lives), true);
                                AdminLogger.log(source.getTextName(), "Set lives for " + data.getUsername() + " to " + lives);
                            });
                        });
                        return 1;
                    })
                )
            )
        );
    }

    private static void registerObituariesCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("obituaries")
            .executes(context -> {
                CommandSourceStack source = context.getSource();

                CompletableFuture.runAsync(() -> {
                    List<PlayerData> deadPlayers = db.getDeadPlayers();
                    source.getServer().execute(() -> {
                        if (deadPlayers.isEmpty()) {
                            source.sendSystemMessage(Component.literal("Nobody has died recently. The server is peaceful.").withStyle(net.minecraft.ChatFormatting.GREEN));
                            return;
                        }

                        source.sendSystemMessage(Component.literal("--- Server Obituaries ---").withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD));
                        for (PlayerData dead : deadPlayers) {
                            String time = "Recently";
                            if (dead.getLastDeath() > 0) {
                                long days = (System.currentTimeMillis() - dead.getLastDeath()) / (1000 * 60 * 60 * 24);
                                time = days + " days ago";
                            }
                            source.sendSystemMessage(Component.literal("- " + dead.getUsername() + " (" + time + ")").withStyle(net.minecraft.ChatFormatting.GRAY));
                        }
                    });
                });

                return 1;
            })
        );
    }

    private static void registerAdminLogCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("adminlog")
            .requires(source -> source.hasPermission(3))
            .executes(context -> {
                CommandSourceStack source = context.getSource();

                CompletableFuture.runAsync(() -> {
                    File logFile = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve(SSoggySoulsMod.MODID).resolve("admin_abuse.log").toFile();
                    if (!logFile.exists()) {
                        source.sendFailure(Component.literal("No admin logs found."));
                        return;
                    }

                    try {
                        java.util.Deque<String> lines = readLastLines(logFile, 15);
                        source.getServer().execute(() -> {
                            source.sendSystemMessage(Component.literal("--- Recent Admin Logs ---").withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD));
                            for (String line : lines) {
                                source.sendSystemMessage(Component.literal(line).withStyle(net.minecraft.ChatFormatting.GRAY));
                            }
                        });
                    } catch (java.io.IOException e) {
                        source.getServer().execute(() -> source.sendFailure(Component.literal("Error reading admin log: " + e.getMessage())));
                    }
                });

                return 1;
            })
        );
    }

    private static java.util.Deque<String> readLastLines(File file, int maxLines) throws java.io.IOException {
        if (maxLines <= 0) return new java.util.ArrayDeque<>();
        java.util.Deque<String> lastLines = new java.util.ArrayDeque<>(maxLines);
        try (java.util.stream.Stream<String> lines = Files.lines(file.toPath())) {
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
