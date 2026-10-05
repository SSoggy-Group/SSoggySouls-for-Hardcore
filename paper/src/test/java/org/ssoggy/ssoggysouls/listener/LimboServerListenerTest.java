package org.ssoggy.ssoggysouls.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.task.LimboCheckTask;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LimboServerListenerTest {

    private MockedStatic<Bukkit> mockedBukkit;
    private SSoggySouls plugin;
    private LimboCheckTask checkTask;
    private DatabaseManager databaseManager;
    private BukkitScheduler scheduler;
    private LimboServerListener listener;

    @BeforeEach
    void setUp() {
        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(mock(org.bukkit.inventory.ItemFactory.class));

        scheduler = mock(BukkitScheduler.class);
        when(scheduler.runTaskAsynchronously(any(Plugin.class), any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        });
        when(scheduler.runTask(any(Plugin.class), any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        });
        mockedBukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

        plugin = mock(SSoggySouls.class);
        databaseManager = mock(DatabaseManager.class);
        when(plugin.getDatabaseManager()).thenReturn(databaseManager);

        checkTask = mock(LimboCheckTask.class);
        listener = new LimboServerListener(plugin, checkTask);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    @Test
    void testCommandPreprocessVisitorAllowed() {
        Player player = mock(Player.class);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(player.hasPermission(anyString())).thenReturn(false);

        PlayerCommandPreprocessEvent event = mock(PlayerCommandPreprocessEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getMessage()).thenReturn("/spawn");

        listener.onCommandPreprocess(event);

        verify(event, never()).setCancelled(true);
    }

    @Test
    void testCommandPreprocessDeadPlayerCancelled() {
        Player player = mock(Player.class);
        when(player.getGameMode()).thenReturn(GameMode.ADVENTURE);
        when(player.hasPermission(anyString())).thenReturn(false);

        PlayerCommandPreprocessEvent event = mock(PlayerCommandPreprocessEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getMessage()).thenReturn("/spawn");

        listener.onCommandPreprocess(event);

        verify(event).setCancelled(true);
    }

    @Test
    void testCommandPreprocessWhitelistedCommandAllowed() {
        Player player = mock(Player.class);
        when(player.getGameMode()).thenReturn(GameMode.ADVENTURE);
        when(player.hasPermission(anyString())).thenReturn(false);

        PlayerCommandPreprocessEvent event = mock(PlayerCommandPreprocessEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getMessage()).thenReturn("/msg hello");

        listener.onCommandPreprocess(event);

        verify(event, never()).setCancelled(true);
    }

    @Test
    void testOnPortalBypassPermission() {
        Player player = mock(Player.class);
        when(player.hasPermission("ssoggysouls.bypass")).thenReturn(true);

        PlayerPortalEvent event = mock(PlayerPortalEvent.class);
        when(event.getPlayer()).thenReturn(player);

        listener.onPortal(event);

        verify(event, never()).setCancelled(true);
        verify(scheduler, never()).runTaskAsynchronously(any(), any(Runnable.class));
    }

    @Test
    void testOnPortalDeadPlayerCancelledAndTeleportedFrom() {
        UUID uuid = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.hasPermission("ssoggysouls.bypass")).thenReturn(false);
        when(player.getGameMode()).thenReturn(GameMode.ADVENTURE);
        when(player.isOnline()).thenReturn(true);

        Location from = mock(Location.class);
        when(from.clone()).thenReturn(from);
        Location to = mock(Location.class);
        when(to.clone()).thenReturn(to);

        PlayerPortalEvent event = mock(PlayerPortalEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getFrom()).thenReturn(from);
        when(event.getTo()).thenReturn(to);

        when(databaseManager.isPlayerDead(uuid)).thenReturn(true);

        listener.onPortal(event);

        verify(event).setCancelled(true);
        verify(player).teleport(from);
        verify(player).sendMessage(anyString());
    }

    @Test
    void testOnPortalAlivePlayerTeleportedTo() {
        UUID uuid = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.hasPermission("ssoggysouls.bypass")).thenReturn(false);
        when(player.getGameMode()).thenReturn(GameMode.ADVENTURE);
        when(player.isOnline()).thenReturn(true);

        Location from = mock(Location.class);
        when(from.clone()).thenReturn(from);
        Location to = mock(Location.class);
        when(to.clone()).thenReturn(to);

        PlayerPortalEvent event = mock(PlayerPortalEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getFrom()).thenReturn(from);
        when(event.getTo()).thenReturn(to);

        when(databaseManager.isPlayerDead(uuid)).thenReturn(false);

        listener.onPortal(event);

        verify(event).setCancelled(true);
        verify(player).teleport(to);
        verify(player, never()).sendMessage(anyString());
    }

    @Test
    void testOnPortalOfflinePlayerNoAction() {
        UUID uuid = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.hasPermission("ssoggysouls.bypass")).thenReturn(false);
        when(player.getGameMode()).thenReturn(GameMode.ADVENTURE);
        when(player.isOnline()).thenReturn(false);

        Location from = mock(Location.class);
        when(from.clone()).thenReturn(from);

        PlayerPortalEvent event = mock(PlayerPortalEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getFrom()).thenReturn(from);

        when(databaseManager.isPlayerDead(uuid)).thenReturn(false);

        listener.onPortal(event);

        verify(player, never()).teleport(any(Location.class));
    }
}
