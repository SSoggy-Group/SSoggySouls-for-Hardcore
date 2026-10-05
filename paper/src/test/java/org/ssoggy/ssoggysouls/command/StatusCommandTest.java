package org.ssoggy.ssoggysouls.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.CommandUtil;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.TabCompleteUtil;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatusCommandTest {

    @Mock
    private SSoggySouls plugin;
    @Mock
    private DatabaseManager db;
    @Mock
    private Command command;
    @Mock
    private BukkitScheduler scheduler;

    private MockedStatic<Bukkit> bukkitMock;
    private MockedStatic<CommandUtil> commandUtilMock;
    private MockedStatic<TabCompleteUtil> tabCompleteUtilMock;

    private StatusCommand cmd;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        when(plugin.getDatabaseManager()).thenReturn(db);
        when(plugin.getGracePeriodMillis()).thenReturn(60000L);
        cmd = new StatusCommand(plugin);

        bukkitMock = Mockito.mockStatic(Bukkit.class);
        bukkitMock.when(Bukkit::getScheduler).thenReturn(scheduler);

        commandUtilMock = Mockito.mockStatic(CommandUtil.class);
        tabCompleteUtilMock = Mockito.mockStatic(TabCompleteUtil.class);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (bukkitMock != null) {
            bukkitMock.close();
        }
        if (commandUtilMock != null) {
            commandUtilMock.close();
        }
        if (tabCompleteUtilMock != null) {
            tabCompleteUtilMock.close();
        }
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void testNullSender() {
        assertFalse(cmd.onCommand(null, command, "pstatus", new String[0]));
    }

    @Test
    void testConsoleNoArgsUsage() {
        CommandSender sender = mock(CommandSender.class);
        boolean result = cmd.onCommand(sender, command, "pstatus", new String[0]);
        assertFalse(result);
        commandUtilMock.verify(() -> CommandUtil.sendInteractiveUsage(sender, "&cUsage: /pstatus <player>", "/pstatus "));
    }

    @Test
    void testPlayerNoArgsSelfCheck() {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("SelfPlayer");

        PlayerData data = mock(PlayerData.class);
        when(data.getUsername()).thenReturn("SelfPlayer");
        when(data.isDead()).thenReturn(false);
        when(data.isInGracePeriod(60000L)).thenReturn(false);
        when(data.getLives()).thenReturn(3);

        when(db.getPlayerByName("SelfPlayer")).thenReturn(data);

        boolean result = cmd.onCommand(player, command, "pstatus", new String[0]);
        assertTrue(result);
        verify(player).sendMessage(MessageUtil.get("status-alive", "player", "SelfPlayer", "lives", 3));
    }

    @Test
    void testTargetNotFound() {
        CommandSender sender = mock(CommandSender.class);
        when(db.getPlayerByName("NonExistent")).thenReturn(null);

        boolean result = cmd.onCommand(sender, command, "pstatus", new String[]{"NonExistent"});
        assertTrue(result);
        verify(sender).sendMessage(MessageUtil.get("revive-player-not-found", "player", "NonExistent"));
    }

    @Test
    void testTargetDead() {
        CommandSender sender = mock(CommandSender.class);
        PlayerData data = mock(PlayerData.class);
        when(data.getUsername()).thenReturn("DeadPlayer");
        when(data.isDead()).thenReturn(true);
        when(db.getPlayerByName("DeadPlayer")).thenReturn(data);

        boolean result = cmd.onCommand(sender, command, "pstatus", new String[]{"DeadPlayer"});
        assertTrue(result);
        verify(sender).sendMessage(MessageUtil.get("status-dead", "player", "DeadPlayer"));
    }

    @Test
    void testTargetGracePeriod() {
        CommandSender sender = mock(CommandSender.class);
        PlayerData data = mock(PlayerData.class);
        when(data.getUsername()).thenReturn("GracePlayer");
        when(data.isDead()).thenReturn(false);
        when(data.isInGracePeriod(60000L)).thenReturn(true);
        when(data.getLives()).thenReturn(1);
        when(data.getGraceTimeRemaining(60000L)).thenReturn("45s");
        when(db.getPlayerByName("GracePlayer")).thenReturn(data);

        boolean result = cmd.onCommand(sender, command, "pstatus", new String[]{"GracePlayer"});
        assertTrue(result);
        verify(sender).sendMessage(MessageUtil.get("status-grace", "player", "GracePlayer", "lives", 1, "time_remaining", "45s"));
    }

    @Test
    void testTargetAlive() {
        CommandSender sender = mock(CommandSender.class);
        PlayerData data = mock(PlayerData.class);
        when(data.getUsername()).thenReturn("AlivePlayer");
        when(data.isDead()).thenReturn(false);
        when(data.isInGracePeriod(60000L)).thenReturn(false);
        when(data.getLives()).thenReturn(5);
        when(db.getPlayerByName("AlivePlayer")).thenReturn(data);

        boolean result = cmd.onCommand(sender, command, "pstatus", new String[]{"AlivePlayer"});
        assertTrue(result);
        verify(sender).sendMessage(MessageUtil.get("status-alive", "player", "AlivePlayer", "lives", 5));
    }

    @Test
    void testTabCompleteSingleArg() {
        tabCompleteUtilMock.when(() -> TabCompleteUtil.getOnlinePlayerNames("pl"))
                .thenReturn(List.of("player1", "player2"));

        List<String> completions = cmd.onTabComplete(mock(CommandSender.class), command, "pstatus", new String[]{"pl"});
        assertEquals(List.of("player1", "player2"), completions);
    }

    @Test
    void testTabCompleteMultipleArgs() {
        List<String> completions = cmd.onTabComplete(mock(CommandSender.class), command, "pstatus", new String[]{"player1", "extra"});
        assertEquals(Collections.emptyList(), completions);
    }
}
