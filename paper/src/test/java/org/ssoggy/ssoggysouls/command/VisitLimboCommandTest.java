package org.ssoggy.ssoggysouls.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.ServerTransferUtil;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VisitLimboCommandTest {

    @Mock
    private SSoggySouls plugin;
    @Mock
    private DatabaseManager db;
    @Mock
    private Command command;
    @Mock
    private BukkitScheduler scheduler;

    private MockedStatic<Bukkit> bukkitMock;
    private MockedStatic<ServerTransferUtil> serverTransferUtilMock;

    private VisitLimboCommand cmd;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        when(plugin.getDatabaseManager()).thenReturn(db);
        cmd = new VisitLimboCommand(plugin);

        bukkitMock = Mockito.mockStatic(Bukkit.class);
        bukkitMock.when(Bukkit::getScheduler).thenReturn(scheduler);

        serverTransferUtilMock = Mockito.mockStatic(ServerTransferUtil.class);

        // Run async scheduler task synchronously
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        // Run sync scheduler task synchronously
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(scheduler).runTask(eq(plugin), any(Runnable.class));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (bukkitMock != null) {
            bukkitMock.close();
        }
        if (serverTransferUtilMock != null) {
            serverTransferUtilMock.close();
        }
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void testNullSender() {
        boolean result = cmd.onCommand(null, command, "visitlimbo", new String[0]);
        assertTrue(result);
    }

    @Test
    void testConsoleSender() {
        CommandSender sender = mock(CommandSender.class);
        boolean result = cmd.onCommand(sender, command, "visitlimbo", new String[0]);
        assertTrue(result);
        verify(sender).sendMessage(MessageUtil.get("command-only-players"));
    }

    @Test
    void testPlayerOfflineDuringSyncCallback() {
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.isOnline()).thenReturn(false);

        boolean result = cmd.onCommand(player, command, "visitlimbo", new String[0]);
        assertTrue(result);
        verify(player, never()).sendMessage(any(String.class));
    }

    @Test
    void testPlayerAlreadyDead() {
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.isOnline()).thenReturn(true);
        when(db.isPlayerDead(uuid)).thenReturn(true);

        boolean result = cmd.onCommand(player, command, "visitlimbo", new String[0]);
        assertTrue(result);
        verify(player).sendMessage(MessageUtil.get("limbo-visit-already-dead"));
        verify(scheduler, never()).runTaskLater(eq(plugin), any(Runnable.class), eq(20L));
    }

    @Test
    void testPlayerAliveSuccessfullyTransfers() {
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.isOnline()).thenReturn(true);
        when(db.isPlayerDead(uuid)).thenReturn(false);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        boolean result = cmd.onCommand(player, command, "visitlimbo", new String[0]);
        assertTrue(result);
        verify(player).sendMessage(MessageUtil.get("limbo-visit-going"));
        verify(scheduler).runTaskLater(eq(plugin), runnableCaptor.capture(), eq(20L));

        // Execute delayed task while player is online
        runnableCaptor.getValue().run();
        serverTransferUtilMock.verify(() -> ServerTransferUtil.sendToLimbo(player));
    }

    @Test
    void testPlayerAliveGoesOfflineBeforeTransfer() {
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.isOnline()).thenReturn(true);
        when(db.isPlayerDead(uuid)).thenReturn(false);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        cmd.onCommand(player, command, "visitlimbo", new String[0]);
        verify(scheduler).runTaskLater(eq(plugin), runnableCaptor.capture(), eq(20L));

        // Player goes offline
        when(player.isOnline()).thenReturn(false);
        runnableCaptor.getValue().run();
        serverTransferUtilMock.verify(() -> ServerTransferUtil.sendToLimbo(player), never());
    }
}
