package org.ssoggy.ssoggysouls.util;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStorage;
import org.ssoggy.ssoggysouls.model.PlayerData;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PlayerRevivalUtilTest {

    private MockedStatic<Bukkit> mockedBukkit;
    private SSoggySouls plugin;
    private BukkitScheduler scheduler;
    private Logger logger;
    private PlayerData playerData;
    private UUID uuid;

    @BeforeEach
    void setUp() {
        mockedBukkit = mockStatic(Bukkit.class);
        plugin = mock(SSoggySouls.class);
        scheduler = mock(BukkitScheduler.class);
        logger = mock(Logger.class);

        uuid = UUID.randomUUID();
        playerData = mock(PlayerData.class);
        when(playerData.getUuid()).thenReturn(uuid);
        when(playerData.getUsername()).thenReturn("TestUser");

        when(plugin.getLogger()).thenReturn(logger);
        mockedBukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

        // Immediately execute scheduled runTask calls
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(scheduler).runTask(eq(plugin), any(Runnable.class));

        // Immediately execute scheduled runTaskLater calls
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(scheduler).runTaskLater(eq(plugin), any(Runnable.class), anyLong());

        RPStatic.DEAD_LOCATIONS = new HashMap<>();
        RPStatic.DEAD_STORAGE = mock(RPStorage.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
        RPStatic.DEAD_LOCATIONS = null;
        RPStatic.DEAD_STORAGE = null;
    }

    @Test
    void testRestoreOnlineSpectator_nullOrOfflinePlayer() {
        mockedBukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(null);

        PlayerRevivalUtil.restoreOnlineSpectator(plugin, playerData);

        verify(RPStatic.DEAD_STORAGE).removeValue(uuid.toString(), "deathpos");
        verify(RPStatic.DEAD_STORAGE).removeValue(uuid.toString(), "deathtime");
        verify(RPStatic.DEAD_STORAGE).saveConfig();
    }

    @Test
    void testRestoreOnlineSpectator_survivalPlayer() {
        Player mockPlayer = mock(Player.class);
        when(mockPlayer.isOnline()).thenReturn(true);
        when(mockPlayer.getGameMode()).thenReturn(GameMode.SURVIVAL);
        mockedBukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(mockPlayer);

        PlayerRevivalUtil.restoreOnlineSpectator(plugin, playerData);

        verify(mockPlayer, never()).setGameMode(GameMode.SURVIVAL);
        verify(mockPlayer, never()).sendMessage(anyString());
    }

    @Test
    void testRestoreOnlineSpectator_spectatorPlayer() {
        Player mockPlayer = mock(Player.class);
        when(mockPlayer.isOnline()).thenReturn(true);
        when(mockPlayer.getGameMode()).thenReturn(GameMode.SPECTATOR);
        mockedBukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(mockPlayer);
        when(plugin.isLimboServer()).thenReturn(false);

        PlayerRevivalUtil.restoreOnlineSpectator(plugin, playerData);

        verify(mockPlayer).setGameMode(GameMode.SURVIVAL);
        verify(mockPlayer).sendMessage(anyString());
        verify(plugin, never()).getMainServerName();
    }

    @Test
    void testRestoreOnlineSpectator_limboServerTransfer() {
        Player mockPlayer = mock(Player.class);
        when(mockPlayer.isOnline()).thenReturn(true);
        when(mockPlayer.getGameMode()).thenReturn(GameMode.SPECTATOR);
        when(mockPlayer.getName()).thenReturn("TestUser");
        mockedBukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(mockPlayer);

        when(plugin.isLimboServer()).thenReturn(true);
        when(plugin.getMainServerName()).thenReturn("main");
        try (MockedStatic<SSoggySouls> mockedSSoggySouls = mockStatic(SSoggySouls.class)) {
            mockedSSoggySouls.when(SSoggySouls::getInstance).thenReturn(plugin);

            PlayerRevivalUtil.restoreOnlineSpectator(plugin, playerData);

            verify(mockPlayer).setGameMode(GameMode.SURVIVAL);
            verify(plugin).getMainServerName();
            verify(mockPlayer).sendPluginMessage(eq(plugin), eq("BungeeCord"), any());
        }
    }

    @Test
    void testClearDlcDeathState_handlesException() {
        doThrow(new RuntimeException("Storage error")).when(RPStatic.DEAD_STORAGE).saveConfig();

        PlayerRevivalUtil.restoreOnlineSpectator(plugin, playerData);

        verify(logger).warning("Failed to clear DLC death state for TestUser");
    }
}
