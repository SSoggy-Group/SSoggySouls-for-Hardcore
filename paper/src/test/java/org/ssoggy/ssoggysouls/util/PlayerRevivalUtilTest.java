package org.ssoggy.ssoggysouls.util;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.GAMEMODESENUM;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStatic;
import org.ssoggy.ssoggysouls.hrm.dlc.util.RPStorage;
import org.ssoggy.ssoggysouls.model.PlayerData;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @BeforeAll
    static void initGameModesEnum() throws Exception {
        File dir = new File("build/tmp/test-player-revival-init");
        dir.mkdirs();
        File f = new File(dir, "ghostmodeplayers.yml");
        if (!f.exists()) {
            Files.writeString(f.toPath(), "ghostmodeplayers:\n  \"00000000-0000-0000-0000-000000000000\": \"Player1\"\n");
        }

        JavaPlugin mockPlugin = mock(JavaPlugin.class);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("test"));
        when(mockPlugin.getDataFolder()).thenReturn(dir);
        RPStatic.CLIENT = mockPlugin;

        assertNotNull(GAMEMODESENUM.SURVIVAL);
    }

    @BeforeEach
    void setUp() {
        mockedBukkit = mockStatic(Bukkit.class);
        plugin = mock(SSoggySouls.class);
        scheduler = mock(BukkitScheduler.class);
        logger = mock(Logger.class);

        File testDir = new File("build/tmp/test-player-revival");
        testDir.mkdirs();
        when(plugin.getDataFolder()).thenReturn(testDir);
        when(plugin.getLogger()).thenReturn(logger);

        RPStatic.CLIENT = plugin;

        uuid = UUID.randomUUID();
        playerData = mock(PlayerData.class);
        when(playerData.getUuid()).thenReturn(uuid);
        when(playerData.getUsername()).thenReturn("TestUser");

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
        RPStatic.DEAD_HOLDERS = new HashMap<>();
        RPStatic.DEAD_STORAGE = mock(RPStorage.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
        RPStatic.CLIENT = null;
        RPStatic.DEAD_LOCATIONS = null;
        RPStatic.DEAD_HOLDERS = null;
        RPStatic.DEAD_STORAGE = null;
    }

    @Test
    void testRestoreOnlineSpectator_nullOrOfflinePlayer() {
        mockedBukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(null);
        UUID holderUuid = UUID.randomUUID();
        RPStatic.DEAD_HOLDERS.put(uuid, holderUuid);

        PlayerRevivalUtil.restoreOnlineSpectator(plugin, playerData);

        assertFalse(RPStatic.DEAD_HOLDERS.containsKey(uuid));
        verify(RPStatic.DEAD_STORAGE).removeValue(uuid.toString(), "deathpos");
        verify(RPStatic.DEAD_STORAGE).removeValue(uuid.toString(), "deathtime");
        verify(RPStatic.DEAD_STORAGE).removeValue(uuid.toString(), "deathholder");
        verify(RPStatic.DEAD_STORAGE).saveConfig();
    }

    @Test
    void testRestoreOnlineSpectator_nullDlcState() {
        RPStatic.DEAD_LOCATIONS = null;
        RPStatic.DEAD_STORAGE = null;
        mockedBukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(null);

        PlayerRevivalUtil.restoreOnlineSpectator(plugin, playerData);

        verify(scheduler).runTask(eq(plugin), any(Runnable.class));
    }

    @Test
    void testRestoreOnlineSpectator_survivalPlayer() {
        Player mockPlayer = mock(Player.class);
        when(mockPlayer.getUniqueId()).thenReturn(uuid);
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
        World mockWorld = mock(World.class);
        when(mockWorld.getViewDistance()).thenReturn(10);
        when(mockPlayer.getWorld()).thenReturn(mockWorld);
        when(mockPlayer.getUniqueId()).thenReturn(uuid);
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
    void testRestoreOnlineSpectator_clientNullSpectatorPlayer() {
        RPStatic.CLIENT = null;
        Player mockPlayer = mock(Player.class);
        when(mockPlayer.getUniqueId()).thenReturn(uuid);
        when(mockPlayer.isOnline()).thenReturn(true);
        when(mockPlayer.getGameMode()).thenReturn(GameMode.SPECTATOR);
        mockedBukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(mockPlayer);
        when(plugin.isLimboServer()).thenReturn(false);

        PlayerRevivalUtil.restoreOnlineSpectator(plugin, playerData);

        verify(mockPlayer).setGameMode(GameMode.SURVIVAL);
        verify(mockPlayer).sendMessage(anyString());
    }

    @Test
    void testRestoreOnlineSpectator_survivalPlayerInGhostMode() {
        Player mockPlayer = mock(Player.class);
        World mockWorld = mock(World.class);
        when(mockWorld.getViewDistance()).thenReturn(10);
        when(mockPlayer.getWorld()).thenReturn(mockWorld);
        when(mockPlayer.getUniqueId()).thenReturn(uuid);
        when(mockPlayer.isOnline()).thenReturn(true);
        when(mockPlayer.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(mockPlayer.getName()).thenReturn("GhostUser");
        mockedBukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(mockPlayer);
        when(plugin.isLimboServer()).thenReturn(false);

        try (MockedStatic<GAMEMODESENUM> mockedGm = mockStatic(GAMEMODESENUM.class)) {
            mockedGm.when(() -> GAMEMODESENUM.getPlayerGameMode(mockPlayer)).thenReturn(GAMEMODESENUM.GHOSTMODE);

            PlayerRevivalUtil.restoreOnlineSpectator(plugin, playerData);

            mockedGm.verify(() -> GAMEMODESENUM.setPlayerGameMode(mockPlayer, GAMEMODESENUM.SURVIVAL));
            verify(mockPlayer).sendMessage(anyString());
        }
    }

    @Test
    void testRestoreOnlineSpectator_limboServerTransfer() {
        Player mockPlayer = mock(Player.class);
        World mockWorld = mock(World.class);
        when(mockWorld.getViewDistance()).thenReturn(10);
        when(mockPlayer.getWorld()).thenReturn(mockWorld);
        when(mockPlayer.getUniqueId()).thenReturn(uuid);
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
    void testRestoreOnlineSpectator_offlineWhenExecutingRevival() {
        Player mockPlayer = mock(Player.class);
        when(mockPlayer.getUniqueId()).thenReturn(uuid);
        when(mockPlayer.getGameMode()).thenReturn(GameMode.SPECTATOR);
        // Returns true during initial check, then false inside scheduled runnable
        when(mockPlayer.isOnline()).thenReturn(true).thenReturn(false);
        mockedBukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(mockPlayer);

        PlayerRevivalUtil.restoreOnlineSpectator(plugin, playerData);

        verify(mockPlayer, never()).setGameMode(any());
        verify(mockPlayer, never()).sendMessage(anyString());
    }

    @Test
    void testRestoreOnlineSpectator_offlineWhenTransferring() {
        Player mockPlayer = mock(Player.class);
        World mockWorld = mock(World.class);
        when(mockWorld.getViewDistance()).thenReturn(10);
        when(mockPlayer.getWorld()).thenReturn(mockWorld);
        when(mockPlayer.getUniqueId()).thenReturn(uuid);
        when(mockPlayer.getGameMode()).thenReturn(GameMode.SPECTATOR);
        // Returns true during initial check and executeRevival check, then false in scheduleTransfer
        when(mockPlayer.isOnline()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockPlayer.getName()).thenReturn("TestUser");
        mockedBukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(mockPlayer);

        when(plugin.isLimboServer()).thenReturn(true);

        PlayerRevivalUtil.restoreOnlineSpectator(plugin, playerData);

        verify(mockPlayer).setGameMode(GameMode.SURVIVAL);
        verify(mockPlayer, never()).sendPluginMessage(any(), anyString(), any());
    }

    @Test
    void testClearDlcDeathState_handlesException() {
        doThrow(new RuntimeException("Storage error")).when(RPStatic.DEAD_STORAGE).saveConfig();

        PlayerRevivalUtil.restoreOnlineSpectator(plugin, playerData);

        verify(logger).warning("Failed to clear DLC death state for TestUser");
    }
}
